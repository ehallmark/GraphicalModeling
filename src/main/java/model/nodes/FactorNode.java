package model.nodes;

import lombok.Getter;
import lombok.Setter;
import model.functions.normalization.NormalizationFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import util.DoubleDoublePair;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Evan on 4/13/2017.
 */
public class FactorNode extends Node {
    private static final Random rand = new Random(69);
    @Getter
    protected int[] strides;
    @Getter
    protected int[] cardinalities;
    @Getter
    protected int numVariables;
    protected Map<String,Integer> cardinalityMap;
    protected Map<String,Integer> strideMap;
    @Getter
    protected String[] varLabels;
    @Getter @Setter
    protected INDArray weights;
    @Getter
    protected Map<String,Integer> varToIndexMap;
    @Getter
    protected int numAssignments;

    public FactorNode(INDArray weights, String[] varLabels, int[] cardinalities, Map<String,INDArray> valueMap) {
        super(null,varLabels.length,null);
        if(varLabels.length!=cardinalities.length) throw new RuntimeException("varLabels and Cardinalities must have same size");
        this.varLabels=varLabels;
        this.cardinalities=cardinalities;
        this.weights=weights;
        this.valueMap.putAll(valueMap);
        this.numVariables=cardinalities.length;
        this.init();
    }

    public FactorNode sumOut(String[] toSumOver) {
        Collection<String> Zset = new HashSet<>();
        Arrays.stream(toSumOver).forEach(z->{
            Integer idx = varToIndexMap.get(z);
            if(idx!=null) {
                Zset.add(z);
            }
        });
        List<String> Ys = new ArrayList<>(Arrays.asList(varLabels));
        Zset.forEach(z->Ys.remove(z));

        int num = Ys.size();
        int[] newCardinalities = new int[num];
        String[] newLabels = Ys.toArray(new String[num]);
        for(int i = 0; i < num; i++) {
            newCardinalities[i] = cardinalityMap.get(Ys.get(i));
        }
        // keep indices sorted
        SortedSet<Integer> indicesToSumOver = new TreeSet<>();
        for(String z : Zset) {
            indicesToSumOver.add(varToIndexMap.get(z));
        }
        Map<String,INDArray> newValuesMap = new HashMap<>(valueMap);
        Zset.forEach(z->newValuesMap.remove(z));
        // reshape
        INDArray newWeights = weights.dup().reshape(cardinalities);
        int cnt = 0;
        for(int idx : indicesToSumOver) {
            newWeights = Nd4j.rollAxis(newWeights,idx-cnt,0).sum(0);
            cnt++;
        }

        // reshape
        newWeights=newWeights.reshape(1,newWeights.length());

        return new FactorNode(newWeights,newLabels,newCardinalities,newValuesMap);

        /*this.assignmentPermutationsStream().parallel().forEach(permutation->{
            int[] assignmentsToKeep = new int[newCardinalities.length];
            int j = 0;
            for(int i = 0; i < cardinalities.length; i++) {
                if(!indicesToSumOver.contains(i)) {
                    assignmentsToKeep[j] = permutation[i];
                    j++;
                }
            }
            int oldIdx = assignmentToIndex(permutation);
            int newIdx = assignmentToIndex(assignmentsToKeep,newStrides,newLabels.length);
            double w = weights.getDouble(oldIdx);
            psi[newIdx] = psi[newIdx] + w;
        });
        return new FactorNode(Nd4j.create(psi),newLabels,newCardinalities,newValuesMap);*/
    }

    // returns all possible assignments with given cardinality array
    public Stream<int[]> assignmentPermutationsStream() {
        int numAssignments=numAssignmentCombinations(cardinalities);
        List<Integer> indices = new ArrayList<>(numAssignments); for(int i = 0; i < numAssignments; i++) indices.add(i);
        return indices.parallelStream().map(idx->{
            int[] assignment = new int[cardinalities.length];
            for(int i = 0; i < cardinalities.length; i++) {
                assignment[i]=indexToAssignment(varLabels[i],idx);
            }
            return assignment;
        });
    }


    public FactorNode multiply(FactorNode other) {
        return applyFunction(other,(pair->pair._1*pair._2));
    }

    public FactorNode divideBy(FactorNode other) {
        return applyFunction(other,(pair->pair._2==0?0:pair._1/pair._2));
    }

    public FactorNode applyFunction(FactorNode other, Function<DoubleDoublePair,Double> f) {
        // Get the union of X1 and X2
        String[] unionLabels = labelUnion(other);
        int unionSize = unionLabels.length;
        int[] unionCardinalities = new int[unionSize];
        for(int i = 0; i < unionSize; i++) {
            String label = unionLabels[i];
            if(cardinalityMap.containsKey(label)) {
                unionCardinalities[i] = cardinalityMap.get(label);
            } else {
                // the other one better have it!
                unionCardinalities[i] = other.cardinalityMap.get(label);
            }
        }

        // Continue with alg.
        int j = 0;
        int k = 0;
        int[] assignments = new int[unionSize];
        Arrays.fill(assignments,0);

        double[] myWeights = weights.data().asDouble();
        double[] otherWeights = other.weights.data().asDouble();
        int numAssignmentsTotal = numAssignmentCombinations(unionCardinalities);
        double[] psi = new double[numAssignmentsTotal];
        for( int i = 0; i < numAssignmentsTotal; i++) {
            psi[i] = f.apply(new DoubleDoublePair(myWeights[j],otherWeights[k]));
            for(int l = 0; l < unionSize; l++) {
                assignments[l]++;
                if(assignments[l]==unionCardinalities[l]) {
                    assignments[l]=0;
                    j -= (unionCardinalities[l] - 1)*strideFor(unionLabels[l]);
                    k -= (unionCardinalities[l] - 1)*other.strideFor(unionLabels[l]);
                } else {
                    j += strideFor(unionLabels[l]);
                    k += other.strideFor(unionLabels[l]);
                    break;
                }
            }
        }
        Map<String,INDArray> newValueMap = new HashMap<>(valueMap);
        newValueMap.putAll(other.valueMap);
        return new FactorNode(Nd4j.create(psi),unionLabels,unionCardinalities,newValueMap);
    }

    public int nextSample() {
        if(numVariables>1||cardinalities.length<1)  throw new RuntimeException("Can only be a single factor scope");
        double curr = 0d;
        double r = rand.nextDouble();
        double[] weightsShallow = weights.data().asDouble();
        for(int i = 0; i < cardinalities[0]; i++) {
            curr+=weightsShallow[i];
            if(r <= curr) {
                return i;
            }
        }
        System.out.println("WARNING: Factor does not appear normalized");
        return cardinalities[0]-1;
    }

    public void init() {
        if(this.varLabels.length==0) throw new RuntimeException("No var labels");
        this.strides=computeStrides();
        this.cardinalityMap=new HashMap<>();
        this.strideMap=new HashMap<>();
        this.varToIndexMap=new HashMap<>();
        this.numAssignments=1;
        for(int i = 0; i < numVariables; i++) {
            cardinalityMap.put(varLabels[i],cardinalities[i]);
            strideMap.put(varLabels[i],strides[i]);
            varToIndexMap.put(varLabels[i],i);
            numAssignments*=cardinalities[i];
        }
        if(this.values==null) {
            double[] shallowValues = new double[numAssignments];
            double[][] shallowValuesPerVar = new double[numVariables][];
            for(int i = 0; i < numVariables; i++) {
                String varLabel = varLabels[i];
                if(!valueMap.containsKey(varLabel)) {
                    System.out.println("Oops");
                }
                shallowValuesPerVar[i]=valueMap.get(varLabel).data().asDouble();
            }
            for(int i = 0; i < numAssignments; i++) {
                final int idx = i;

                double val = 0d;
                for(int j = 0; j < numVariables; j++) {
                    String label = varLabels[j];
                    int y = indexToAssignment(label,idx);
                    val+=shallowValuesPerVar[j][y];
                }
                shallowValues[idx]=val;
            }
            this.values=Nd4j.create(shallowValues);
        }
        if(weights!=null && numAssignments!=weights.length()) throw new RuntimeException("Invalid factor dimensions");
        if(values!=null && numAssignments!=values.length()) throw new RuntimeException("Invalid value dimensions");
    }

    public void reNormalize(NormalizationFunction f) {
        f.normalize(weights);
    }

    public int strideFor(String varLabel) {
        Integer stride = strideMap.get(varLabel);
        if(stride==null)return 0;
        else return stride;
    }


    // where each cardinality number represents a distint variable
    public static int numAssignmentCombinations(int[] cardinalities) {
        int num = 1;
        for(int i = 0; i < cardinalities.length; i++) {
            num*=cardinalities[i];
        }
        return num;
    }


    protected String[] labelUnion(FactorNode other) {
        Set<String> varUnion = new HashSet<>();
        for(String label : varLabels) varUnion.add(label);
        for(String label : other.varLabels) varUnion.add(label);
        String[] unionArray = new String[varUnion.size()];
        return varUnion.toArray(unionArray);
    }

    public int assignmentToIndex(int[] assignments) {
        return assignmentToIndex(assignments,strides,numVariables);
    }

    public static int assignmentToIndex(int[] assignments, int[] strides, int numVariables) {
        if(assignments.length!=strides.length) throw new RuntimeException("Invalid number of assignments. Should have size: "+strides.length);
        int index = 0;
        for(int i = 0; i < numVariables; i++) {
            index+= (assignments[i]*strides[i]);
        }
        return index;
    }

    public int indexToAssignment(String varName, int index) {
        Integer stride = strideMap.get(varName);
        Integer cardinality = cardinalityMap.get(varName);
        if(stride==null||cardinality==null) throw new RuntimeException("Variable "+varName+" not found.");
        return (index/stride) % cardinality;
    }

    public int[] computeStrides() {
        return computeStrides(cardinalities,numVariables);
    }

    public static int[] computeStrides(int[] cardinalities, int numVariables) {
        int strides[] = new int[numVariables];
        int stride = 1;
        for(int i = 0; i < numVariables; i++) {
            int cardinality = cardinalities[i];
            if(cardinality>0) {
                strides[i] = stride;
                stride = stride * cardinality;
            } else {
                throw new RuntimeException("Stride should be positive");
            }
        }
        return strides;
    }


    @Override
    public String toString() {
        return "Scope: "+Arrays.toString(varLabels)+"\n"+
                "Factor: "+weights;
    }
}
