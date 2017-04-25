package model.graphs;

import model.nodes.Node;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Evan on 4/24/2017.
 */
public class BayesianNet extends Graph {
    // directed graph
    public BayesianNet() {
        super(true);
    }


    public MarkovNet moralize() {
        MarkovNet newNet = new MarkovNet();
        allNodesList.forEach(node->{
            newNet.addNode(node.getLabel(),node.getCardinality());
        });
        allNodesList.forEach(node->{
            List<Node> parents = node.getInBound();
            for(int i = 0; i < parents.size(); i++) {
                // connect each parent to node
                Node parent = parents.get(i);
                newNet.connectNodes(node,parent);
                // connect all pairs of parents
                for(int j = 0; j < parents.size(); j++) {
                    if(i!=j) {
                        Node parent2 = parents.get(j);
                        newNet.connectNodes(parent.getLabel(),parent2.getLabel());
                    }
                }
            }
            // factors
            factorNodes.forEach(factor->{
                newNet.addFactorNode(factor.getWeights(),newNet.findNodes(factor.getNeighbors().stream().map(n->n.getLabel()).collect(Collectors.toList())).toArray(new Node[factor.getNeighbors().size()]));
            });
        });

        return newNet;
    }
}
