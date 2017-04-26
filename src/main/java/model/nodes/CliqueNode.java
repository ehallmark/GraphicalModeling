package model.nodes;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/25/17.
 */
public class CliqueNode extends Node {
    protected Collection<Node> nodes;
    @Getter
    protected Set<String> nameSet;

    public CliqueNode(Collection<Node> nodes) {
        super(null,nodes.size());
        this.nodes=nodes;
        this.nameSet=new HashSet<>();
        nodes.forEach(node->nameSet.add(node.getLabel()));
    }

    public CliqueNode() {
        this(new ArrayList<>());
    }

    public void addNode(Node node) {
        if(!this.nameSet.contains(node.getLabel())) {
            this.nodes.add(node);
            this.nameSet.add(node.getLabel());
        }
        cardinality=this.nodes.size();
    }

    // Incorporate incoming child messages and return resulting message to parent (1st pass)
    public float[] receiveMessagesFromChildren(List<float[]> messages) {
        return null;
    }

    // Send messages downstream (2nd pass)
    public void sendMessagesToChildren() {

    }

    public boolean hasFactorScope(String[] varLabels) {
        return Arrays.stream(varLabels).allMatch(label->nameSet.contains(label));
    }
}
