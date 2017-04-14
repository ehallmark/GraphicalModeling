package graph;

/**
 * Created by ehallmark on 4/13/17.
 */
public class Edge<T> {

    private Node<T> node1;
    private Node<T> node2;
    protected Edge(Node<T> node1, Node<T> node2) {
        this.node1=node1;
        this.node2=node2;
    }

    public Node<T> getNode1() {return node1; }
    public Node<T> getNode2() { return node2;}


    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Edge)) {
            return false;
        }

        Edge<T> otherEdge = (Edge<T>)other;

        Node<T> otherNode1 = otherEdge.getNode1();
        Node<T> otherNode2 = otherEdge.getNode2();
        if(node1.getLabel().equals(otherNode1.getLabel())&&node2.getLabel().equals(otherNode2.getLabel())) {
            return true;
        }

        if(node2.getLabel().equals(otherNode1.getLabel())&&node1.getLabel().equals(otherNode2.getLabel())) {
            return true;
        }

        return false;
    }
}
