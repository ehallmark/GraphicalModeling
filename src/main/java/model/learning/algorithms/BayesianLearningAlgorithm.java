package model.learning.algorithms;

import model.graphs.Graph;
import model.learning.distributions.Distribution;
import model.learning.distributions.DistributionCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Evan on 4/29/2017.
 */
public class BayesianLearningAlgorithm implements LearningAlgorithm {
    protected DistributionCreator creator;
    protected List<Distribution> distributions;
    public BayesianLearningAlgorithm(DistributionCreator creator) {
        this.creator=creator;
        this.distributions=new ArrayList<>();
    }
    @Override
    public Function<Graph, Void> runAlgorithm() {
        return (graph -> {
            if(distributions.isEmpty()) {
                graph.getFactorNodes().forEach(factor -> {
                    System.out.println("Adding factor: "+factor.toString());
                    distributions.add(creator.create(factor));
                });
            }
            System.out.println("Training Data Count: "+graph.getTrainingData().size());
            graph.getTrainingData().forEach(assignment->{
                Map<String,Integer> cleanAssignment = handleAssignment(assignment,graph);
                distributions.forEach(distribution -> {
                    distribution.train(cleanAssignment);
                });
            });
            // set factors and normalize
            distributions.forEach(distribution -> {
                distribution.finish();
            });
            return null;
        });
    }

    protected Map<String,Integer> handleAssignment(Map<String,Integer> assignment, Graph graph) {
        return assignment;
    }

    @Override
    public Function<Graph, Double> computeCurrentScore() {
        return (graph -> 0d);
    }
}
