package rank;

import graph.Graph;
import graph.Node;
import util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/20/17.
 */
public class SimRank extends RankGraph<SimRank> {

    protected SimRank(File file, Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        super(file, labelToCitationLabelsMap, damping);
    }

    protected void init(Map<String, ? extends Collection<String>> labelToCitationLabelsMap) {
        this.rankTable=new HashMap<>();
        labelToCitationLabelsMap.keySet().forEach(label->{
            nodes.add(graph.addNode(label));
        });
        labelToCitationLabelsMap.forEach((label,citations)->{
            rankTable.put(new Pair<>(label,label).toString(),1f);
            citations.forEach(citation->{
                graph.connectNodes(label, citation, true);
                rankTable.put(new Pair<>(label,citation).toString(),0f);
            });
        });
        if(nodes.size()!=labelToCitationLabelsMap.size()) throw new RuntimeException("Error constructing graph!");
    }

    public List<Pair<String,Float>> findSimilarDocuments(Collection<String> nodeLabels, int limit) {
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

    @Override
    public void solve(int numEpochs) {
        for(int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Starting epoch ["+(epoch+1)+"]");
            nodes.parallelStream().forEach(n1->nodes.forEach(n2->{
                double rank = rankValue(n1,n2);
                if(rank>0) {
                    rankTable.put(new Pair<>(n1.getLabel(),n2.getLabel()).toString(),(float)rank);
                }
            }));
        }
    }

    protected double rankValue(Node n1, Node n2) {
        return (damping / (n1.getNeighbors().size()*n2.getNeighbors().size())) *
                n1.getNeighbors().stream().collect(Collectors
                        .summingDouble(fam1->n2.getNeighbors().stream().collect(Collectors.summingDouble(fam2->{
                            Float famRank = rankTable.get(new Pair<>(fam1,fam2).toString());
                            if(famRank==null) return 0f;
                            else return famRank;
                        }))));
    }


    public static void main(String[] args) throws Exception {
        Map<String,Collection<String>> test = new HashMap<>();
        test.put("n1",Arrays.asList("n2","n3"));
        test.put("n2",Arrays.asList("n1"));
        test.put("n3",Collections.emptyList());
        test.put("n4",Arrays.asList("n1","n2"));
        double damping = 0.75;
        SimRank pr = new SimRank(new File("test_file.jobj"),test,damping);
        //System.out.println("Similar to n4: "+String.join("; ",pr.findSimilarDocuments(Arrays.asList("n4"),3,4,2)));
    }
}
