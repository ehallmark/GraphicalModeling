package model.functions.normalization;

import java.util.function.Function;

/**
 * Created by ehallmark on 4/26/17.
 */
public interface NormalizationFunction {
    Function<double[],double[]> getFunction();
}
