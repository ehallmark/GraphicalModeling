package model.graphs;

import model.nodes.CliqueNode;
import model.nodes.Node;

/**
 * Created by Evan on 4/25/2017.
 */
public class CliqueTree extends MarkovNet {
    public CliqueNode addNode(CliqueNode node) {
        allNodesList.add(node);
        return node;
    }

    public int size() {
        return allNodesList.size();
    }
}
