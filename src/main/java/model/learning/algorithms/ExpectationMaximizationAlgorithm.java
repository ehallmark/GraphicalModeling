package model.learning.algorithms;

import model.graphs.Graph;
import model.learning.distributions.DistributionCreator;
import util.MathHelper;

import java.util.*;

/**
 * Created by ehallmark on 5/10/17.
 */
public class ExpectationMaximizationAlgorithm extends BayesianLearningAlgorithm {

    public ExpectationMaximizationAlgorithm(DistributionCreator creator) {
        super(creator);
    }

    @Override
    protected Map<String,Integer> handleAssignment(Map<String,Integer> assignment, Graph graph) {
        Map<String,Integer> assignmentCopy = new HashMap<>(assignment);
        graph.setCurrentAssignment(assignmentCopy);
        graph.getAllNodesList().forEach(node->{
            if(!assignmentCopy.containsKey(node.getLabel())) {
                // Find Expectation
                double[] weights = graph.variableElimination(new String[]{node.getLabel()}).getWeights();
                int maxIdx = MathHelper.indexOfMaxValue(weights);
                if(maxIdx<0||maxIdx>=node.getCardinality()) throw new RuntimeException("Invalid assignment: "+maxIdx);
                assignmentCopy.put(node.getLabel(),maxIdx);
            }
        });
        return assignmentCopy;
    }
}
