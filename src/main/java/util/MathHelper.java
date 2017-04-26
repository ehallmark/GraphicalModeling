package util;

import java.util.Arrays;

/**
 * Created by ehallmark on 4/26/17.
 */
public class MathHelper {
    public static float sum(float[] x) {
        float y = 0f;
        for(float xi : x) y+=xi;
        System.out.println("Sum of "+ Arrays.toString(x)+": "+y);
        return y;
    }

    public static double sum(double[] x) {
        double y = 0f;
        for(double xi : x) y+=xi;
        System.out.println("Sum of "+ Arrays.toString(x)+": "+y);
        return y;
    }

    public static float max(float[] x) {
        float max = Float.MIN_VALUE;
        for(float xi : x) {
            if(xi>max)max=xi;
        }
        System.out.println("Max of "+ Arrays.toString(x)+": "+max);
        return max;
    }
}
