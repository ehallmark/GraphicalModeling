package rank;

import graph.Graph;
import graph.Node;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ehallmark on 4/21/17.
 */
public abstract class RankGraph<T> implements Serializable {
    private static final long serialVersionUID = 1l;
    protected Map<String,? extends Collection<String>> labelToCitationLabelsMap;
    protected Graph graph;
    protected Set<Node> nodes;
    protected double damping;
    protected int parallelism;
    
    protected File file;
    protected RankGraph(File file, Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping, int parallelism) {
        if(damping<0||damping>1) throw new RuntimeException("Illegal damping constant");
        this.labelToCitationLabelsMap=labelToCitationLabelsMap;
        this.graph=new Graph();
        this.parallelism=parallelism;
        this.damping=damping;
        this.nodes = new HashSet<>(labelToCitationLabelsMap.size());
        this.init();) {
        this.file=file;
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

    public abstract void solve();

    protected void save() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            oos.writeObject(this);
            oos.flush();
            oos.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public T load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
            return (T) ois.readObject();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
