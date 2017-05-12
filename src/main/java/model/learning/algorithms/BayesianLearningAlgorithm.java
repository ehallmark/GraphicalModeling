package model.learning.algorithms;

import model.graphs.Graph;
import model.learning.distributions.DirichletCreator;
import model.learning.distributions.Distribution;
import model.learning.distributions.DistributionCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Evan on 4/29/2017.
 */
public class BayesianLearningAlgorithm extends AbstractLearningAlgorithm {
    public BayesianLearningAlgorithm(Graph graph, double alpha) {
        this(graph,new DirichletCreator(alpha));
    }

    protected BayesianLearningAlgorithm(Graph graph, DistributionCreator creator) {
        super(creator,graph);
    }

    protected Map<String,Integer> handleAssignment(Map<String,Integer> assignment, Graph graph) {
        return assignment;
    }

}
