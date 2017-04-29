package model.learning.distributions;

import model.functions.normalization.DivideByPartition;
import model.graphs.Graph;
import model.nodes.FactorNode;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ehallmark on 4/28/17.
 */
public class Dirichlet implements Distribution {
    protected float alpha;
    protected FactorNode factor;
    public Dirichlet(FactorNode factor, float alpha) {
        this.alpha=alpha;
        this.factor=factor;
    }

    @Override
    public void train(Map<String,int[]> assignmentMap, int batchSize) {
        System.out.println("Starting batch");
        INDArray assignments = Nd4j.create(factor.getNumVariables(),batchSize);
        factor.getVarToIndexMap().forEach((var,idx)->{
            int[] varAssignments = assignmentMap.get(var);
            if(varAssignments==null) throw new RuntimeException("Null assignment");
            float[] assignmentFloats = new float[varAssignments.length];
            for(int i = 0; i < varAssignments.length; i++) {
                assignmentFloats[i]=(float)varAssignments[i];
            }
            assignments.putRow(idx,Nd4j.create(assignmentFloats));
        });
        for(int col = 0; col < assignments.columns(); col++) {
            int[] assignment = new int[factor.getNumVariables()];
            for(int i = 0; i < assignment.length; i++) {
                assignment[i]=assignments.getInt(i,col);
            }
            int idx = factor.assignmentToIndex(assignment);
            factor.incrementAtIndex(idx);
        }
        factor.reNormalize(new DivideByPartition());
    }

    @Override
    public void initializeWeights() {
        float[] newWeights = new float[factor.getNumAssignments()];
        Arrays.fill(newWeights,alpha);
        factor.setWeights(newWeights);
    }
}
