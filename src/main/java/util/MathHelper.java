package util;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

/**
 * Created by ehallmark on 4/26/17.
 */
public class MathHelper {

    public static int indexOfMaxValue(INDArray x) {
        return Nd4j.getExecutioner().execAndReturn(new IMax(x)).getFinalResult();
    }


}
