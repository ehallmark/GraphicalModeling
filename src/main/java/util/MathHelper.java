package util;

import org.nd4j.linalg.factory.Nd4j;

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

    public static double[] divideBy(double[] in, double d) {
        if(d==0) throw new RuntimeException("Cannot divide by zero");
        double[] out = new double[in.length];
        for(int i = 0; i < out.length; i++) {
            out[i]=in[i]/d;
        }
        return out;
    }

    public static double[] subtract(double[] y, double[] x) {
        if(y.length!=x.length) throw new RuntimeException("x and y have different lengths");
        double[] out = new double[y.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = y[i]-x[i];
        }
        return out;
    }

    public static double computeDistance(double[] x, double[] y) {
        double sumX = sum(x);
        double sumY = sum(y);
        double[] unitX = divideBy(x,sumX);
        double[] unitY = divideBy(y,sumY);
        double[] yMinusX = subtract(unitY,unitX);
        return max(yMinusX);
    }

    public static double expectedValue(double[] x, double[] values) {
        if(x.length!=values.length) throw new RuntimeException("Values and probabilities are inconsistent in length");
        double expectation = 0d;
        for(int i = 1; i < x.length; i++) {
            expectation+=x[i]*values[i];
        }
        return expectation;
    }

    public static double[] defaultValues(int cardinality) {
        if(cardinality<=0) throw new RuntimeException("Cardinality must be positive");
        double[] values = new double[cardinality];
        for(int i = 0; i < cardinality; i++) {
            values[i]=i;
        }
        return values;
    }
}
