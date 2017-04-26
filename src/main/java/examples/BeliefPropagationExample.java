package examples;

import model.functions.normalization.DivideByPartition;
import model.functions.normalization.SoftMax;
import model.graphs.BayesianNet;
import model.graphs.CliqueTree;
import model.graphs.MarkovNet;
import model.functions.heuristic.MinimalCliqueSizeHeuristic;
import model.nodes.Node;

import java.util.List;

/**
 * Created by ehallmark on 4/26/17.
 */
public class BeliefPropagationExample {
    public static void main(String[] args) {
        // Create Bayesian Network
        BayesianNet bayesianNet = new BayesianNet();

        // Add nodes
        Node n1 = bayesianNet.addNode("Node 1",2);
        Node n2 = bayesianNet.addNode("Node 2",2);
        Node n3 = bayesianNet.addNode("Node 3",2);
        Node n4 = bayesianNet.addNode("Node 4",2);
        Node n5 = bayesianNet.addBinaryNode("Node 5"); // Shorthand
        Node n6 = bayesianNet.addBinaryNode("Node 6"); // Shorthand

        // Connect nodes
        bayesianNet.connectNodes("Node 1","Node 2");
        bayesianNet.connectNodes("Node 2","Node 3");
        bayesianNet.connectNodes("Node 2","Node 4");
        bayesianNet.connectNodes("Node 3","Node 4");
        bayesianNet.connectNodes("Node 3","Node 5");
        bayesianNet.connectNodes(n6,n5); // Or by variable

        // Add factors
        bayesianNet.addFactorNode(new float[]{1,2,3,4},n1,n2);
        bayesianNet.addFactorNode(new float[]{5,6,7,8},n2,n3);
        bayesianNet.addFactorNode(new float[]{1,2,3,5},n2,n4);
        bayesianNet.addFactorNode(new float[]{7,11,3,5},n3,n4);
        bayesianNet.addFactorNode(new float[]{4,3,2,1},n3,n5);
        bayesianNet.addFactorNode(new float[]{6,3,12,9},n6,n5);

        // Moralize to a Markov Network
        MarkovNet markovNet = bayesianNet.moralize();

        // Triangulate with given heuristic
        markovNet.triangulateInPlace(new MinimalCliqueSizeHeuristic());

        // Find Perfect Elimination Ordering
        List<Node> PEO = markovNet.findPerfectEliminitationOrdering();

        // Create Clique Tree From Triangulated Graph
        CliqueTree cliqueTree = markovNet.createCliqueTree();

        // Re-Normalize Values to Probabilities
        cliqueTree.getFactorNodes().forEach(node->{
            node.reNormalize(new DivideByPartition()); // Could also use new SoftMax();
        });

        System.out.println("Clique Tree: "+cliqueTree.toString());
        /**
             Clique Tree: Num factors: 3
             Num nodes: 3
             Clique: 0
             Scope: [Node 3, Node 2, Node 4]
             Factor: [0.04310345, 0.094827585, 0.10344828, 0.21674877, 0.05541872, 0.12931034, 0.11083744, 0.24630542]
             Clique: 1
             Scope: [Node 3, Node 5, Node 6]
             Factor: [0.1904762, 0.14285715, 0.1904762, 0.0952381, 0.0952381, 0.071428575, 0.14285715, 0.071428575]
             Clique: 2
             Scope: [Node 1, Node 2]
             Factor: [0.1, 0.2, 0.3, 0.4]
         **/

        // Run Belief Propagation
        cliqueTree.runBeliefPropagation();

    }
}
