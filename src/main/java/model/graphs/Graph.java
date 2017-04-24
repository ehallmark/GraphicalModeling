package model.graphs;

import lombok.Getter;
import lombok.Setter;
import model.nodes.FactorNode;
import model.nodes.Node;
import model.edges.Edge;
import util.Assignment;
import util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Created by Evan on 4/13/2017.
 */
public abstract class Graph implements Serializable {
    protected Map<String, Node> labelToNodeMap;
    protected Set<FactorNode> factorNodes;
    protected List<Node> allNodesList;
    protected boolean directed;
    @Getter @Setter
    protected Assignment currentAssignment;
    protected Stream<Assignment> dataAssignments;

    public Graph(boolean directed) {
        this.labelToNodeMap=new HashMap<>();
        this.allNodesList=new ArrayList<>();
        this.factorNodes=new HashSet<>();
        this.directed=directed;
    }

    public Node addBinaryNode(String label) { // default binary
        return this.addNode(label,2);
    }

    public Node findNode(String label) {
        return labelToNodeMap.get(label);
    }

    public Node addNode(String label, int cardinality) {
        if(labelToNodeMap.containsKey(label)) throw new RuntimeException("Label already exists");
        Node node = new Node(label,cardinality,directed);
        allNodesList.add(node);
        labelToNodeMap.put(label, node);
        return node;
    }

    public FactorNode addFactorNode(float[] weights, Node... connectingNodes) {
        String[] connectingLabels = new String[connectingNodes.length];
        int[] varCardinalities = new int[connectingNodes.length];
        for(int i = 0; i < connectingNodes.length; i++) {
            Node node = connectingNodes[i];
            String label = connectingNodes[i].getLabel();
            if(label==null) throw new RuntimeException("Unable to find node");
            connectingLabels[i] = label;
            varCardinalities[i] = node.getCardinality();
        }
        FactorNode factor = new FactorNode(weights,connectingLabels,varCardinalities);
        Arrays.stream(connectingNodes).forEach(node->{
            node.connectFactor(factor);
        });
        factorNodes.add(factor);
        return factor;
    }

    public Edge connectNodes(String label1, String label2) {
        return connectNodes(labelToNodeMap.get(label1),labelToNodeMap.get(label2));
    }

    public Edge connectNodes(Node node1, Node node2) {
        if(node1==null||node2==null) return null;
        Edge edge = node1.connectNodes(node2);
        return edge;
    }

    public void removeCurrentAssignment() {
        currentAssignment=null;
    }

    public FactorNode variableElimination(String[] queryVars, Assignment varAssignments) {
        Set<String> queryLabels = new HashSet<>();
        Arrays.stream(queryVars).forEach(var->queryLabels.add(var));
        Set<String> evidenceLabels = new HashSet<>();
        varAssignments.forEach(a->evidenceLabels.add(a._1));
        // choose elimination ordering


        // Initialize F
        AtomicReference<List<FactorNode>> F = new AtomicReference<>(new LinkedList<>());
        factorNodes.forEach(fac->{
            F.get().add(fac);
        });

        // HOW TO ADD EVIDENCE?
        varAssignments.forEach(assignment->{
            Node x = labelToNodeMap.get(assignment._1);
            float[] weights = new float[x.getCardinality()];
            for(int i = 0; i < x.getCardinality(); i++) {
                if(assignment._2.equals(i)) {
                    weights[i]=1f;
                } else {
                    weights[i]=0f;
                }
            }
            F.get().add(new FactorNode(weights,new String[]{x.getLabel()},new int[]{x.getCardinality()}));
        });

        for(Node z : allNodesList) {
            if(!queryLabels.contains(z.getLabel())) {
                F.set(sumProductVariableElimination(F.get(), z));
            }
        }

        if(F.get().isEmpty()) throw new RuntimeException("Factor Stack should not be empty");

        // multiply to get query
        FactorNode query = F.get().remove(0);
        while(!F.get().isEmpty()) query = query.multiply(F.get().remove(0));

        float sum = query.sumOut(query.getVarLabels()).getWeights()[0];

        // normalize
        query.reNormalize(x->{
            for(int i = 0; i < x.length; i++) {
                x[i]=x[i]/sum;
            }
            return x;
        });

        return query;
    }

    protected List<FactorNode> sumProductVariableElimination(List<FactorNode> F, Node x) {
        FactorNode newFac = null;
        List<FactorNode> newList = new LinkedList<>();
        while(!F.isEmpty()) {
            FactorNode f = F.remove(0);
            if(f.getVarToIndexMap().containsKey(x.getLabel())) {
                if(newFac==null) newFac=f;
                else newFac=newFac.multiply(f);
            } else {
                newList.add(f);
            }
        }
        if(newFac==null) throw new RuntimeException("Nothing happened");
        // sum out
        newFac=newFac.sumOut(new String[]{x.getLabel()});
        newList.add(newFac);
        return newList;
    }
}
