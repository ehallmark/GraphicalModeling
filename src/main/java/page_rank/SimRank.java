package page_rank;

import model.edges.Edge;
import model.graphs.Graph;
import model.learning_algorithms.LearningAlgorithm;
import model.nodes.Node;
import util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/20/17.
 */
public class SimRank extends RankGraph {
    private static final int jaccardDepth = 5;
    public SimRank(Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        super(labelToCitationLabelsMap, damping);
    }

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
        AtomicInteger cnt = new AtomicInteger(0);
        this.nodes.forEach(node->{
            System.out.println("Adding neighbors of: "+cnt.getAndIncrement());
           addNeighborsToMap(node,node,0,jaccardDepth);
        });
        System.out.println("Done.");
    }

    protected void addNeighborsToMap(Node thisNode, Node otherNode, int currentIdx, int maxIdx) {
        rankTable.put(new Pair<>(thisNode.getLabel(),otherNode.getLabel()).toString(),thisNode.getLabel().equals(otherNode.getLabel())?1f:0f);
        if(currentIdx<maxIdx) {
            thisNode.getNeighbors().forEach(neighbor->{
                addNeighborsToMap(thisNode,neighbor,currentIdx+1,maxIdx);
            });
        }
    }

    @Override
    public LearningAlgorithm getLearningAlgorithm() {
        return new Algorithm();
    }

    public List<Pair<String,Float>> findSimilarDocuments(Collection<String> nodeLabels, int limit) {
        return findSimilarDocumentsFromRankTable(rankTable,nodeLabels,limit);
    }

    public static List<Pair<String,Float>> findSimilarDocumentsFromRankTable(Map<String,Float> rankTable, Collection<String> nodeLabels, int limit) {
        // greedily iterate through all values and sum ranks over nodelabels
        Map<String,Float> scoreMap = new HashMap<>();
        rankTable.entrySet().stream().filter(e->{
            for(String doc: e.getKey().split(";")) {
                if(nodeLabels.contains(doc)) return true;
            }
            return false;
        }).forEach(e->{
            for(String doc: e.getKey().split(";")) {
                if(nodeLabels.contains(doc)) continue;
                Float score = scoreMap.get(doc);
                if (score == null) score = 0f;
                score += e.getValue();
                scoreMap.put(doc, score);
            }
        });
        return scoreMap.entrySet().stream().sorted((e1,e2)->e2.getValue().compareTo(e1.getValue())).limit(limit).map(e->new Pair<>(e.getKey(),e.getValue())).collect(Collectors.toList());
    }

    protected double rankValue(Node n1, Node n2) {
        if(n1.getInBound().size()==0||n2.getInBound().size()==0) return 0d;
        if(n1.equals(n2)) return 1d;
        return (damping / (n1.getInBound().size()*n2.getInBound().size())) *
                n1.getInBound().stream().collect(Collectors
                        .summingDouble(fam1->n2.getInBound().stream().collect(Collectors.summingDouble(fam2->{
                            Float famRank = rankTable.get(new Pair<>(fam1,fam2).toString());
                            if(famRank==null) return 0f;
                            else return famRank;
                        }))));
    }

    public class Algorithm implements LearningAlgorithm {
        @Override
        public Function<Graph, Graph> runAlgorithm() {
            return (graph) -> {
                AtomicInteger cnt = new AtomicInteger(0);
                Map<String,Float> rankTableCopy = new HashMap<>(rankTable);
                rankTableCopy.forEach((pair,oldRank)->{
                    String[] split = pair.split(";");
                    if(split.length!=2) throw new RuntimeException("Unknown error happened!");
                    Node n1 = graph.findNode(split[0]);
                    Node n2 = graph.findNode(split[1]);
                    double newRank = rankValue(n1,n2);
                    cnt.getAndIncrement();
                    if(newRank>0) {
                        System.out.println("Adding to TABLE: Point: "+cnt.get()+", Rank: "+newRank);
                        rankTable.put(new Pair<>(n1.getLabel(),n2.getLabel()).toString(),(float)newRank);
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

    public static void main(String[] args) throws Exception {
        Map<String,Collection<String>> test = new HashMap<>();
        test.put("n1",Arrays.asList("n2","n3"));
        test.put("n2",Arrays.asList("n1"));
        test.put("n3",Collections.emptyList());
        test.put("n4",Arrays.asList("n1","n2"));
        double damping = 0.75;
        SimRank pr = new SimRank(test,damping);
        //System.out.println("Similar to n4: "+String.join("; ",pr.findSimilarDocuments(Arrays.asList("n4"),3,4,2)));
    }
}
