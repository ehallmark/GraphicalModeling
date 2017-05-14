package model.learning.distributions;

import lombok.Getter;
import lombok.Setter;
import model.functions.normalization.DivideByPartition;
import model.nodes.FactorNode;
import util.MathHelper;


import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ehallmark on 4/28/17.
 */
public class Dirichlet implements Distribution {
    private static final double EPSILON = 0.00001;
    protected double alpha;
    protected FactorNode factor;
    protected AtomicInteger seenSoFar;
    protected double[] accumulatedValues;
    protected double[] expectationsInData;
    protected double[] expectationsInTheta;
    protected double[] weightsCopy;
    protected double[] previousWeightsCopy;
    protected boolean converged;
    @Getter
    protected double score;
    @Getter @Setter
    protected double learningRate = 0.01d;
    protected boolean useGradientDescent;
    public Dirichlet(FactorNode factor, double alpha, boolean useGradientDescent) {
        this.alpha=alpha;
        this.factor=factor;
        this.converged=false;
        this.useGradientDescent=useGradientDescent;
        this.seenSoFar=new AtomicInteger(0);
        this.score=Double.MAX_VALUE;
        if(useGradientDescent) {
            this.accumulatedValues=new double[factor.getNumAssignments()];
            this.expectationsInData=new double[factor.getNumAssignments()];
            this.expectationsInTheta=new double[factor.getNumAssignments()];
            Arrays.fill(this.accumulatedValues,0d);
        }
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
        } else {
            if (previousWeightsCopy != null && seenSoFar.get()>1) {
                converged=true;
                score = MathHelper.computeDistance(weightsCopy,previousWeightsCopy);
                score=Math.abs(score);
                updateConvergedStatus();
            }
            previousWeightsCopy = Arrays.copyOf(weightsCopy, weightsCopy.length);
        }

        // increment final counter
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
            score=0d;
            double[] values = factor.getValues();
            final int M = seenSoFar.get();
            double[] weights = factor.getWeights();
            for (int i = 0; i < factor.getNumAssignments(); i++) {
                double expectData = accumulatedValues[i] / M;
                double expectTheta = (weightsCopy[i] * values[i]) / M;
                double deriv = expectData - expectTheta;
                score+=deriv;
                weights[i] = weights[i] + learningRate * deriv;
            }
            score/=factor.getNumAssignments();
            score=Math.abs(score);
            updateConvergedStatus();
        }
    }

    private void updateConvergedStatus() {
        if (score > EPSILON) converged = false;
    }

    @Override
    public void initialize() {
        double[] newWeights = new double[factor.getNumAssignments()];
        Arrays.fill(newWeights,alpha);
        weightsCopy=newWeights;
    }
}
