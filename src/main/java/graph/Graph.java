package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Evan on 4/13/2017.
 */
public class Graph {
    private Map<String,Node> labelToNodeMap;
    private List<Node> allNodesList;

    public Graph() {
        this.labelToNodeMap=new HashMap<>();
        this.allNodesList=new ArrayList<>();
    }

    public Node addNode(String label) {
        if(labelToNodeMap.containsKey(label)) throw new RuntimeException("Label already exists");
        Node node = new Node(label);
        allNodesList.add(node);
        labelToNodeMap.put(label, node);
        return node;
    }

    public Edge connectNodes(String label1, String label2) {
        Node node1 = labelToNodeMap.get(label1);
        Node node2 = labelToNodeMap.get(label2);
        return connectNodes(node1,node2);
    }

    public Edge connectNodes(Node node1, Node node2) {
        if(node1==null||node2==null) return null;
        Edge edge = node1.connect(node2);
        return edge;
    }
}
