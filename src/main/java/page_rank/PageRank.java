package page_rank;

import model.nodes.Node;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/21/17.
 */
public class PageRank extends RankGraph {
    public PageRank(Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        super(labelToCitationLabelsMap, damping);
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

    public void solve(int numEpochs) {
        for(int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Starting epoch ["+(epoch+1)+"]");
            nodes.parallelStream().forEach(node->{
                double rank = rankValue(node);
                if(rank>0) {
                    rankTable.put(node.getLabel(),(float)rank);
                }
            });
        }
    }

    @Override
    protected void init(Map<String, ? extends Collection<String>> labelToCitationLabelsMap) {
        this.rankTable=new HashMap<>();
        labelToCitationLabelsMap.keySet().forEach(label->{
            nodes.add(graph.addBinaryNode(label));
        });
        labelToCitationLabelsMap.forEach((label,citations)->{
            rankTable.put(label,1f/nodes.size());
            citations.forEach(citation->{
                graph.connectNodes(label, citation);
            });
        });
        if(nodes.size()!=labelToCitationLabelsMap.size()) throw new RuntimeException("Error constructing graph!");
    }


}
