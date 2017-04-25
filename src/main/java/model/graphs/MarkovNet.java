package model.graphs;

import model.edges.Edge;
import model.heuristics.triangulation.TriangulationHeuristic;
import model.nodes.CliqueNode;
import model.nodes.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    //  based on given triangulation heuristic
    public void triangulateInPlace(TriangulationHeuristic heuristic) {
        System.out.println("Starting triangulation");
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
            copyOfNodes.remove(nodeIdx.intValue());
            // this method removes the correct connections
            node.removeNeighborConnections();
        }

        System.out.println("Reconstructing graph from edges");
        // reconstruct
        edges.forEach(edge->{
            connectNodes(edge.getNode1(),edge.getNode2());
        });
        System.out.println("Completed triangulation with: "+edges.size()+" edges");
    }

    public CliqueTree createCliqueTree() {
        return createCliqueTree(this);
    }

    public static CliqueTree createCliqueTree(MarkovNet graph) {
        // maximum cardinality search using perfect ordering
        List<Node> PEO = graph.findPerfectEliminitationOrdering();
        // find maximal clique tree
        System.out.println("Constructing clique tree");
        AtomicInteger prevMark = new AtomicInteger(-1);
        AtomicInteger j = new AtomicInteger(0);
        Map<Node,Integer> markMap = new HashMap<>();
        Map<Node,Set<Node>> M = new HashMap<>();
        Map<Node,CliqueNode> C = new HashMap<>();
        Map<Node,Node> lastMap = new HashMap<>();
        AtomicReference<CliqueNode> CjRef = new AtomicReference<>(new CliqueNode());
        graph.allNodesList.forEach(node->{
            markMap.put(node,0);
            M.put(node,new HashSet<>());
        });
        CliqueTree cliqueTree = new CliqueTree();
        cliqueTree.addNode(CjRef.get());
        // Reverse Perfect Elimination Algorithm
        Collections.reverse(PEO);

        PEO.forEach(node->{
            CliqueNode Cj = CjRef.get();
            int markX = markMap.get(node);
            if(markX<=prevMark.get()) {
                j.getAndIncrement();
                System.out.println("Adding clique tree: "+j.get());
                // create clique
                Cj = new CliqueNode(new ArrayList<>(M.get(node)));
                CjRef.set(Cj);
                Cj.addNode(node);
                // add node to graph
                cliqueTree.addNode(Cj);
                // create link
                Node lastNode = lastMap.get(node);
                cliqueTree.connectNodes(Cj,C.get(lastNode));
            } else {
                Cj.addNode(node);
            }

            node.getNeighbors().forEach(neighbor->{
                M.get(neighbor).add(node);
                markMap.put(neighbor,markMap.get(neighbor)+1);
                lastMap.put(neighbor,node);
            });

            prevMark.set(markX);
            C.put(node,Cj);
        });
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
                        set.remove(neighbor);
                        if(set.isEmpty()) {
                            setSequence.remove(s);
                            break;
                        }
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
        }

        System.out.println("Found perfect elimination ordering");
        return outputSequence;
    }
}
