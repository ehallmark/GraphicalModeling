package graph;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Edge {

    private Node node1;
    private Node node2;
    protected Edge(Node node1, Node node2) {
        this.node1=node1;
        this.node2=node2;
    }

    public Node getNode1() {return node1; }
    public Node getNode2() { return node2;}


    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Edge)) {
            return false;
        }

        Edge otherEdge = (Edge)other;

        Node otherNode1 = otherEdge.getNode1();
        Node otherNode2 = otherEdge.getNode2();
        if(node1.getLabel().equals(otherNode1.getLabel())&&node2.getLabel().equals(otherNode2.getLabel())) {
            return true;
        }

        if(node2.getLabel().equals(otherNode1.getLabel())&&node1.getLabel().equals(otherNode2.getLabel())) {
            return true;
        }

        return false;
    }
}
