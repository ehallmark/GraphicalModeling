package model.graphs;

import model.heuristics.triangulation.TriangulationHeuristic;
import model.nodes.Node;

import java.util.List;

/**
 * Created by Evan on 4/24/2017.
 */
public class MarkovNet extends Graph {
    // undirected graph
    public MarkovNet() {
        super(false);
    }

    // Returns triangulated (chordal) version of this graph
    public MarkovNet triangulated(TriangulationHeuristic heuristic) {
        return null;
    }


    public MarkovNet createCliqueTree() { return null; }

    // Make sure the graph is triangulated, or one may not exist!
    List<Node> findPerfectEliminitationOrdering() {
        return null;
    }
}
