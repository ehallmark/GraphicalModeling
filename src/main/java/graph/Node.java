package graph;


import graph.edges.DirectedEdge;
import graph.edges.Edge;
import graph.edges.UndirectedEdge;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Node {
    @Getter
    protected final List<Node> neighbors;
    @Getter
    protected final List<Node> inBound;
    @Getter
    protected final List<Node> outBound;
    @Getter
    protected final List<FactorNode> factors;
    @Getter
    protected final String label;
    @Getter
    protected final int cardinality;
    @Getter @Setter
    protected float[] weights;
    protected final Map<Edge,Integer> edgeIndexMap;
    protected final Map<Edge,Integer> factorEdgeIndexMap;
    @Getter
    protected boolean directed;

    public Node(String label, int cardinality, boolean directed) {
        this.label=label;
        this.neighbors=new ArrayList<>();
        this.cardinality=cardinality;
        this.edgeIndexMap=new HashMap<>();
        this.factors=new ArrayList<>();
        this.factorEdgeIndexMap = new HashMap<>();
        this.outBound=new ArrayList<>();
        this.inBound=new ArrayList<>();
        this.directed=directed;
    }

    public Edge connectNode(Node otherNode) {
        Edge edge;
        if(directed) {
            edge = new DirectedEdge(this,otherNode);
        } else {
            edge = new UndirectedEdge(this,otherNode);
        }
        if (edgeIndexMap.containsKey(edge)) {
            return edge;
        } else {
            synchronized (neighbors) {
                edgeIndexMap.put(edge, neighbors.size());
                neighbors.add(otherNode);
            }
            if(directed) {
                outBound.add(otherNode);
                if(!otherNode.inBound.contains(this))otherNode.inBound.add(this);
            }
            return edge;
        }
    }

    public Edge connectFactor(FactorNode otherFactor) {
        Edge edge = new UndirectedEdge(this, otherFactor);
        if (factorEdgeIndexMap.containsKey(edge)) {
            return edge;
        } else {
            synchronized (neighbors) {
                factorEdgeIndexMap.put(edge, factors.size());
                factors.add(otherFactor);

            }
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

        String otherLabel = ((Node) other).getLabel();

        if(otherLabel==null) return super.equals(other);

        return otherLabel.equals(label);
    }
}
