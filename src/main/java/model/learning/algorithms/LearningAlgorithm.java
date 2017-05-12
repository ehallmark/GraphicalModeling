package model.learning.algorithms;

import model.graphs.Graph;

import java.util.function.Function;

/**
 * Created by Evan on 4/24/2017.
 */
public interface LearningAlgorithm {
    // returns convergence
    boolean runAlgorithm();
    double computeCurrentScore();
}
