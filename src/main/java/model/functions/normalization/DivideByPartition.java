package model.functions.normalization;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;
import util.MathHelper;

import java.util.function.Function;

/**
 * Created by ehallmark on 4/26/17.
 */
public class DivideByPartition implements NormalizationFunction {
    public void normalize(INDArray weights) {
        weights.divi(weights.sumNumber().doubleValue());
    }
}
