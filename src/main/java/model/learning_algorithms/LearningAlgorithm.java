package model.learning_algorithms;

import model.graphs.Graph;

import java.util.function.Function;

/**
 * Created by Evan on 4/24/2017.
 */
public interface LearningAlgorithm {
    Function<Graph,Void> getAlgorithm();
}
