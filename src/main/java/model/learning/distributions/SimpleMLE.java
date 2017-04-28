package model.learning.distributions;

import model.nodes.FactorNode;

/**
 * Created by ehallmark on 4/28/17.
 */
public class SimpleMLE extends Dirichlet {
    public SimpleMLE(FactorNode factor) { // Dirichlet with each alpha[i] = 0
        super(factor,0);
    }
}
