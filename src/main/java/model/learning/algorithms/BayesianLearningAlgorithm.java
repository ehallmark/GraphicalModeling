package model.learning.algorithms;

import model.graphs.Graph;
import model.learning.algorithms.LearningAlgorithm;
import model.learning.distributions.Distribution;

import java.util.function.Function;

/**
 * Created by ehallmark on 4/28/17.
 */
public class BayesianLearningAlgorithm implements LearningAlgorithm {
    protected Distribution prior;
    public BayesianLearningAlgorithm(Distribution prior) {
        this.prior=prior;
    }

    @Override
    public Function<Graph, Void> runAlgorithm() {
        return null;
    }

    @Override
    public Function<Graph, Double> computeCurrentScore() {
        return null;
    }
}
