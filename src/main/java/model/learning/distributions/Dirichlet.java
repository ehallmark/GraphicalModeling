package model.learning.distributions;

import lombok.Getter;
import lombok.Setter;
import model.functions.normalization.DivideByPartition;
import model.graphs.Graph;
import model.nodes.FactorNode;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import util.MathHelper;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ehallmark on 4/28/17.
 */
public class Dirichlet implements Distribution {
    private static final double EPSILON = 0.00005;
    protected double alpha;
    protected FactorNode factor;
    protected AtomicInteger seenSoFar;
    protected double[] accumulatedValues;
    protected double[] expectationsInData;
    protected double[] expectationsInTheta;
    protected double[] weightsCopy;
    protected boolean converged;
    @Getter @Setter
    protected double learningRate = 0.01d;
    protected boolean useGradientDescent;
    public Dirichlet(FactorNode factor, double alpha, boolean useGradientDescent) {
        this.alpha=alpha;
        this.factor=factor;
        this.converged=false;
        this.useGradientDescent=useGradientDescent;
        this.seenSoFar=new AtomicInteger(0);
        this.accumulatedValues=new double[factor.getNumAssignments()];
        this.expectationsInData=new double[factor.getNumAssignments()];
        this.expectationsInTheta=new double[factor.getNumAssignments()];
        this.weightsCopy=new double[factor.getNumAssignments()];
        Arrays.fill(this.accumulatedValues,0d);
        Arrays.fill(this.weightsCopy,0d);
    }


    public boolean getConverged() {
        return converged;
    }

    @Override
    public void train(Map<String,Integer> assignmentMap) {
        int[] assignment = new int[factor.getNumVariables()];
        factor.getVarToIndexMap().forEach((var,idx)->{
            Integer varAssignment = assignmentMap.get(var);
            if(varAssignment==null) throw new RuntimeException("Null assignment");
            assignment[idx]=varAssignment;
        });
        int idx = factor.assignmentToIndex(assignment);
        weightsCopy[idx]++;
        if(useGradientDescent) {
            accumulatedValues[idx]+=factor.getValues()[idx];
        }
        seenSoFar.getAndIncrement();
    }

    @Override
    public void updateFactorWeights() {
        if(factor.getWeights()==null || !useGradientDescent) {
            factor.setWeights(Arrays.copyOf(weightsCopy, weightsCopy.length));
            factor.reNormalize(new DivideByPartition());
        }

        if(useGradientDescent && seenSoFar.get()>0) {
            converged = true;
            double[] values = factor.getValues();
            final int M = seenSoFar.get();
            double[] weights = factor.getWeights();
            for (int i = 0; i < factor.getNumAssignments(); i++) {
                double expectData = accumulatedValues[i] / M;
                double expectTheta = (weightsCopy[i] * values[i]) / M;
                double deriv = expectData - expectTheta;
                if (Math.abs(deriv) > EPSILON) converged = false;
                System.out.println("Deriv: " + deriv);
                weights[i] = weights[i] + learningRate * deriv;
            }
        }
    }

    @Override
    public void initialize() {
        double[] newWeights = new double[factor.getNumAssignments()];
        Arrays.fill(newWeights,alpha);
        weightsCopy=newWeights;
    }
}
