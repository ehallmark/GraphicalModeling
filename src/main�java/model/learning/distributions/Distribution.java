package model.learning.distributions;

import java.util.Map;

/**
 * Created by ehallmark on 4/28/17.
 */
public interface Distribution {
    void train(Map<String,Integer> assignmentMap);
    void initializeWeights();

}
