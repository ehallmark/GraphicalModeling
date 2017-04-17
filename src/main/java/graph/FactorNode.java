package graph;

import util.FloatPair;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created by Evan on 4/13/2017.
 */
public class FactorNode extends Node {
    private float[] weights;
    private int[] strides;
    private int[] cardinalities;
    private int numVariables;
    private Map<String,Integer> cardinalityMap;
    private Map<String,Integer> strideMap;
    private String[] varLabels;
    private Map<String,Integer> varToIndexMap;

    // 1 dimension array
    private FactorNode(String label, float[] weights, String[] varLabels, int[] cardinalities) {
        super(label);
        if(varLabels.length!=cardinalities.length) throw new RuntimeException("varLabels and Cardinalities must have same size");
        this.varLabels=varLabels;
        this.cardinalities=cardinalities;
        this.weights=weights;
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
        List<String> Ys = new ArrayList<>(numVariables);
        Arrays.stream(varLabels).forEach(x->{
            if(!Zset.contains(x)) Ys.add(x);
        });

        int num = Ys.size();
        int[] unionCardinalities = new int[num];
        for(int i = 0; i < num; i++) {
            unionCardinalities[i] = cardinalityMap.get(Ys.get(i));
        }
        int numAssignmentsTotal = numAssignmentCombinations(unionCardinalities);
        float[] psi = new float[numAssignmentsTotal];
        List<String> Zs = new ArrayList<>(Zset);
        Ys.forEach(y->{
            int yIdx = varToIndexMap.get(y);
            Zs.forEach(z->{
                int zIdx = varToIndexMap.get(z);
                for(int i = 0; i < cardinalities[yIdx]; i++) {
                    for(int j = 0; j < cardinalities[zIdx]; j++) {
                        // compute assignment
                        Arrays.copyOf(as)
                        // get index from assigment
                        int index = assignmentToIndex(assigment)
                        // apply psi[yIdx]=psi[yIdx]+weights[assignmentToIdx(assigment)]
                        psi[yIdx]=psi[yIdx]+weights[index];
                    }
                }
            });
        });
    }

    public FactorNode multiply(FactorNode other) {
        return applyFunction(other,(pair->pair._1*pair._2));
    }

    public FactorNode divideBy(FactorNode other) {
        return applyFunction(other,(pair->pair._2==0?0:pair._1/pair._2));
    }

    public FactorNode applyFunction(FactorNode other, Function<FloatPair,Float> f) {
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
        for( int i = 0; i < numAssignmentsTotal-1; i++) {
            psi[i] = f.apply(new FloatPair(weights[j],other.weights[k]));
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

        return new FactorNode(label,psi,unionLabels,unionCardinalities);
    }

    public void init() {
        this.strides=computeStrides();
        this.cardinalityMap=new HashMap<>();
        this.strideMap=new HashMap<>();
        this.varToIndexMap=new HashMap<>();
        for(int i = 0; i < numVariables; i++) {
            cardinalityMap.put(varLabels[i],cardinalities[i]);
            strideMap.put(varLabels[i],strides[i]);
            varToIndexMap.put(varLabels[i],i);
        }
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

    public static void main(String[] args) throws Exception {
        // TEST
        float[] phi1 = new float[] {
                0.2f,0.25f,0f,0.3f,0.1f,0.15f
        };
        float[] phi2 = new float[] {
                0.1f,0.25f,0f,0.3f,0f,0.1f,0.1f,0.24f,0.03f
        };
        FactorNode AB = new FactorNode("FactorNode 1",phi1,new String[]{"A","B"},new int[]{2,3});
        FactorNode BC = new FactorNode("FactorNode 2",phi2,new String[]{"B","C"},new int[]{3,3});
        FactorNode result = AB.multiply(BC);
        FactorNode result2 = BC.multiply(AB);
        System.out.println("AB * BC: "+ Arrays.toString(result.weights));
        System.out.println("BC * AB: "+Arrays.toString(result2.weights));
    }
}
