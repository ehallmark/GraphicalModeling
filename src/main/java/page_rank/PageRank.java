package page_rank;

import graph.Graph;
import graph.Node;
import lombok.Setter;
import util.FloatPair;
import util.Pair;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/20/17.
 */
public class PageRank {
    protected Map<String,? extends Collection<String>> labelToCitationLabelsMap;
    protected Graph graph;
    protected Set<Node> nodes;
    protected double damping;
    protected int parallelism;
    public PageRank(Map<String,? extends Collection<String>> labelToCitationLabelsMap, double damping, int parallelism) {
        if(damping<0||damping>1) throw new RuntimeException("Illegal damping constant");
        this.labelToCitationLabelsMap=labelToCitationLabelsMap;
        this.graph=new Graph();
        this.parallelism=parallelism;
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
            node.setWeights(new float[]{1f/nodes.size()});
        });
    }

    public List<Pair<String,Float>> findSimilarDocuments(Collection<String> nodeLabels, int topN, int numEpochs, int depthOfSearch) {
        resetWeights();

        List<Node> nodeList = nodeLabels.stream().map(label->graph.findNode(label)).filter(n->n!=null).collect(Collectors.toList());
        if(nodeList.isEmpty()) return Collections.emptyList();

        ForkJoinPool pool = new ForkJoinPool(Math.max(1,parallelism));
        for(int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Starting epoch ["+(epoch+1)+"]");
            nodeList.forEach(node->pool.execute(new RecursiveAction() {
                @Override
                protected void compute() {
                    findSimilarDocumentsHelper(node,0,depthOfSearch,pool);
                }
            }));
        }


        // collect results
        return nodes.stream().filter(n->n.getWeights()[0]>1f/nodes.size()).sorted((n1,n2)->Float.compare(n2.getWeights()[0],n1.getWeights()[0])).limit(topN).map(n->new Pair<>(n.getLabel(),n.getWeights()[0])).collect(Collectors.toList());
    }

    private void findSimilarDocumentsHelper(Node node, final int currentDepth,final int maxDepth, ForkJoinPool pool) {
        double distanceDiscount = Math.pow(0.5,currentDepth);
        runPageRankOnSingleNode(node,distanceDiscount);
        if(currentDepth<maxDepth) node.getNeighbors().forEach(neighbor->pool.execute(new RecursiveAction() {
            @Override
            protected void compute() {
                findSimilarDocumentsHelper(neighbor,currentDepth+1,maxDepth,pool);
            }
        }));
    }

    private void runPageRankOnSingleNode(Node node, double distanceDiscount) {
        double pr = (1d-damping)/nodes.size() + (damping * node.getNeighbors().stream().collect(Collectors.summingDouble(neighbor->(double)(neighbor.getWeights()[0]/neighbor.getNeighbors().size()))));
        node.setWeights(new float[]{(float)(pr*distanceDiscount)});
    }

    public static void main(String[] args) throws Exception {
        Map<String,Collection<String>> test = new HashMap<>();
        test.put("n1",Arrays.asList("n2","n3"));
        test.put("n2",Arrays.asList("n1"));
        test.put("n3",Collections.emptyList());
        test.put("n4",Arrays.asList("n1","n2"));
        double damping = 0.85;
        int parallelism = 4;
        PageRank pr = new PageRank(test,damping,parallelism);
        //System.out.println("Similar to n4: "+String.join("; ",pr.findSimilarDocuments(Arrays.asList("n4"),3,4,2)));
    }
}
