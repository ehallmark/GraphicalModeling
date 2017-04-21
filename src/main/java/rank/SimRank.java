package rank;

import graph.Graph;
import graph.Node;
import util.Pair;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/20/17.
 */
public class SimRank extends RankGraph<SimRank> {
    protected Map<String,? extends Collection<String>> labelToCitationLabelsMap;
    protected Graph graph;
    protected Set<Node> nodes;
    protected double damping;
    protected int parallelism;
    public SimRank(Map<String,? extends Collection<String>> labelToCitationLabelsMap, double damping, int parallelism) {
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

    @Override
    public void solve() {
        init();
        
    }

    public void resetWeights(Collection<String> actuals) {
        nodes.forEach(node->{
            node.setWeights(new float[]{actuals.contains(node.getLabel())?1f:0f});
        });
    }

    public List<Pair<String,Float>> findSimilarDocuments(Collection<String> nodeLabels, int topN, int numEpochs, int depthOfSearch, long timeoutSeconds) {
        resetWeights(nodeLabels);

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

        pool.shutdown();
        try {
            pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
        } catch(Exception e) {
            System.out.println("WARNING: Did not terminate!");
            return null;
        }

        // collect results
        return nodes.stream().filter(n->n.getWeights()[0]>1f/nodes.size()).sorted((n1,n2)->Float.compare(n2.getWeights()[0],n1.getWeights()[0])).limit(topN).map(n->new Pair<>(n.getLabel(),n.getWeights()[0])).collect(Collectors.toList());
    }

    private void findSimilarDocumentsHelper(Node node, final Node targetNode, final int currentDepth,final int maxDepth, ForkJoinPool pool) {
        runSimRankOnSingleNode(node,targetNode);
        if(currentDepth<maxDepth) node.getNeighbors().forEach(neighbor->pool.execute(new RecursiveAction() {
            @Override
            protected void compute() {
                findSimilarDocumentsHelper(neighbor,targetNode,currentDepth+1,maxDepth,pool);
            }
        }));
    }

    private void runSimRankOnSingleNode(Node node, Set<Node> targetNodes) {
        if(targetNodes.contains(node)) return;


        double pr = (damping / (node.getNeighbors().size()*targetNode.getNeighbors().size())) *
                node.getNeighbors().stream().collect(Collectors
                        .summingDouble(n1->targetNode.getNeighbors().stream().collect(Collectors.summingDouble(n2->)));
        node.setWeights(new float[]{(float)(pr*distanceDiscount)});
    }

    public static void main(String[] args) throws Exception {
        Map<String,Collection<String>> test = new HashMap<>();
        test.put("n1",Arrays.asList("n2","n3"));
        test.put("n2",Arrays.asList("n1"));
        test.put("n3",Collections.emptyList());
        test.put("n4",Arrays.asList("n1","n2"));
        double damping = 0.75;
        int parallelism = 4;
        SimRank pr = new SimRank(test,damping,parallelism);
        //System.out.println("Similar to n4: "+String.join("; ",pr.findSimilarDocuments(Arrays.asList("n4"),3,4,2)));
    }
}
