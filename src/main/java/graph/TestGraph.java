package graph;

import graph.Edge;
import graph.Graph;
import graph.Node;

/**
 * Created by Evan on 4/16/2017.
 */
public class TestGraph {
    public static void main(String[] args) throws Exception {
        Graph graph = new Graph();
        Node n1 = graph.addNode("Node 1",2);
        Node n2 = graph.addNode("Node 2",3);
        Node n3 = graph.addNode("Node 3",10);

        Edge e12 = graph.connectNodes("Node 1","Node 2",false);
        Edge e23 = graph.connectNodes(n2,n3,false);

        if(!e12.equals(graph.connectNodes("Node 1","Node 2",true))) {
            System.out.println("Error!");
        } else {
            System.out.println("PASSED");
        }

        if(e12.equals(e23)) {
            System.out.println("Error!");
        } else {
            System.out.println("PASSED");
        }

        if(n1.equals(n2)) {
            System.out.println("Error!");
        } else {
            System.out.println("PASSED");
        }

        if(!n1.equals(new Node("Node 1",10) {})) {
            System.out.println("Error!");
        } else {
            System.out.println("PASSED");
        }

        if((new Node(null,10) {}).equals(new Node(null,10) {})) {
            System.out.println("Error!");
        } else {
            System.out.println("PASSED");
        }

        graph.addFactorNode(new float[]{1,2,3,4,5,6},n1,n2);
    }
}
