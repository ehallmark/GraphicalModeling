package model.heuristics.triangulation;

import model.nodes.Node;

import java.util.List;
import java.util.function.Function;

/**
 * Created by ehallmark on 4/25/17.
 */
public class MinimalCliqueSizeHeuristic implements TriangulationHeuristic {
    @Override
    public Function<List<Node>, Integer> nextNodeToEliminateFunction() {
        return (nodes)-> {
            return null;
        };
    }
}
