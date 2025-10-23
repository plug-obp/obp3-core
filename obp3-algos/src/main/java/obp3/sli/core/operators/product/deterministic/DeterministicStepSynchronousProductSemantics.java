package obp3.sli.core.operators.product.deterministic;

import obp3.runtime.sli.DeterministicSemanticRelation;
import obp3.sli.core.operators.product.Product;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.product.deterministic.model.DeterministicStepProductParameters;

import java.util.Optional;

public class DeterministicStepSynchronousProductSemantics<LA, LC, RA, RC> implements DeterministicSemanticRelation<Product<Step<LA, LC>, RA>, Product<LC, RC>> {
    DeterministicStepProductParameters<LA, LC, RA, RC> model;

    public DeterministicStepSynchronousProductSemantics(DeterministicStepProductParameters<LA, LC, RA, RC> model) {
        this.model = model;
    }

    @Override
    public Optional<Product<LC, RC>> initial() {
        return model.lhs().initial().flatMap(lc ->
                model.rhs().initial().map( rc ->
                        new Product<>(lc, rc)));
    }

    @Override
    public Optional<Product<Step<LA, LC>, RA>> actions(Product<LC, RC> configuration) {
        var la = model.lhs().actions(configuration.l());
        if (la.isPresent()) {
            var lt = model.lhs().execute(la.get(), configuration.l());
            if (lt.isPresent()) {
                var l_step = new Step<>(configuration.l(), la, lt.get());
                return model.rhs()
                        .actions(l_step, configuration.r())
                        .map(rav ->
                                new Product<>(l_step, rav));
            }
        }
        //the deadlock case: the kripke does not have a step, add stuttering
        var l_step = new Step<>(configuration.l(), Optional.<LA>empty(), configuration.l());
        return model.rhs()
                .actions(l_step, configuration.r())
                .map(ra ->
                        new Product<>(l_step, ra));
    }

    @Override
    public Optional<Product<LC, RC>> execute(Product<Step<LA, LC>, RA> action, Product<LC, RC> configuration) {
        var l_step = action.l();
        return model.rhs()
                .execute(action.r(), l_step, configuration.r())
                .map(rt ->
                        new Product<>(l_step.end(), rt));
    }
}
