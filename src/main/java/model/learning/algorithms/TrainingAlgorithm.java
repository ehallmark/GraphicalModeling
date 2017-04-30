package model.learning.algorithms;

import model.graphs.Graph;
import model.learning.distributions.Distribution;
import model.learning.distributions.DistributionCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Evan on 4/29/2017.
 */
public class TrainingAlgorithm implements LearningAlgorithm {
    protected DistributionCreator creator;
    protected List<Distribution> distributions;
    protected int batchSize;
    public TrainingAlgorithm(DistributionCreator creator, int batchSize) {
        this.creator=creator;
        this.batchSize=batchSize;
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
            System.out.println("Num Assignments: "+graph.getAssignments().size());
            graph.getAssignments().forEach(assignment->{
                distributions.forEach(distribution -> {
                    distribution.train(assignment,batchSize);
                });
            });
            return null;
        });
    }

    @Override
    public Function<Graph, Double> computeCurrentScore() {
        return (graph -> 0d);
    }
}
