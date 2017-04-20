package graph;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Node {
    @Getter
    protected final List<Node> neighbors;
    @Getter
    protected final List<FactorNode> factors;
    @Getter
    protected final String label;
    protected final int cardinality;
    @Getter @Setter
    protected float[] weights;
    protected final Map<Edge,Integer> edgeIndexMap;
    protected final Map<Edge,Integer> factorEdgeIndexMap;

    protected Node(String label, int cardinality) {
        this.label=label;
        this.neighbors=new ArrayList<>();
        this.cardinality=cardinality;
        this.edgeIndexMap=new HashMap<>();
        this.factors=new ArrayList<>();
        this.factorEdgeIndexMap = new HashMap<>();
    }


    public Edge connectNode(Node otherNode) {
        Edge edge = new Edge(this, otherNode);
        if (edgeIndexMap.containsKey(edge)) {
            return edge;
        } else {
            synchronized (neighbors) {
                edgeIndexMap.put(edge, neighbors.size());
                neighbors.add(otherNode);

            }
            return edge;
        }
    }

    public Edge connectFactor(FactorNode otherFactor) {
        Edge edge = new Edge(this, otherFactor);
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
