package model.learning.algorithms;

import model.functions.heuristic.MinimalCliqueSizeHeuristic;
import model.functions.inference_methods.InferenceMethod;
import model.graphs.BayesianNet;
import model.graphs.CliqueTree;
import model.graphs.Graph;
import model.graphs.MarkovNet;
import model.learning.distributions.DirichletCreator;
import model.learning.distributions.DistributionCreator;
import model.nodes.FactorNode;
import util.MathHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 5/10/17.
 */
public class ExpectationMaximizationAlgorithm extends BayesianLearningAlgorithm {
    protected InferenceMethod inferenceMethod;
    public ExpectationMaximizationAlgorithm(Graph graph, double alpha, InferenceMethod inferenceMethod) {
        super(graph,alpha);
        this.inferenceMethod=inferenceMethod;
    }

    protected ExpectationMaximizationAlgorithm(Graph graph, DistributionCreator creator, InferenceMethod inferenceMethod) {
        super(graph,creator);
        this.inferenceMethod=inferenceMethod;
    }

    @Override
    protected Map<String,Integer> handleAssignment(Map<String,Integer> assignment, Graph graph) {
        return inferenceMethod.nextAssignments(graph,assignment);
    }
}
