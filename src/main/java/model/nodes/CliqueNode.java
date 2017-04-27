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

    // Incorporate incoming messages
    public float[] receiveMessages(List<float[]> messages) {
        return null;
    }

    // Send messages upstream (1st pass)
    public void sendMessagesToParent() {
        if(this.getParents().size()>1) throw new RuntimeException("Invalid tree");
        this.getParents().forEach(parent->{
            float[] message = prepMessageFor(parent);
            ((CliqueNode)parent).receiveMessages(Arrays.asList(message));
        });
    }

    // Send messages downstream (2nd pass)
    public void sendMessagesToChildren() {
        this.getChildren().forEach(child->{
            float[] message = prepMessageFor(child);
            ((CliqueNode)child).receiveMessages(Arrays.asList(message));
        });
    }

    //
    public float[] prepMessageFor(Node otherNode) {
        return null;
    }

    public boolean hasFactorScope(String[] varLabels) {
        return Arrays.stream(varLabels).allMatch(label->nameSet.contains(label));
    }
}
