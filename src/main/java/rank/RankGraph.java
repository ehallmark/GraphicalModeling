package rank;

import graph.Graph;
import graph.Node;
import util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/21/17.
 */
public abstract class RankGraph<T> implements Serializable {
    private static final long serialVersionUID = 1l;
    protected transient Graph graph;
    protected transient Set<Node> nodes;
    protected double damping;
    protected Map<String,Float> rankTable;

    protected File file;
    protected RankGraph(File file, Map<String, ? extends Collection<String>> labelToCitationLabelsMap, double damping) {
        if(damping<0||damping>1) throw new RuntimeException("Illegal damping constant");
        this.graph=new Graph();
        this.damping=damping;
        this.nodes = new HashSet<>(labelToCitationLabelsMap.size());
        this.init(labelToCitationLabelsMap);
        this.file=file;
    }

    protected abstract void init(Map<String, ? extends Collection<String>> labelToCitationLabelsMap);

    public void save() {
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
            return null;
        }
    }

    public abstract void solve(int numEpochs);

}
