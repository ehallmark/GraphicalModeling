package graph;

import java.util.*;

/**
 * Created by Evan on 4/13/2017.
 */
public class Graph {
    private Map<String,Node> labelToNodeMap;
    private Set<FactorNode> factorNodes;
    private List<Node> allNodesList;

    public Graph() {
        this.labelToNodeMap=new HashMap<>();
        this.allNodesList=new ArrayList<>();
        this.factorNodes=new HashSet<>();
    }

    public Node addNode(String label) { // default binary
        return this.addNode(label,2);
    }

    public Node findNode(String label) {
        return labelToNodeMap.get(label);
    }

    public Node addNode(String label, int cardinality) {
        if(labelToNodeMap.containsKey(label)) throw new RuntimeException("Label already exists");
        Node node = new Node(label,cardinality);
        allNodesList.add(node);
        labelToNodeMap.put(label, node);
        return node;
    }

    public FactorNode addFactorNode(float[] weights, Node... nodes) {
        String[] connectingLabels = new String[nodes.length];
        int[] varCardinalities = new int[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            String label = nodes[i].getLabel();
            if(label==null) throw new RuntimeException("Unable to find node");
            connectingLabels[i] = label;
            varCardinalities[i] = node.cardinality;
        }
        FactorNode factor = new FactorNode(weights,connectingLabels,varCardinalities);
        Arrays.stream(nodes).forEach(node->{
            node.connectFactor(factor);
            factor.connectNode(node);
        });
        factorNodes.add(factor);
        return factor;
    }

    public Edge connectNodes(String label1, String label2) {
        return connectNodes(labelToNodeMap.get(label1),labelToNodeMap.get(label2));
    }

    public Edge connectNodes(Node node1, Node node2) {
        if(node1==null||node2==null) return null;
        Edge edge = node1.connectNode(node2);
        node2.connectNode(node1);
        return edge;
    }
}
