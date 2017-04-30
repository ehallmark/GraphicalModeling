package examples;

import model.functions.normalization.DivideByPartition;
import model.functions.normalization.SoftMax;
import model.graphs.BayesianNet;
import model.graphs.CliqueTree;
import model.graphs.MarkovNet;
import model.functions.heuristic.MinimalCliqueSizeHeuristic;
import model.learning.algorithms.TrainingAlgorithm;
import model.learning.distributions.Dirichlet;
import model.learning.distributions.DirichletCreator;
import model.nodes.Node;

import java.util.*;

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


        /*
        // Add factors
        bayesianNet.addFactorNode(new float[]{1,2,3,4},n1,n2);
        bayesianNet.addFactorNode(new float[]{5,6,7,8},n2,n3);
        bayesianNet.addFactorNode(new float[]{1,2,3,5},n2,n4);
        bayesianNet.addFactorNode(new float[]{7,11,3,5},n3,n4);
        bayesianNet.addFactorNode(new float[]{4,3,2,1},n3,n5);
        bayesianNet.addFactorNode(new float[]{6,3,12,9},n6,n5);
        */

        bayesianNet.addFactorNode(null,n1,n2);
        bayesianNet.addFactorNode(null,n2,n3);
        bayesianNet.addFactorNode(null,n2,n4);
        bayesianNet.addFactorNode(null,n3,n4);
        bayesianNet.addFactorNode(null,n3,n5);
        bayesianNet.addFactorNode(null,n6,n5);
        bayesianNet.addFactorNode(null,n1);

        Random rand = new Random(69);
        List<Map<String,int[]>> assignments = new ArrayList<>();
        Map<String,int[]> assignment = new HashMap<>();
        for(int i = 0; i < 1000; i++) {
            if(i%10!=0) {
                for(Node node : bayesianNet.getAllNodesList()) {
                    assignment.get(node.getLabel())[(i%10)-1]=rand.nextInt(node.getCardinality());
                }
            } else {
                if(assignment.size()>0)assignments.add(assignment);
                assignment=new HashMap<>();
                for(Node node : bayesianNet.getAllNodesList()) {
                    assignment.put(node.getLabel(),new int[9]);
                }
            }

        }

        bayesianNet.setTrainingData(assignments);

        bayesianNet.applyLearningAlgorithm(new TrainingAlgorithm(new DirichletCreator(50f),9),1);

        // Moralize to a Markov Network
        MarkovNet markovNet = bayesianNet.moralize();

        // Triangulate with given heuristic
        markovNet.triangulateInPlace(new MinimalCliqueSizeHeuristic());

        // Create Clique Tree From Triangulated Graph
        CliqueTree cliqueTree = markovNet.createCliqueTree();

        // Re-Normalize Values to Probabilities
        cliqueTree.getFactorNodes().forEach(node->{
            node.reNormalize(new DivideByPartition()); // Could also use new SoftMax();
        });

        System.out.println("Clique Tree: "+cliqueTree.toString());

        // Run Belief Propagation
        cliqueTree.runBeliefPropagation();

        // Re-Normalize Values to Probabilities
        cliqueTree.reNormalize(new DivideByPartition());

        System.out.println("Clique Tree (after BP): "+cliqueTree.toString());

    }
}
