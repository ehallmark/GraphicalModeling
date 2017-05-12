package model.learning.algorithms;

import model.graphs.Graph;
import model.learning.distributions.Distribution;
import model.learning.distributions.DistributionCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 5/12/17.
 */
public abstract class AbstractLearningAlgorithm implements LearningAlgorithm {
    protected DistributionCreator creator;
    protected List<Distribution> distributions;
    protected Graph graph;
    protected boolean converged;
    protected AbstractLearningAlgorithm(DistributionCreator creator, Graph graph) {
        this.creator=creator;
        this.distributions=new ArrayList<>();
        this.graph=graph;
        this.converged=false;
        graph.getFactorNodes().forEach(factor -> {
            distributions.add(creator.create(factor));
        });
    }

    @Override
    public boolean runAlgorithm() {
        graph.getTrainingData().forEach(assignment->{
            if(!converged()) {
                Map<String, Integer> cleanAssignment = handleAssignment(assignment, graph);
                distributions.forEach(distribution -> {
                    distribution.train(cleanAssignment);
                });
            }
        });
        // set factors and normalize
        distributions.forEach(distribution -> {
            distribution.updateFactorWeights();
        });
        return converged();
    }

    public boolean converged() {
        if(converged) return true;
        converged = distributions.stream().allMatch(distribution -> distribution.getConverged());
        return converged; // DON'T CALL 'converged()' OR IT WILL POTENTIALLY RUN FOREVER!!!
    }

    // Handle an assignment before learning (say for Expecation Maximization algorithm)
    protected abstract Map<String,Integer> handleAssignment(Map<String,Integer> assignment, Graph graph);

    @Override
    public double computeCurrentScore() {
        return distributions.stream().collect(Collectors.averagingDouble(distribution -> distribution.getScore()));
    }
}
