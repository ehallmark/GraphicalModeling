package model.graphs;

import model.nodes.CliqueNode;
import model.nodes.FactorNode;
import model.nodes.Node;
import util.CliqueFactorList;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by Evan on 4/25/2017.
 */
public class CliqueTree extends BayesianNet {
    public CliqueTree() {
        super();
    }

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
                clique.setCliqueFactor(endFactor);
            }
        });
        if(factorNodes.size()>0) throw new RuntimeException("Could not include every factor!");
        this.factorNodes=new CliqueFactorList(allNodesList);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n");
        sj.add("Num nodes: "+allNodesList.size());
        AtomicInteger idx = new AtomicInteger(0);
        allNodesList.forEach(n->{
            CliqueNode clique = (CliqueNode)n;
            sj.add("Clique: "+idx.getAndIncrement());
            sj.add(clique.getCliqueFactor().toString());
        });
        return sj.toString();
    }

    // Recursive
    protected void accumulateMessagesTo(CliqueNode root) {
        // head recursion
        root.getChildren().forEach(child->{
            accumulateMessagesTo((CliqueNode)child);
        });

        // incorporate any messages
        root.incorporateMessagesIntoFactor();

        // send next messages
        root.prepAndSendMessageToParent();
    }

    // Recursive
    protected void propagateMessagesFrom(CliqueNode root) {
        // incorporate any messages
        root.incorporateMessagesIntoFactor();

        // send next messages
        root.prepAndSendMessagesToChildren();

        // tail recursion
        root.getChildren().forEach(child->{
            propagateMessagesFrom((CliqueNode)child);
        });
    }


    public void runBeliefPropagation() {
        // select root
        Node root = allNodesList.stream().filter(n->n.getInBound().isEmpty()).findFirst().get();
        if(root==null) throw new RuntimeException("No root found");

        // 1) pass messages inwards starting from the leaves
        accumulateMessagesTo((CliqueNode)root);

        // 2) second message passing starting from root
        propagateMessagesFrom((CliqueNode)root);

        // 3) Done!
    }
}
