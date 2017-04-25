package model.graphs;

import model.edges.Edge;
import model.heuristics.triangulation.TriangulationHeuristic;
import model.nodes.CliqueNode;
import model.nodes.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created by Evan on 4/24/2017.
 */
public class MarkovNet extends Graph {
    // undirected graph
    public MarkovNet() {
        super(false);
    }

    // Returns triangulated (chordal) version of this graph
    public void triangulateInPlace(TriangulationHeuristic heuristic) {
        System.out.println("Starting triangulation with heuristic: "+heuristic.getClass().getName());
        List<Node> copyOfNodes = new ArrayList<>(allNodesList);
        Set<Edge> edges = new HashSet<>(allNodesList.size());
        Function<List<Node>,Integer> function = heuristic.nextNodeToEliminateFunction();
        while(!copyOfNodes.isEmpty()) {
            Integer nodeIdx = function.apply(copyOfNodes);
            Node node = copyOfNodes.get(nodeIdx);
            // add links to pairs adjacent nodes and store in edges Set
            List<Node> neighbors = node.getNeighbors();
            for(int i = 0; i < neighbors.size(); i++) {
                // add edge for easy reconstruction
                Node n1 = neighbors.get(i);
                edges.add(connectNodes(node,n1));
                for(int j = 0; j < neighbors.size(); j++) {
                    if(i!=j) {
                        Node n2 = neighbors.get(j);
                        edges.add(n1.connectNodes(n2));
                    }
                }
            }
            // remove node and all links to other nodes
            copyOfNodes.remove(nodeIdx);
            node.removeNeighborConnections();
        }

        System.out.println("Reconstructing graph from edges");
        // reconstruct
        edges.forEach(edge->{
            connectNodes(edge.getNode1(),edge.getNode2());
        });
        System.out.println("Completed triangulation");
    }

    public CliqueTree createCliqueTree() {
        return createCliqueTree(this);
    }

    public static CliqueTree createCliqueTree(MarkovNet graph) {
        // maximum cardinality search using perfect ordering
        List<Node> PEO = graph.findPerfectEliminitationOrdering();
        // find maximal clique tree
        AtomicInteger prevMark = new AtomicInteger(-1);
        AtomicInteger j = new AtomicInteger(0);
        Map<Node,Integer> markMap = new HashMap<>();
        Map<Node,List<Node>> M = new HashMap<>();
        Map<Node,Integer> C = new HashMap<>();
        Map<Node,Integer> lastMap = new HashMap<>();
        CliqueNode Cj = new CliqueNode();
        graph.allNodesList.forEach(node->markMap.put(node,0));
        CliqueTree cliqueTree = new CliqueTree();
        List<CliqueNode> cliqueNodes = new ArrayList<>();
        PEO.forEach(node->{
            int markX = markMap.get(node);
            if(markX<=prevMark.get()) {
                if(Cj!=null) {
                    cliqueNodes.add(Cj));
                }
                j.getAndIncrement();
                Cj.clear();
                Cj.addAll(M.get(node));
                Cj.add(node);
                int lastX = lastMap.get(node);

            } else {
                Cj.add(node);
            }



            prevMark.set(markX);
            C.put(node,j.get());
        });
        cliqueNodes.forEach(clique->cliqueTree.addNode(clique));
        return cliqueTree;
    }

    // Make sure the graph is triangulated, or one may not exist!
    List<Node> findPerfectEliminitationOrdering() {
        System.out.println("Starting to find perfect elimination ordering");
        // sequence of sigmas
        List<Set<Node>> setSequence = new LinkedList<>();
        // to output
        List<Node> outputSequence = new LinkedList<>();
        // initial set of sequence
        setSequence.add(new HashSet<>(allNodesList));
        while(!setSequence.isEmpty()) {
            // find and remove a node from the set sequence
            Set<Node> firstSet = setSequence.get(0);
            Node node = firstSet.stream().findAny().get();
            firstSet.remove(node);

            // if first set of sequence is empty, remove it
            if(firstSet.isEmpty()) setSequence.remove(0);

            // add node to the end of output
            outputSequence.add(0, node);
            System.out.println("Adding node: "+node.getLabel());

            // for each neighbor of node that exists in one of the sequence sets S
            List<Node> remainingNeighbors = new LinkedList<>(node.getNeighbors());

            int s = 0;
            while(s<setSequence.size()) {
                Set<Node> set = setSequence.get(s);
                Set<Node> newSet = new HashSet<>();
                int i = 0;
                while(i < remainingNeighbors.size()) {
                    Node neighbor = remainingNeighbors.get(i);
                    if(set.contains(neighbor)) {
                        // if set S has not been replaced while processing this node
                        newSet.add(neighbor);
                        // move w from S to T
                        remainingNeighbors.remove(i);
                    } else {
                        i++;
                    }

                }
                if(!newSet.isEmpty()) {
                    // create new empty replacement set T and place it before S in the sequence
                    setSequence.add(s,newSet);
                    s++;
                }
                s++;
            }
            // if S is empty, remove S from sequence
            setSequence.removeIf(set->set.isEmpty());
        }

        System.out.println("Found perfect elimination ordering");
        return outputSequence;
    }
}
