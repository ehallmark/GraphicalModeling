package model.graphs;

import lombok.Getter;
import lombok.Setter;
import model.functions.normalization.DivideByPartition;
import model.functions.normalization.NormalizationFunction;
import model.functions.normalization.SoftMax;
import model.learning_algorithms.LearningAlgorithm;
import model.nodes.FactorNode;
import model.nodes.Node;
import model.edges.Edge;
import util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Evan on 4/13/2017.
 */
public abstract class Graph implements Serializable {
    protected Map<String, Node> labelToNodeMap;
    @Getter
    protected List<FactorNode> factorNodes;
    @Getter
    protected List<Node> allNodesList;
    @Getter @Setter
    protected List<Pair<String,Integer>> currentAssignment;

    public Graph() {
        this.labelToNodeMap=new HashMap<>();
        this.allNodesList=new ArrayList<>();
        this.factorNodes=new ArrayList<>();
    }

    public Node addBinaryNode(String label) { // default binary
        return this.addNode(label,2);
    }

    public Node findNode(String label) {
        return labelToNodeMap.get(label);
    }

    public Node addNode(String label, int cardinality) {
        if(labelToNodeMap.containsKey(label)) return labelToNodeMap.get(label);
        Node node = new Node(label,cardinality);
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
        for(int i = 0; i < connectingNodes.length; i++) {
            Node node = connectingNodes[i];
            node.addFactor(factor);
        }
        factorNodes.add(factor);
        return factor;
    }

    public Node[] findNodes(String... labels) {
        Node[] toReturn = new Node[labels.length];
        for(int i = 0; i < toReturn.length; i++) {
            toReturn[i]=findNode(labels[i]);
        }
        return toReturn;
    }

    public void applyLearningAlgorithm(LearningAlgorithm function, int epochs) {
        for(int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Starting epoch: "+(epoch+1));
            Double currentScore = function.runAlgorithm().andThen(function.computeCurrentScore()).apply(this);
            System.out.println("    Score: "+currentScore);
        }
    }

    public void connectNodes(String label1, String label2) {
        connectNodes(labelToNodeMap.get(label1),labelToNodeMap.get(label2));
    }

    public abstract void connectNodes(Node node1, Node node2);

    public void removeCurrentAssignment() {
        currentAssignment=null;
    }

    public FactorNode variableEliminationDefault(String[] queryVars) {
        return variableElimination(queryVars,new DivideByPartition());
    }

    public FactorNode variableEliminationLogistic(String[] queryVars) {
        return variableElimination(queryVars,new SoftMax());
    }

    public FactorNode variableElimination(String[] queryVars, NormalizationFunction normalizationFunction) {
        if(currentAssignment==null) throw new RuntimeException("Must set current assignment");

        Set<String> queryLabels = new HashSet<>();
        Arrays.stream(queryVars).forEach(var->queryLabels.add(var));
        Set<String> evidenceLabels = new HashSet<>();
        currentAssignment.forEach(a->evidenceLabels.add(a._1));
        // choose elimination ordering


        // Initialize F
        AtomicReference<List<FactorNode>> F = new AtomicReference<>(new LinkedList<>());
        factorNodes.forEach(fac->{
            F.get().add(fac);
        });

        // HOW TO ADD EVIDENCE?
        currentAssignment.forEach(assignment->{
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

        // normalize
        query.reNormalize(normalizationFunction);

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
