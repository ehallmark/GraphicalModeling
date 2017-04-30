package model.functions.normalization;

import util.MathHelper;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by ehallmark on 4/26/17.
 */
public class SoftMax implements NormalizationFunction {
    public Function<double[],double[]> getFunction() {
        return (x)->softMax(x);
    }

    // Numerically stable implementation
    public static double[] softMax(double[] in) {
        double max = MathHelper.max(in);
        for(int i = 0; i < in.length; i++) {
            in[i]-=max;
        }
        for(int i = 0; i < in.length; i++) {
            in[i]= Math.exp(in[i]);
        }
        double sum = MathHelper.sum(in);
        for(int i = 0; i < in.length; i++) {
            in[i] /= sum;
        }
        return in;
    }
}
