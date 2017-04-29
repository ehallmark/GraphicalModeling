package model.learning.distributions;

import model.nodes.FactorNode;

/**
 * Created by Evan on 4/29/2017.
 */
public interface DistributionCreator {
    Distribution create(FactorNode factor);
}
