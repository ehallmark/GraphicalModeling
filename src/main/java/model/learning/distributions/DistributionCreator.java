package model.learning.distributions;

import lombok.Setter;
import model.nodes.FactorNode;

/**
 * Created by Evan on 4/29/2017.
 */
public abstract class DistributionCreator {
    @Setter
    protected boolean useGradientDescent;
    public abstract Distribution create(FactorNode factor);
}
