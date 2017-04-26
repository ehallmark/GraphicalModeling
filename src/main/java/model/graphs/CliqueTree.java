package model.graphs;

import model.nodes.CliqueNode;
import model.nodes.FactorNode;
import model.nodes.Node;

/**
 * Created by Evan on 4/25/2017.
 */
public class CliqueTree extends BayesianNet {
    public CliqueNode addNode(CliqueNode node) {
        allNodesList.add(node);
        return node;
    }

    @Override
    public Node addNode(String node, int cardinality) {
        throw new UnsupportedOperationException("Must use addNode(CliqueNode) signature");
    }

    public void constructFactors() {
        allNodesList.forEach(node->{
            if(node instanceof CliqueNode) {
                CliqueNode clique = (CliqueNode)node;
                FactorNode endFactor = null;
                int i = 0;
                while(i < factorNodes.size()) {
                    FactorNode factor = factorNodes.get(i);
                    // find clique to assign it to
                    if(clique.hasFactorScope(factor.getVarLabels())) {
                        if(endFactor==null) endFactor=factor;
                        else endFactor= endFactor.multiply(factor);
                        factorNodes.remove(i);
                    } else i++;
                }
                if(endFactor==null) throw new RuntimeException("Node "+node.getLabel()+" has no factor");
                clique.addFactor(endFactor);
            }
        });
    }

}
