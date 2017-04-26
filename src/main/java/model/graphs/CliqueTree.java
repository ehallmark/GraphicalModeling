package model.graphs;

import model.nodes.CliqueNode;

/**
 * Created by Evan on 4/25/2017.
 */
public class CliqueTree extends BayesianNet {

    public CliqueNode addNode(CliqueNode node) {
        allNodesList.add(node);
        return node;
    }

}
