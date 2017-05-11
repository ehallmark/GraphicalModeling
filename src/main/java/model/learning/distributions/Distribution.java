package model.learning.distributions;

import java.util.Map;

/**
 * Created by ehallmark on 4/28/17.
 */
public interface Distribution {
    void train(Map<String,Integer> assignmentMap);
    void initialize();
    // This method copies results of weightsCopy into weights and normalizes
    void finish();

}
