package model.nodes;

import java.util.Arrays;

/**
 * Created by ehallmark on 4/18/17.
 */
public class TestFactorNode {
    public static void main(String[] args) throws Exception {
        // TEST
        float[] phi1 = new float[] {
                0.2f,0.25f,0f,0.3f,0.1f,0.15f
        };
        float[] phi2 = new float[] {
                0.1f,0.25f,0f,0.3f,0f,0.1f,0.1f,0.24f,0.03f
        };
        FactorNode AB = new FactorNode(phi1,new String[]{"A","B"},new int[]{2,3});
        FactorNode BC = new FactorNode(phi2,new String[]{"B","C"},new int[]{3,3});
        FactorNode result = AB.multiply(BC);
        FactorNode result2 = BC.multiply(AB);
        if(Arrays.equals(result.weights,result2.weights)) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED: AB*BC should equal BC*AB");
        }
        int num = 18;
        if(result.weights.length==num) System.out.println("PASSED");
        else System.out.println("FAILED: "+result.weights.length+" should be "+num);

        result2 = result.sumOut(new String[]{"A"});
        num = 9;
        if(result2.weights.length==num) System.out.println("PASSED");
        else System.out.println("FAILED: "+result2.weights.length+" should be "+num);
        result2 = result.sumOut(new String[]{"B"});
        num = 6;
        if(result2.weights.length==num) System.out.println("PASSED");
        else System.out.println("FAILED: "+result2.weights.length+" should be "+num);

        FactorNode X = new FactorNode(new float[]{1f,2f,3f,5f,7f,11f},new String[]{"A","B"}, new int[]{2,3});
        System.out.println("X: "+Arrays.toString(X.sumOut(new String[]{"B"}).weights));
    }
}
