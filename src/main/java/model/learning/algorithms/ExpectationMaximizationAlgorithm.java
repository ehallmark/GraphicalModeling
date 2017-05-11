package model.learning.algorithms;

import model.functions.heuristic.MinimalCliqueSizeHeuristic;
import model.graphs.BayesianNet;
import model.graphs.CliqueTree;
import model.graphs.Graph;
import model.graphs.MarkovNet;
import model.learning.distributions.DistributionCreator;
import model.nodes.FactorNode;
import util.MathHelper;

import java.util.*;
import java.util.stream.Collectors;

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
        List<String> nodeLabels = graph.getAllNodesList().stream().filter(node->!assignmentCopy.containsKey(node.getLabel())).map(node->node.getLabel()).collect(Collectors.toList());

        // Handles most cases
        CliqueTree cliqueTree = graph.createCliqueTree();
        cliqueTree.setCurrentAssignment(assignmentCopy);
        Map<String,FactorNode> expectations = cliqueTree.runBeliefPropagation(nodeLabels);
        expectations.forEach((label,factor)->{
            // Find Expectation
            double[] weights = factor.getWeights();
            int maxIdx = MathHelper.indexOfMaxValue(weights);
            if(maxIdx<0||maxIdx>factor.getCardinality()) throw new RuntimeException("Invalid assignment: "+maxIdx);
            assignmentCopy.put(label,maxIdx);
        });
        return assignmentCopy;
    }
}
