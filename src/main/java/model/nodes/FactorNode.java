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
    protected INDArray stridesVec;
    @Getter
    protected int[] cardinalities;
    @Getter
    protected int numVariables;
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
            String Yi = Ys.get(i);
            int idx = varToIndexMap.get(Yi);
            newCardinalities[i] = cardinalities[idx];
        }
        int newNumAssignments = numAssignmentCombinations(newCardinalities);
        int[] newStridesPrim = computeStrides(newCardinalities);
        INDArray newStrides = Nd4j.create(newStridesPrim.length);
        for(int i = 0; i < newStridesPrim.length; i++) {
            newStrides.putScalar(i,newStridesPrim[i]);
        }

        // keep indices sorted
        SortedSet<Integer> indicesToSumOver = new TreeSet<>();
        for(String z : Zset) {
            indicesToSumOver.add(varToIndexMap.get(z));
        }
        Map<String,INDArray> newValuesMap = new HashMap<>(valueMap);
        Zset.forEach(z->newValuesMap.remove(z));

        double[] weightsCopy = weights.data().asDouble();
        double[] psi = new double[newNumAssignments];

        this.assignmentPermutationsStream().parallel().forEach(permutation->{
            double[] assignmentsToKeep = new double[newCardinalities.length];
            double[] permutationPrim = permutation.data().asDouble();
            int j = 0;
            for(int i = 0; i < cardinalities.length; i++) {
                if(!indicesToSumOver.contains(i)) {
                    assignmentsToKeep[j] = permutationPrim[i];
                    j++;
                }
            }
            int oldIdx = assignmentToIndex(permutation);
            int newIdx = assignmentToIndex(Nd4j.create(assignmentsToKeep),newStrides);
            double w = weightsCopy[oldIdx];
            psi[newIdx] = psi[newIdx] + w;
        });
        return new FactorNode(Nd4j.create(psi),newLabels,newCardinalities,newValuesMap);
    }

    // returns all possible assignments with given cardinality array
    public Stream<INDArray> assignmentPermutationsStream() {
        List<Integer> indices = new ArrayList<>(numAssignments); for(int i = 0; i < numAssignments; i++) indices.add(i);
        return indices.stream().map(idx->{
            double[] assignment = new double[cardinalities.length];
            for(int i = 0; i < cardinalities.length; i++) {
                assignment[i]=indexToAssignment(varLabels[i],idx);
            }
            return Nd4j.create(assignment);
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
        int[] myUnionStrides = new int[unionSize];
        int[] otherUnionStrides = new int[unionSize];
        for(int i = 0; i < unionSize; i++) {
            String label = unionLabels[i];
            Integer myIdx = varToIndexMap.get(label);
            Integer otherIdx = other.varToIndexMap.get(label);
            if(myIdx!=null && otherIdx!=null) {
                myUnionStrides[i] = strides[myIdx];
                unionCardinalities[i] = cardinalities[otherIdx];
                otherUnionStrides[i] = other.strides[otherIdx];
            } else if(myIdx==null) {
                myUnionStrides[i] = 0;
                unionCardinalities[i] = other.cardinalities[otherIdx];
                otherUnionStrides[i] = other.strides[otherIdx];
            } else if(otherIdx==null) {
                myUnionStrides[i] = strides[myIdx];
                unionCardinalities[i] = cardinalities[myIdx];
                otherUnionStrides[i] = 0;
            }
        }

        // Continue with alg.
        int j = 0;
        int k = 0;
        int[] assignments = new int[unionSize];
        for(int l = 0; l < unionSize; l++) {
            assignments[l]=0;
        }

        double[] myWeights = weights.data().asDouble();
        double[] otherWeights = other.weights.data().asDouble();
        int numAssignmentsTotal = numAssignmentCombinations(unionCardinalities);
        double[] psi = new double[numAssignmentsTotal];

        for( int i = 0; i < numAssignmentsTotal; i++) {
            psi[i] = f.apply(new DoubleDoublePair(myWeights[j],otherWeights[k]));
            for(int l = 0; l < unionSize; l++) {
                assignments[l]++;
                int myStride = myUnionStrides[l];
                int otherStride = otherUnionStrides[l];
                if(assignments[l]==unionCardinalities[l]) {
                    assignments[l]=0;
                    j -= (unionCardinalities[l] - 1)*myStride;
                    k -= (unionCardinalities[l] - 1)*otherStride;
                } else {
                    j += myStride;
                    k += otherStride;
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
        for(int i = 0; i < cardinalities[0]; i++) {
            curr+=weights.getDouble(i);
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
        this.stridesVec=Nd4j.create(numVariables);
        this.varToIndexMap=new HashMap<>();
        this.numAssignments=1;
        for(int i = 0; i < numVariables; i++) {
            varToIndexMap.put(varLabels[i],i);
            numAssignments*=cardinalities[i];
            stridesVec.putScalar(i,strides[i]);
        }
        if(this.values==null) {
            this.values = Nd4j.create(numAssignments);
            for(int i = 0; i < numAssignments; i++) {
                final int idx = i;

                double val = 0d;
                for(String label : varLabels) {
                    int y = indexToAssignment(label,idx);
                    INDArray values = valueMap.get(label);
                    if(values!=null) {
                        val+=values.getDouble(y);
                    }
                }
                values.putScalar(idx,val);
            }

        }
        if(weights!=null && numAssignments!=weights.length()) throw new RuntimeException("Invalid factor dimensions");
        if(values!=null && numAssignments!=values.length()) throw new RuntimeException("Invalid value dimensions");
    }

    public void reNormalize(NormalizationFunction f) {
        f.normalize(weights);
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

    public int assignmentToIndex(INDArray assignments) {
        return assignmentToIndex(assignments,stridesVec);
    }

    public static int assignmentToIndex(INDArray assignments, INDArray strides) {
        if(assignments.length()!=strides.length()) throw new RuntimeException("Invalid number of assignments. Should have size: "+strides.length());
        INDArray scalar = assignments.mmul(strides.transpose());
        if(scalar.isScalar()) return scalar.getInt(0);
        else throw new RuntimeException("Not scalar but should be");
        /*int index = 0;
        for(int i = 0; i < assignments.length; i++) {
            index+= (assignments[i]*strides[i]);
        }
        return index;*/
    }

    public int indexToAssignment(String varName, int assignmentIdx) {
        Integer varIdx = varToIndexMap.get(varName);
        if(varIdx == null) throw new RuntimeException("Variable "+varName+" not found.");
        int stride = strides[varIdx];
        int cardinality = cardinalities[varIdx];
        return (assignmentIdx/stride) % cardinality;
    }

    public int[] computeStrides() {
        return computeStrides(cardinalities);
    }

    public static int[] computeStrides(int[] cardinalities) {
        int strides[] = new int[cardinalities.length];
        int stride = 1;
        for(int i = 0; i < cardinalities.length; i++) {
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
