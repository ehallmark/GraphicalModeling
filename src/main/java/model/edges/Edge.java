package model.edges;

import model.nodes.Node;

/**
 * Created by Evan on 4/23/2017.
 */
public abstract class Edge {
    protected Node node1;
    protected Node node2;
    protected Edge(Node node1, Node node2) {
        this.node1=node1;
        this.node2=node2;
    }

    public abstract boolean equals(Object other);
    public abstract int hashCode();

    public Node getNode1() {return node1; }
    public Node getNode2() { return node2;}

}
