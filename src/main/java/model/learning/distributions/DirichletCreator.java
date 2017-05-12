package model.learning.distributions;

import lombok.Setter;
import model.nodes.FactorNode;

/**
 * Created by Evan on 4/29/2017.
 */
public class DirichletCreator extends DistributionCreator {
    protected double alpha;
    protected final boolean useGradientDescent;
    public DirichletCreator(double alpha, boolean useGradientDescent) {
        this.alpha=alpha;
        this.useGradientDescent=useGradientDescent;
    }

    public DirichletCreator(double alpha) {
        this(alpha,false);
    }

    @Override
    public Distribution create(FactorNode factor) {
        Dirichlet dirichlet = new Dirichlet(factor,alpha,useGradientDescent);
        dirichlet.initialize();
        return dirichlet;
    }
}
