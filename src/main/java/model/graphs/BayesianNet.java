package model.graphs;

import model.nodes.Node;
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
            node.getInBound().forEach(parent->{
                // connect each parent to node
                newNet.connectNodes(node,parent);
                // connect all pairs of parents
                node.getInBound().forEach(parent2->{
                    if(!parent.equals(parent2)) {
                        newNet.connectNodes(parent.getLabel(),parent2.getLabel());
                    }
                });
            });
            // factors
            factorNodes.forEach(factor->{
                newNet.addFactorNode(factor.getWeights(),newNet.findNodes(factor.getNeighbors().stream().map(n->n.getLabel()).collect(Collectors.toList())).toArray(new Node[factor.getNeighbors().size()]));
            });
        });

        return newNet;
    }
}
