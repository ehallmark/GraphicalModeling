package util;

import java.util.Arrays;

/**
 * Created by ehallmark on 4/26/17.
 */
public class MathHelper {
    public static double sum(double[] x) {
        double y = 0d;
        for(double xi : x) y+=xi;
        return y;
    }

    public static double max(double[] x) {
        double max = Float.MIN_VALUE;
        for(double xi : x) {
            if(xi>max)max=xi;
        }
        return max;
    }

    public static int indexOfMaxValue(double[] x) {
        double max = max(x);
        for(int i = 0; i < x.length; i++) {
            double xi = x[i];
            if(xi==max) return i;
        }
        return -1;
    }
}
