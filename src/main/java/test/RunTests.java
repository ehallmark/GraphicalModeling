package test;

import graph.TestFactorNode;
import graph.TestGraph;

/**
 * Created by ehallmark on 4/18/17.
 */
public class RunTests {
    public static void main(String[] args) throws Exception{
        System.out.println("Starting Factor Node Tests");
        TestFactorNode.main(args);
        System.out.println("Starting Graph Tests");
        TestGraph.main(args);
    }
}
