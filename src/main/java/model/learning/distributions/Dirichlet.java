package model.learning.distributions;

import model.nodes.FactorNode;
import util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by ehallmark on 4/28/17.
 */
public class Dirichlet implements Distribution {
    public Dirichlet(FactorNode factor, float alpha) {
        this(factor, Arrays.stream(factor.getVarLabels()).map(label->new Pair<>(label,alpha)).collect(Collectors.toList()));
    }

    public Dirichlet(FactorNode factor,  List<Pair<String, Float>> alphas) {

    }
}
