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

    public boolean hasFactorScope(String[] varLabels) {
        return Arrays.stream(varLabels).allMatch(label->nameSet.contains(label));
    }
}
