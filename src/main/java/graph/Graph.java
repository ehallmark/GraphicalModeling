package graph;

import util.Pair;

import java.util.*;

/**
 * Created by Evan on 4/13/2017.
 */
public class Graph {
    private Map<String,Node> labelToNodeMap;
    private Set<FactorNode> factorNodes;
    private List<Node> allNodesList;

    public Graph() {
        this.labelToNodeMap=new HashMap<>();
        this.allNodesList=new ArrayList<>();
        this.factorNodes=new HashSet<>();
    }

    public Node addNode(String label) { // default binary
        return this.addNode(label,2);
    }

    public Node findNode(String label) {
        return labelToNodeMap.get(label);
    }

    public Node addNode(String label, int cardinality) {
        if(labelToNodeMap.containsKey(label)) throw new RuntimeException("Label already exists");
        Node node = new Node(label,cardinality);
        allNodesList.add(node);
        labelToNodeMap.put(label, node);
        return node;
    }

    public FactorNode addFactorNode(float[] weights, Node... nodes) {
        String[] connectingLabels = new String[nodes.length];
        int[] varCardinalities = new int[nodes.length];
        for(int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            String label = nodes[i].getLabel();
            if(label==null) throw new RuntimeException("Unable to find node");
            connectingLabels[i] = label;
            varCardinalities[i] = node.cardinality;
        }
        FactorNode factor = new FactorNode(weights,connectingLabels,varCardinalities);
        Arrays.stream(nodes).forEach(node->{
            node.connectFactor(factor);
            factor.connectNode(node);
        });
        factorNodes.add(factor);
        return factor;
    }

    public Edge connectNodes(String label1, String label2, boolean directed) {
        return connectNodes(labelToNodeMap.get(label1),labelToNodeMap.get(label2),directed);
    }

    public Edge connectNodes(Node node1, Node node2, boolean directed) {
        if(node1==null||node2==null) return null;
        Edge edge = node1.connectNode(node2);
        if(!directed)node2.connectNode(node1);
        return edge;
    }

    public FactorNode variableElimination(String[] queryVars, List<Pair<String,Integer>> varAssignments) {
        Set<String> queryLabels = new HashSet<>();
        Arrays.stream(queryVars).forEach(var->queryLabels.add(var));
        Set<String> evidenceLabels = new HashSet<>();
        varAssignments.forEach(a->evidenceLabels.add(a._1));
        // choose elimination ordering


        // Initialize F
        List<FactorNode> F = new LinkedList<>();
        for(FactorNode fac : factorNodes) {
            F.add(fac);
        }

        // HOW TO ADD EVIDENCE?
        for(Pair<String,Integer> assignment : varAssignments) {
            Node x = labelToNodeMap.get(assignment._1);
            float[] weights = new float[x.cardinality];
            for(int i = 0; i < x.cardinality; i++) {
                if(assignment._2.equals(i)) {
                    weights[i]=2f;
                } else {
                    weights[i]=0f;
                }
            }
            F.add(new FactorNode(weights,new String[]{x.getLabel()},new int[]{x.cardinality}));
        }

        for(Node z : allNodesList) {
            if(!queryLabels.contains(z.getLabel())) {
                F = sumProductVariableElimination(F, z);
            }
        }

        if(F.isEmpty()) throw new RuntimeException("Factor Stack should not be empty");

        // multiply to get query
        FactorNode query = F.remove(0);
        while(!F.isEmpty()) query = query.multiply(F.remove(0));

        float sum = query.sumOut(query.varLabels).getWeights()[0];

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
            if(f.varToIndexMap.containsKey(x.getLabel())) {
                if(newFac==null) newFac=f;
                else newFac=newFac.multiply(f);
            } else {
                newList.add(f);
            }
        }
        if(newFac==null) throw new RuntimeException("Nothing happend");
        // sum out
        newFac=newFac.sumOut(new String[]{x.getLabel()});
        newList.add(newFac);
        return newList;
    }
}
