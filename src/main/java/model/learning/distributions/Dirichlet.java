package model.learning.distributions;

import lombok.Getter;
import lombok.Setter;
import model.functions.normalization.DivideByPartition;
import model.nodes.FactorNode;
import util.MathHelper;

import java.util.Arrays;
import java.util.LinkedList;
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
    protected double[] weightsCopy;
    protected double[] previousWeights;
    protected boolean converged;
    // for L-BFGS
    protected LinkedList<double[]> S;
    protected LinkedList<double[]> Y;
    protected double[] Gk;
    protected double[] GkMinusOne;
    protected double[] H0k;
    protected LinkedList<double[]> P;
    @Getter
    protected double score;
    @Getter @Setter
    protected double learningRate = 0.01d;
    protected boolean useGradientDescent;
    protected int historyLength = 10;

    public Dirichlet(FactorNode factor, double alpha, boolean useGradientDescent) {
        this.alpha=alpha;
        this.factor=factor;
        this.converged=false;
        this.useGradientDescent=useGradientDescent;
        this.seenSoFar=new AtomicInteger(0);
        this.score=Double.MAX_VALUE;
        if(useGradientDescent) {
            this.accumulatedValues= new double[factor.getNumAssignments()];
            /*this.S = new LinkedList<>();
            this.Y = new LinkedList<>();
            this.P = new LinkedList<>();
            this.H0k = Nd4j.eye(factor.getNumAssignments());
            */
            this.Gk = null;
            this.GkMinusOne = null;
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
        }
        // increment final counter
        seenSoFar.getAndIncrement();
    }

    @Override
    public void updateFactorWeights() {
        previousWeights=factor.getWeights();

        factor.setWeights(weightsCopy);
        factor.reNormalize(new DivideByPartition());


        // Check for convergence
        if (previousWeights != null) {
            score = MathHelper.euclideanDistance(factor.getWeights(),previousWeights);
            score = Math.abs(score);
            updateConvergedStatus();
        }

    }

    private void updateConvergedStatus() {
        if(!converged)converged = (score < EPSILON);
    }

    @Override
    public void initialize() {
        weightsCopy=new double[factor.getNumAssignments()];
        Arrays.fill(weightsCopy,alpha);
    }
}
