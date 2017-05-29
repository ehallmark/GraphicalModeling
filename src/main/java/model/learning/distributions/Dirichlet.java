package model.learning.distributions;

import lombok.Getter;
import lombok.Setter;
import model.functions.normalization.DivideByPartition;
import model.nodes.FactorNode;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.inverse.InvertMatrix;
import org.nd4j.linalg.ops.transforms.Transforms;
import util.MathHelper;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
    protected INDArray accumulatedValues;
    protected INDArray weightsCopy;
    protected INDArray previousWeightsCopy;
    protected boolean converged;
    // for L-BFGS
    protected LinkedList<INDArray> S;
    protected LinkedList<INDArray> Y;
    protected INDArray Gk;
    protected INDArray GkMinusOne;
    protected INDArray H0k;
    protected LinkedList<INDArray> P;
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
            this.accumulatedValues= Nd4j.zeros(factor.getNumAssignments());
            this.S = new LinkedList<>();
            this.Y = new LinkedList<>();
            this.P = new LinkedList<>();
            this.Gk = null;
            this.GkMinusOne = null;
            this.H0k = Nd4j.eye(factor.getNumAssignments());
        }
    }


    public boolean getConverged() {
        return converged;
    }

    @Override
    public void train(Map<String,Integer> assignmentMap) {
        // Set Previous Weights
        if(factor.getWeights()!=null)previousWeightsCopy = factor.getWeights().dup();

        int[] assignment = new int[factor.getNumVariables()];
        factor.getVarToIndexMap().forEach((var,idx)->{
            Integer varAssignment = assignmentMap.get(var);
            if(varAssignment==null) throw new RuntimeException("Null assignment");
            assignment[idx]=varAssignment;
        });

        int idx = factor.assignmentToIndex(assignment);

        weightsCopy.get(NDArrayIndex.point(idx)).addi(1d);
        if(useGradientDescent) {
            accumulatedValues.get(NDArrayIndex.point(idx)).addi(factor.getValues().getDouble(idx));
        }
        // increment final counter
        seenSoFar.getAndIncrement();
    }

    @Override
    public void updateFactorWeights() {
        if(factor.getWeights()==null || !useGradientDescent) {
            factor.setWeights(weightsCopy.dup());
        }

        if(useGradientDescent && seenSoFar.get()>0) {
            // Calculate L-BFGS
            {
                INDArray values = factor.getValues();
                final int M = seenSoFar.get();

                // Calculate derivative
                GkMinusOne = Gk;
                Gk = weightsCopy.mul(values).subi(accumulatedValues).divi(M);

                // Update Weights
                factor.getWeights().addi(Gk.mul(learningRate));
            }

            // Update Data
            {
                // update S
                if (previousWeightsCopy != null) {
                    S.add(factor.getWeights().sub(previousWeightsCopy));
                }
                // update Y
                if (GkMinusOne != null) {
                    Y.add(Gk.sub(GkMinusOne));
                }

                // update P
                if (Y.size() > 0 && S.size() > 0 ) {
                    P.add(Y.getLast().transpose().mmul(S.getLast()));
                    try {
                        InvertMatrix.invert(P.getLast(), true);
                    } catch(Exception e) {
                        System.out.println("Warning matrix is singular");
                        converged=true;
                        return;
                    }
                }
            }
        }
        factor.reNormalize(new DivideByPartition());

        // Check for convergence
        if (previousWeightsCopy != null) {
            score = Transforms.euclideanDistance(factor.getWeights(),previousWeightsCopy);
            score=Math.abs(score);
            updateConvergedStatus();
        }

    }

    private void updateConvergedStatus() {
        converged = (score < EPSILON);
    }

    @Override
    public void initialize() {
        weightsCopy=Nd4j.zeros(factor.getNumAssignments()).addi(alpha);
    }
}
