package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Evan on 4/13/2017.
 */
public class Graph<T> {
    private Map<String,Node<T>> labelToNodeMap;
    private List<Node<T>> allNodesList;

    public Graph() {
        this.labelToNodeMap=new HashMap<>();
        this.allNodesList=new ArrayList<>();
    }

    public Node<T> addNode(String label, T data) {
        if(labelToNodeMap.containsKey(label)) throw new RuntimeException("Label already exists");
        Node<T> node = new Node<>(label,data);
        allNodesList.add(node);
        labelToNodeMap.put(label, node);
        return node;
    }

    public Edge connectNodes(String label1, String label2) {
        Node<T> node1 = labelToNodeMap.get(label1);
        Node<T> node2 = labelToNodeMap.get(label2);
        return connectNodes(node1,node2);
    }

    public Edge connectNodes(Node<T> node1, Node<T> node2) {
        if(node1==null||node2==null) return null;
        Edge edge = node1.connect(node2);
        return edge;
    }
}
