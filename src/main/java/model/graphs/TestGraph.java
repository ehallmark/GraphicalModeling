package model.graphs;

import model.edges.Edge;
import model.nodes.FactorNode;
import model.nodes.Node;
import util.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Evan on 4/16/2017.
 */
public class TestGraph {
    public static void test2() {
        MarkovNet graph = new MarkovNet();
        Node n1 = graph.addNode("Node 1",2);
        Node n2 = graph.addNode("Node 2",2);
        Node n3 = graph.addNode("Node 3",3);
        graph.connectNodes("Node 1","Node 2");
        graph.connectNodes("Node 3","Node 1");
        //graph.connectNodes("Node 2","Node 3");

        graph.addFactorNode(new float[]{1,2,2,4},n1,n2);
        graph.addFactorNode(new float[]{7,8,9,10,11,12},n2,n3);

        graph.setCurrentAssignment( Arrays.asList(new Pair<>("Node 1",1)));
        FactorNode ve = graph.variableEliminationDefault(new String[]{"Node 3","Node 2"});

        graph.triangulateInPlace(()->(List<Node> list)->0);

        CliqueTree tree = graph.createCliqueTree();
        System.out.println("Tree: "+tree.allNodesList.size());

        System.out.println(ve.toString());


    }

    public static void main(String[] args) throws Exception {
        test2();

        Graph graph = new BayesianNet();
        Node n1 = graph.addNode("Node 1",2);
        Node n2 = graph.addNode("Node 2",3);
        Node n3 = graph.addNode("Node 3",10);

        graph=((BayesianNet)graph).moralize();

        graph.connectNodes("Node 1","Node 2");
        graph.connectNodes(n2,n3);


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

        if((new Node(null,10) {}).equals(new Node(null,10 ) {})) {
            System.out.println("Error!");
        } else {
            System.out.println("PASSED");
        }

        graph.addFactorNode(new float[]{1,2,3,4,5,6},n1,n2);

    }
}
