package graph;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Node {
    protected final List<Edge> neighborhood;
    protected final List<Edge> factors;
    protected final String label;
    protected final int cardinality;
    protected final Map<Edge,Integer> edgeIndexMap;
    protected final Map<Edge,Integer> factorEdgeIndexMap;

    protected Node(String label, int cardinality) {
        this.label=label;
        this.neighborhood=new ArrayList<>();
        this.cardinality=cardinality;
        this.edgeIndexMap=new HashMap<>();
        this.factors=new ArrayList<>();
        this.factorEdgeIndexMap = new HashMap<>();
    }

    public Edge connect(Node otherNode) {
        Map<Edge,Integer> indexMap;
        List<Edge> connections;
        if(otherNode instanceof FactorNode) {
            if(this instanceof FactorNode)throw new RuntimeException("Cannot directly connect to factor nodes.");
            indexMap=factorEdgeIndexMap;
            connections=factors;
        } else {
            indexMap=edgeIndexMap;
            connections=neighborhood;
        }
        Edge edge = new Edge(this, otherNode);
        if (indexMap.containsKey(edge)) {
            return edge;
        } else {
            synchronized (connections) {
                indexMap.put(edge, connections.size());
                connections.add(edge);
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
