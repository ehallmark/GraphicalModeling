package model.functions.normalization;

import util.MathHelper;

import java.util.function.Function;

/**
 * Created by ehallmark on 4/26/17.
 */
public class DivideByPartition implements NormalizationFunction {

    public Function<double[],double[]> getFunction() {
        return (x)->{
            double sum = MathHelper.sum(x);
            for(int i = 0; i < x.length; i++) {
                x[i]=x[i]/sum;
            }
            return x;
        };
    }
}
