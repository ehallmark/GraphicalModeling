package model.functions.normalization;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import util.MathHelper;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by ehallmark on 4/26/17.
 */
public class SoftMax implements NormalizationFunction {
    public void normalize(INDArray weights) {
        Nd4j.getExecutioner().exec(new org.nd4j.linalg.api.ops.impl.transforms.SoftMax(weights));
    }
}
