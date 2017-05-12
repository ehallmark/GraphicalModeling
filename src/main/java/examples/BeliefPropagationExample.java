package examples;

import model.functions.normalization.DivideByPartition;
import model.graphs.BayesianNet;
import model.graphs.CliqueTree;
import model.graphs.GibbsChain;
import model.graphs.MarkovNet;
import model.functions.heuristic.MinimalCliqueSizeHeuristic;
import model.learning.algorithms.BayesianLearningAlgorithm;
import model.learning.algorithms.ExpectationMaximizationAlgorithm;
import model.learning.algorithms.MarkovLearningAlgorithm;
import model.learning.distributions.DirichletCreator;
import model.nodes.FactorNode;
import model.nodes.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/26/17.
 */
public class BeliefPropagationExample {
    public static void main(String[] args) {
        // Create Bayesian Network
        BayesianNet bayesianNet = new BayesianNet();

        // Add nodes
        Node n1 = bayesianNet.addBinaryNode("Node 1");
        Node n2 = bayesianNet.addBinaryNode("Node 2");
        Node n3 = bayesianNet.addBinaryNode("Node 3");
        Node n4 = bayesianNet.addBinaryNode("Node 4");
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
        // Add known factors
        bayesianNet.addFactorNode(new float[]{1,2,3,4},n1,n2);
        bayesianNet.addFactorNode(new float[]{5,6,7,8},n2,n3);
        bayesianNet.addFactorNode(new float[]{1,2,3,5},n2,n4);
        bayesianNet.addFactorNode(new float[]{7,11,3,5},n3,n4);
        bayesianNet.addFactorNode(new float[]{4,3,2,1},n3,n5);
        bayesianNet.addFactorNode(new float[]{6,3,12,9},n6,n5);
        */

        // Add factors to learn
        bayesianNet.addFactorNode(null,n1,n2);
        bayesianNet.addFactorNode(null,n2,n3);
        bayesianNet.addFactorNode(null,n2,n4);
        bayesianNet.addFactorNode(null,n3,n4);
        bayesianNet.addFactorNode(null,n3,n5);
        bayesianNet.addFactorNode(null,n6,n5);
        bayesianNet.addFactorNode(null,n1);

        Random rand = new Random(69);
        List<Map<String,Integer>> assignments = new ArrayList<>();
        Map<String,Integer> assignment = new HashMap<>();
        for(int i = 0; i < 1000; i++) {
            if(i%10!=0) {
                for(Node node : bayesianNet.getAllNodesList()) {
                    // randomly don't include some
                   // if(rand.nextBoolean()&&rand.nextBoolean()) {
                        assignment.put(node.getLabel(),rand.nextInt(node.getCardinality()));
                   // }
                }
            } else {
                if(assignment.size()>0)assignments.add(assignment);
                assignment=new HashMap<>();
            }

        }

        bayesianNet.setTrainingData(assignments);

        // Moralize to a Markov Network
        MarkovNet markovNet = bayesianNet.moralize();
        markovNet.setTrainingData(assignments);

        markovNet.applyLearningAlgorithm(new BayesianLearningAlgorithm(new DirichletCreator(1)),1000);

        // Triangulate with given heuristic
        markovNet.triangulateInPlace(new MinimalCliqueSizeHeuristic());

        // Create Clique Tree From Triangulated Graph
        CliqueTree cliqueTree = markovNet.createCliqueTree();

        // Re-Normalize Values to Probabilities
        cliqueTree.getFactorNodes().forEach(node->{
            node.reNormalize(new DivideByPartition()); // Could also use new SoftMax();
        });

        System.out.println("Clique Tree: "+cliqueTree.toString());

        // Add assignment
        Map<String,Integer> test = new HashMap<>();
        test.put("Node 3",0);
        cliqueTree.setCurrentAssignment(test);

        // Run Belief Propagation
        Map<String,FactorNode> result = cliqueTree.runBeliefPropagation(bayesianNet.getAllNodesList().stream().map(node->node.getLabel()).collect(Collectors.toList()));

        System.out.println("Clique Tree (after BP): "+cliqueTree.toString());

        result.forEach((label,factor)->{
            System.out.println("Probability of "+label+": "+factor.toString());
        });

        GibbsChain chain = new GibbsChain(markovNet,test);
        for(int i = 0; i < 1000; i++) {
            System.out.println("Starting chain: "+i);
            Map<String,FactorNode> p = chain.next();
            if(i==999)p.forEach((label,f)->System.out.println("Prob "+label+": "+Arrays.toString(f.getWeights())));
        }
    }
}
