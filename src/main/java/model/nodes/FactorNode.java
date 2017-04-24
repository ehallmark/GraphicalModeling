package model.nodes;

import lombok.Getter;
import util.FloatFloatPair;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by Evan on 4/13/2017.
 */
public class FactorNode extends Node {
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
    @Getter
    protected Map<String,Integer> varToIndexMap;
    protected int numAssignments;

    // 1 dimension array // if null then unnamed
    public FactorNode(float[] weights, String[] varLabels, int[] cardinalities) {
        super(null,0,false);
        if(varLabels.length!=cardinalities.length) throw new RuntimeException("varLabels and Cardinalities must have same size");
        this.varLabels=varLabels;
        this.cardinalities=cardinalities;
        this.weights=weights;
        this.numVariables=cardinalities.length;
        this.init();
    }

    public int cardinalityFor(String varName) {
        Integer c = cardinalityMap.get(varName);

        if(c==null || c < 0) return 0;

        return c;
    }

    public FactorNode sumOut(String[] toSumOver) {
        Collection<String> Zset = new HashSet<>();
        Arrays.stream(toSumOver).forEach(z->{
            Integer idx = varToIndexMap.get(z);
            if(idx!=null) {
                Zset.add(z);
            }
        });
        List<String> Ys = new ArrayList<>(numVariables);
        for(String x : varLabels) {
            if(!Zset.contains(x)) Ys.add(x);
        }

        int num = Ys.size();
        int[] newCardinalities = new int[num];
        String[] newLabels = Ys.toArray(new String[num]);
        for(int i = 0; i < num; i++) {
            newCardinalities[i] = cardinalityMap.get(Ys.get(i));
        }
        int numAssignmentsTotal = numAssignmentCombinations(newCardinalities);
        int[] newStrides = computeStrides(newCardinalities,newLabels.length);
        float[] psi = new float[numAssignmentsTotal];
        List<String> Zs = new ArrayList<>(Zset);
        Set<Integer> indicesToSumOver = new HashSet<>();
        for(int i = 0; i < Zs.size(); i++) {
            String z = Zs.get(i);
            indicesToSumOver.add(varToIndexMap.get(z));
        }
        this.assignmentPermutationsStream().forEach(permutation->{
            int[] assignmentsToKeep = new int[newCardinalities.length];
            int j = 0;
            for(int i = 0; i < cardinalities.length; i++) {
                if(!indicesToSumOver.contains(i)) {
                    assignmentsToKeep[j]=permutation[i];
                    j++;
                }
            }
            int oldIdx = assignmentToIndex(permutation);
            int newIdx = assignmentToIndex(assignmentsToKeep,newStrides,newLabels.length);
            psi[newIdx]=psi[newIdx]+weights[oldIdx];
        });
        return new FactorNode(psi,newLabels,newCardinalities);
    }

    // returns all possible assignments with given cardinality array
    public Stream<int[]> assignmentPermutationsStream() {
        int numAssignments=numAssignmentCombinations(cardinalities);
        List<Integer> indices = new ArrayList<>(numAssignments); for(int i = 0; i < numAssignments; i++) indices.add(i);
        return indices.stream().map(idx->{
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

    public FactorNode applyFunction(FactorNode other, Function<FloatFloatPair,Float> f) {
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
        for(int l = 0; l < unionSize; l++) {
            assignments[l]=0;
        }

        int numAssignmentsTotal = numAssignmentCombinations(unionCardinalities);
        float[] psi = new float[numAssignmentsTotal];
        for( int i = 0; i < numAssignmentsTotal; i++) {
            psi[i] = f.apply(new FloatFloatPair(weights[j],other.weights[k]));
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
        return new FactorNode(psi,unionLabels,unionCardinalities);
    }

    public void init() {
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
        if(numAssignments!=weights.length) throw new RuntimeException("Invalid factor dimensions");
    }

    public void reNormalize(Function<float[],float[]> f) {
        weights=f.apply(weights);
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
        return "Name: "+label+"\n"+"Scope: "+Arrays.toString(varLabels)+"\n"+
                "Factors: "+Arrays.toString(weights);
    }
}
