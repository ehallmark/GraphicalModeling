package model.learning.algorithms;

import model.graphs.CliqueTree;
import model.graphs.Graph;
import model.graphs.MarkovNet;
import model.learning.distributions.Distribution;
import model.learning.distributions.DistributionCreator;
import model.nodes.FactorNode;
import util.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Evan on 4/29/2017.
 */
public class MarkovLearningAlgorithm extends ExpectationMaximizationAlgorithm {
    public MarkovLearningAlgorithm(DistributionCreator creator) {
        super(creator);
        creator.setUseGradientDescent(true);
    }

    @Override
    public Function<Graph, Boolean> runAlgorithm() { // makes sure we are using a markov net
        Function<Graph, Graph> preFunc = (graph) -> {
            if (!(graph instanceof MarkovNet)) throw new RuntimeException("Should be using a markov net");
            return graph;
        };
        return preFunc.andThen(super.runAlgorithm());
    }

    @Override
    public Function<Graph, Double> computeCurrentScore() {
        return (graph->0d);
    }
}
