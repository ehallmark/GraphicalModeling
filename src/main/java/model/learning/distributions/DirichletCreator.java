package model.learning.distributions;

import model.nodes.FactorNode;

/**
 * Created by Evan on 4/29/2017.
 */
public class DirichletCreator implements DistributionCreator {
    protected float alpha;
    public DirichletCreator(float alpha) {
        this.alpha=alpha;
    }
    @Override
    public Distribution create(FactorNode factor) {
        Dirichlet dirichlet = new Dirichlet(factor,alpha);
        dirichlet.initializeWeights();
        return dirichlet;
    }
}
