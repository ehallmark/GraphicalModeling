package graphical_modeling.model.learning.algorithms;

import graphical_modeling.model.graphs.Graph;

import java.util.function.Function;

/**
 * Created by Evan on 4/24/2017.
 */
public interface LearningAlgorithm {
    // returns convergence
    boolean runAlgorithm();
    double computeCurrentScore();
}
