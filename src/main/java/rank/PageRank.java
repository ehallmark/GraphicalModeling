package rank;

import graph.Node;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.Pair;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ehallmark on 4/21/17.
 */
public class PageRank extends RankGraph<PageRank> {
    protected PageRank(File file, Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        super(file, labelToCitationLabelsMap, damping);
    }

    protected double rankValue(Node node) {
        throw new NotImplementedException();
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


}
