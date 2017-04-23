package graph.edges;

import graph.Node;

import java.util.Objects;

/**
 * Created by ehallmark on 4/13/17.
 */
public class UndirectedEdge extends Edge {

    public UndirectedEdge(Node node1, Node node2) {
        super(node1,node2);
    }

    // Checks whether connects the same pair of nodes
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof UndirectedEdge)) {
            return false;
        }

        UndirectedEdge otherEdge = (UndirectedEdge)other;

        Node otherNode1 = otherEdge.getNode1();
        Node otherNode2 = otherEdge.getNode2();

        if(node1.equals(otherNode1)&&node2.equals(otherNode2)) {
            return true;
        }

        if (node2.equals(otherNode1) && node1.equals(otherNode2)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Math.min(Objects.hash(node1,node2),Objects.hash(node2,node1));
    }
}
