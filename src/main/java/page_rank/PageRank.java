package page_rank;

import model.graphs.Graph;
import model.learning_algorithms.LearningAlgorithm;
import model.nodes.Node;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/21/17.
 */
public class PageRank extends RankGraph {
    public PageRank(Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        super(labelToCitationLabelsMap, damping);
    }

    @Override
    public LearningAlgorithm getLearningAlgorithm() {
        return new Algorithm();
    }

    protected double rankValue(Node node) {
        return (1d-damping)/nodes.size() + damping * node.getInBound().stream().collect(Collectors.summingDouble(neighbor->{
            Float rank = rankTable.get(neighbor.getLabel());
            if(rank==null)rank=0f;
            if(neighbor.getInBound().size()>0) {
                return (double)rank/neighbor.getInBound().size();
            } else return 0d;
        }));
    }

    @Override
    protected void initGraph(Map<String, ? extends Collection<String>> labelToCitationLabelsMap) {
        this.rankTable=new HashMap<>();
        System.out.println("Adding initial nodes...");
        labelToCitationLabelsMap.forEach((label,citations)->{
            graph.addBinaryNode(label);
            citations.forEach(citation->{
                graph.addBinaryNode(citation);
                graph.connectNodes(label, citation);
            });
        });
        this.nodes=graph.getAllNodesList();
        this.nodes.forEach(node->{
            rankTable.put(node.getLabel(),1f/nodes.size());
        });
        System.out.println("Done.");
    }


    public class Algorithm implements LearningAlgorithm {
        @Override
        public Function<Graph, Graph> runAlgorithm() {
            return (graph) -> {
                nodes.parallelStream().forEach(node -> {
                    double rank = rankValue(node);
                    if (rank > 0) {
                        rankTable.put(node.getLabel(), (float) rank);
                    }
                });
                return graph;
            };
        }

        @Override
        public Function<Graph, Double> computeCurrentScore() {
            return (graph)->0d;
        }
    }
}
