package test;

import graph.Edge;
import graph.Graph;
import graph.Node;

/**
 * Created by Evan on 4/16/2017.
 */
public class TestGraph {
    public static void main(String[] args) throws Exception {
        Graph graph = new Graph();
        Node n1 = graph.addNode("Node 1");
        Node n2 = graph.addNode("Node 2");
        Node n3 = graph.addNode("Node 3");

        Edge e12 = graph.connectNodes("Node 1","Node 2");
        Edge e23 = graph.connectNodes(n2,n3);

    }
}
