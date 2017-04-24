package page_rank;

import model.graphs.BayesianNet;
import model.graphs.Graph;
import model.nodes.Node;
import util.ObjectIO;

import java.io.*;
import java.util.*;

/**
 * Created by ehallmark on 4/21/17.
 */
public abstract class RankGraph {
    private static final long serialVersionUID = 1l;
    protected transient Graph graph;
    protected transient Set<Node> nodes;
    protected double damping;
    protected Map<String,Float> rankTable;

    protected RankGraph(Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        if(damping<0||damping>1) throw new RuntimeException("Illegal damping constant");
        this.graph=new BayesianNet();
        this.damping=damping;
        this.nodes = new HashSet<>(labelToCitationLabelsMap.size());
        this.init(labelToCitationLabelsMap);
    }

    protected abstract void init(Map<String, ? extends Collection<String>> labelToCitationLabelsMap);



    public abstract void solve(int numEpochs);

    public void save(File file) {
        new ObjectIO().save(file, rankTable);
    }

    public static Map<String,Float> loadRankTable(File file) {
        return new ObjectIO<Map<String,Float>>().load(file);
    }
}
