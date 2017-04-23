package graph.edges;

import graph.Node;
import lombok.Getter;
import util.Pair;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by Evan on 4/23/2017.
 */
public class DirectedEdge extends Edge {
    protected Node node1;
    protected Node node2;

    public DirectedEdge(Node node1, Node node2) {
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

        return false;
    }

    @Override
    public int hashCode() {
         return Objects.hash(node1,node2);
    }

}
