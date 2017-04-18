package graph;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Node {
    protected List<Edge> neighborhood;
    protected String label;

    protected Node(String label) {
        this.label=label;
        this.neighborhood=new ArrayList<>();
    }

    public Edge connect(Node otherNode) {
        Edge edge = new Edge(this,otherNode);
        int idx = neighborhood.indexOf(edge);
        if(idx>=0) return neighborhood.get(idx);
        else {
            neighborhood.add(edge);
            return edge;
        }
    }

    public String getLabel() { return label; }


    @Override
    public int hashCode() {
        return (label==null)? super.hashCode():label.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Node)) {
            return false;
        }

        return ((Node) other).getLabel().equals(label);
    }
}
