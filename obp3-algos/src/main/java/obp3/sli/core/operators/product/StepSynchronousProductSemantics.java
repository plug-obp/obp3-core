package obp3.sli.core.operators.product;

import obp3.runtime.sli.SemanticRelation;
import obp3.sli.core.operators.product.model.StepProductParameters;
import obp3.runtime.sli.Step;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class StepSynchronousProductSemantics<LA, LC, RA, RC> implements SemanticRelation<Product<Step<LA, LC>, RA>, Product<LC, RC>> {

    StepProductParameters<LA, LC, RA, RC> model;

    public StepSynchronousProductSemantics(StepProductParameters<LA, LC, RA, RC> model) {
        this.model = model;
    }

    @Override
    public List<Product<LC, RC>> initial() {
        var initials = new ArrayList<Product<LC, RC>>();
        for (LC lc : model.lhs().initial()) {
            for (RC rc : model.rhs().initial()) {
                initials.add(new Product<>(lc, rc));
            }
        }
        return initials;
    }

    @Override
    public List<Product<Step<LA, LC>, RA>> actions(Product<LC, RC> configuration) {
        var actions = new ArrayList<Product<Step<LA, LC>, RA>>();
        var l_actions = model.lhs().actions(configuration.l());
        var n_actions = l_actions.size();
        for (var l_action : l_actions) {
            var l_targets = model.lhs().execute(l_action, configuration.l());
            if (l_targets.isEmpty()) {
                n_actions--;
                continue;
            }
            for (var l_target : l_targets) {
                var l_step = new Step<>(configuration.l(), Optional.of(l_action), l_target);
                var r_actions = model.rhs().actions(l_step, configuration.r());
                r_actions.forEach(r_action -> {
                    actions.add(new Product<>(l_step, r_action));
                });
            }
        }
        //the deadlock case: the kripke does not have a step, add stuttering
        if (n_actions <= 0) {
            var l_step = new Step<>(configuration.l(), Optional.<LA>empty(), configuration.l());
            var r_actions = model.rhs().actions(l_step, configuration.r());
            r_actions.forEach(r_action -> {
                actions.add(new Product<>(l_step, r_action));
            });
        }
        return actions;
    }

    @Override
    public List<Product<LC, RC>> execute(Product<Step<LA, LC>, RA> action, Product<LC, RC> configuration) {
        var l_step = action.l();
        var r_targets = model.rhs().execute(action.r(), l_step, configuration.r());
        return r_targets.stream()
                .map(target -> new Product<>(l_step.end(), target))
                .collect(Collectors.toList());
    }

    public static <LA, LC> boolean evaluateAtom(String atom, Step<LA, LC> step, BiPredicate<String, Step<LA, LC>> baseEvaluator) {
        if (atom.equals("deadlock")) {
            return step.action().isEmpty() && step.end() == step.start();
        }
        return baseEvaluator.test(atom, step);
    }
}

