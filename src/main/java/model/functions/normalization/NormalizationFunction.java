package model.functions.normalization;

import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.function.Function;

/**
 * Created by ehallmark on 4/26/17.
 */
public interface NormalizationFunction {
    void normalize(INDArray weights);
}
