package page_rank;

import graph.Graph;
import graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/20/17.
 */
public class PageRank {
    private Map<String,? extends Collection<String>> labelToCitationLabelsMap;
    private Graph graph;
    private Set<Node> nodes;
    private double damping;
    public PageRank(Map<String,? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        if(damping<0||damping>1) throw new RuntimeException("Illegal damping constant");
        this.labelToCitationLabelsMap=labelToCitationLabelsMap;
        this.graph=new Graph();
        this.damping=damping;
        this.nodes = new HashSet<>(labelToCitationLabelsMap.size());
        this.init();
    }

    public void init() {
        labelToCitationLabelsMap.keySet().forEach(label->{
            nodes.add(graph.addNode(label));
        });
        labelToCitationLabelsMap.forEach((label,citations)->{
            citations.forEach(citation->{
                graph.connectNodes(label, citation);
            });
        });
        if(nodes.size()!=labelToCitationLabelsMap.size()) throw new RuntimeException("Error constructing graph!");
    }

    public void resetWeights() {
        nodes.forEach(node->{
            node.setWeights(new float[]{0f});
        });
    }

    public List<String> findSimilarDocuments(String nodeLabel, int topN, int numEpochs, int breadthOfSearch) {
        resetWeights();

        Node node = graph.findNode(nodeLabel);

        if(node==null) return Collections.emptyList();
        for(int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Starting epoch ["+(epoch+1)+"]");
            findSimilarDocumentsHelper(node,0,breadthOfSearch);
        }

        // collect results
        return nodes.stream().sorted((n1,n2)->Float.compare(n2.getWeights()[0],n1.getWeights()[0])).map(n->n.getLabel()).limit(topN).collect(Collectors.toList());
    }

    private void findSimilarDocumentsHelper(Node node, final int currentBreadth,final int maxBreadth) {
        if(currentBreadth>maxBreadth) return;
        runPageRankOnSingleNode(node);
        node.getNeighbors().forEach(neighbor->findSimilarDocumentsHelper(neighbor,currentBreadth+1,maxBreadth));
    }

    private void runPageRankOnSingleNode(Node node) {
        double weight = (1d-damping) + (damping * node.getNeighbors().stream().collect(Collectors.summingDouble(neighbor->(double)(neighbor.getWeights()[0]))));
        node.setWeights(new float[]{(float)weight});
    }

    public static void main(String[] args) throws Exception {
        Map<String,Collection<String>> test = new HashMap<>();
        test.put("n1",Arrays.asList("n2","n3"));
        test.put("n2",Arrays.asList("n1"));
        test.put("n3",Collections.emptyList());
        test.put("n4",Arrays.asList("n1","n2"));
        double damping = 0.85;
        PageRank pr = new PageRank(test,damping);
        System.out.println("Similar to n4: "+String.join("; ",pr.findSimilarDocuments("n4",3,4,2)));
    }
}
