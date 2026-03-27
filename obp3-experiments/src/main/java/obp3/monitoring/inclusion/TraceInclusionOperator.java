package obp3.monitoring.inclusion;

import obp3.runtime.sli.DependentSemanticRelation;
import obp3.runtime.sli.DeterministicIOSemanticRelation;
import obp3.runtime.sli.IOSematicRelation;
import obp3.sli.core.operators.product.Product;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class TraceInclusionOperator<I1, O1, A1, C1, A2, C2> implements DeterministicIOSemanticRelation<I1, Boolean, TraceInclusionAction<A1>, Product<C1, Set<C2>>> {
    DeterministicIOSemanticRelation<I1, O1, A1, C1> lhs;
    DependentSemanticRelation<O1, A2, C2> rhs;
    boolean recoverable = false;

    @Override
    public Optional<Product<C1, Set<C2>>> initial() {
        var lhsC = lhs.initial().orElse(null);
        if (lhsC == null) return Optional.empty();
        var rhs = this.rhs.initial();
        return Optional.of(new Product<>(lhsC, Set.copyOf(rhs)));
    }

    @Override
    public Optional<TraceInclusionAction<A1>> actions(I1 input, Product<C1, Set<C2>> configuration) {
        return lhs.actions(input, configuration.l()).map(TraceInclusionAction::new);
    }

    @Override
    public Optional<IOSematicRelation.Pair<Boolean, Product<C1, Set<C2>>>> execute(TraceInclusionAction<A1> action, I1 input, Product<C1, Set<C2>> configuration) {
        if (action == null) return Optional.empty();
        var laction = action.action();
        var lpairO = lhs.execute(laction, input, configuration.l());
        if (lpairO.isEmpty()) return Optional.empty();
        var lpair = lpairO.get();
        var output = lpair.output();
        var ltarget = lpair.configuration();

        var rconfigs = new ArrayList<C2>();
        for (var rconfig : configuration.r()) {
            var ractions = rhs.actions(output, rconfig);
            for (var raction : ractions) {
                var rtargets = rhs.execute(raction, output, rconfig);
                rconfigs.addAll(rtargets);
            }
        }
        //if the specification does not have any targets, then we are outside its behavior
        //output is false
        if (rconfigs.isEmpty()) return
                recoverable ?
                            //if recoverable, then we keep the spec where it is, hoping that later we can re-match a step
                          Optional.of(new IOSematicRelation.Pair<>(false, new Product<>(ltarget, configuration.r())))
                            //not recoverable, then we have hard absortion, we output false always until the end of the trace
                        : Optional.of(new IOSematicRelation.Pair<>(false, new Product<>(ltarget, Set.of())));
        return Optional.of(new IOSematicRelation.Pair<>(true, new Product<>(ltarget, Set.copyOf(rconfigs))));
    }
}
