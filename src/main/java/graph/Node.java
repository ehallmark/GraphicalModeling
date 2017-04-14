package graph;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Node<T> {
    private List<Edge> neighborhood;
    private String label;
    private T data;

    protected Node(String label, T data) {
        this.data=data;
        this.label=label;
        this.neighborhood=new ArrayList<>();
    }

    public Edge connect(Node<T> otherNode) {
        Edge edge = new Edge(this,otherNode);
        int idx = neighborhood.indexOf(edge);
        if(idx>=0) return neighborhood.get(idx);
        else {
            neighborhood.add(edge);
            return edge;
        }
    }

    public String getLabel() { return label; }

    public T getData() { return data; }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Node)) {
            return false;
        }

        return ((Node) other).getLabel().equals(label);
    }
}
