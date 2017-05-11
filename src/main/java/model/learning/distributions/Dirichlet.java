package model.learning.distributions;

import model.functions.normalization.DivideByPartition;
import model.graphs.Graph;
import model.nodes.FactorNode;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by ehallmark on 4/28/17.
 */
public class Dirichlet implements Distribution {
    protected double alpha;
    protected FactorNode factor;
    public Dirichlet(FactorNode factor, double alpha) {
        this.alpha=alpha;
        this.factor=factor;
    }

    @Override
    public void train(Map<String,Integer> assignmentMap) {
        INDArray assignments = Nd4j.create(factor.getNumVariables());
        factor.getVarToIndexMap().forEach((var,idx)->{
            Integer varAssignment = assignmentMap.get(var);
            if(varAssignment==null) throw new RuntimeException("Null assignment");
            assignments.putScalar(idx,varAssignment);
        });
        int[] assignment = new int[factor.getNumVariables()];
        for(int i = 0; i < assignment.length; i++) {
            assignment[i]=assignments.getInt(i);
        }
        int idx = factor.assignmentToIndex(assignment);
        factor.incrementAtIndex(idx);
    }

    @Override
    public void finish() {
        factor.setWeights(Arrays.copyOf(factor.getWeightsCopy(),factor.getNumAssignments()));
        factor.reNormalize(new DivideByPartition());
    }

    @Override
    public void initialize() {
        double[] newWeights = new double[factor.getNumAssignments()];
        Arrays.fill(newWeights,alpha);
        factor.setWeightsCopy(newWeights);
    }
}
