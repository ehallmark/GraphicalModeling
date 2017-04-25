package model.heuristics.triangulation;

import model.graphs.Graph;
import model.nodes.Node;

import java.util.List;
import java.util.function.Function;

/**
 * Created by ehallmark on 4/24/17.
 */
public interface TriangulationHeuristic {
    Function<List<Node>,Integer> nextNodeToEliminateFunction();
}
