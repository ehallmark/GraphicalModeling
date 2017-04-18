package graph;

import java.util.Objects;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Edge {

    private Node node1;
    private Node node2;
    protected Edge(Node node1, Node node2) {
        this.node1=node1;
        this.node2=node2;
    }

    public Node getNode1() {return node1; }
    public Node getNode2() { return node2;}

    // Checks whether connects the same pair of nodes
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Edge)) {
            return false;
        }

        Edge otherEdge = (Edge)other;

        Node otherNode1 = otherEdge.getNode1();
        Node otherNode2 = otherEdge.getNode2();

        if(node1.equals(otherNode1)&&node2.equals(otherNode2)) {
            return true;
        }

        if(node2.equals(otherNode1)&&node1.equals(otherNode2)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1,node2);
    }
}
