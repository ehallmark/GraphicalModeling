package model.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ehallmark on 4/25/17.
 */
public class CliqueNode extends Node {
    protected Collection<Node> nodes;

    public CliqueNode(Collection<Node> nodes) {
        super(null, nodes.size());
        this.nodes=nodes;
    }

    public CliqueNode() {
        this(new ArrayList<>());
    }

    public void addNode(Node node) {
        if(!this.nodes.contains(node))this.nodes.add(node);
        cardinality=this.nodes.size();
    }

    public int size() {
        return cardinality;
    }
}
