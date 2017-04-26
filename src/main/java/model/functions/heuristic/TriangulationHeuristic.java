package model.functions.heuristic;

import model.graphs.Graph;
import model.nodes.Node;

import java.util.List;
import java.util.function.Function;

/**
 * Created by ehallmark on 4/24/17.
 *
 * Function that takes a list of nodes and outputs
 *  the index of the next node to remove
 */
public interface TriangulationHeuristic {
    Function<List<Node>,Integer> nextNodeToEliminateFunction();
}
