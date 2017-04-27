package model.graphs;

import model.nodes.CliqueNode;
import model.nodes.FactorNode;
import model.nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Evan on 4/25/2017.
 */
public class CliqueTree extends BayesianNet {
    protected CliqueNode root;
    public CliqueNode addNode(CliqueNode node) {
        allNodesList.add(node);
        return node;
    }

    @Override
    public Node addNode(String node, int cardinality) {
        throw new UnsupportedOperationException("Must use addNode(CliqueNode) signature");
    }

    public void constructFactors() {
        List<FactorNode> newFactors = new ArrayList<>();
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
                newFactors.add(endFactor);
            }
        });
        if(factorNodes.size()>0) throw new RuntimeException("Could not include every factor!");
        this.factorNodes=newFactors;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Num factors: "+factorNodes.size());
        sj.add("Num nodes: "+allNodesList.size());
        AtomicInteger idx = new AtomicInteger(0);
        factorNodes.forEach(factor->{
            sj.add("Clique: "+idx.getAndIncrement());
            sj.add(factor.toString());
        });
        return sj.toString();
    }

    // Recursive
    protected FactorNode accumulateMessagesTo(CliqueNode root) {
        if(root.getChildren().isEmpty()) {
            // leaf
            return root.sendMessagesToParent();
        }
        else root.getChildren().stream().map(child->accumulateMessagesTo((CliqueNode)child)).collect(Collectors.toList());
    }

    // Recursive
    protected void propagateMessagesFrom(CliqueNode root) {
        root.sendMessagesToChildren();
        root.getChildren().forEach(child->{
            propagateMessagesFrom((CliqueNode)child);
        });
    }

    public void runBeliefPropagation() {
        // select root
        Node root = allNodesList.stream().filter(n->n.getInBound().isEmpty()).findFirst().get();
        if(root==null) throw new RuntimeException("No root found");
        // pass messages inwards starting from the leaves
        accumulateMessagesTo((CliqueNode)root);

        // second message passing starting from root
        propagateMessagesFrom((CliqueNode)root);
    }
}
