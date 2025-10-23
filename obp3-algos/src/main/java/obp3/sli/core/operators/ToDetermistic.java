package obp3.sli.core.operators;

import obp3.runtime.sli.DeterministicSemanticRelation;
import obp3.runtime.sli.SemanticRelation;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

public class ToDetermistic<A, C> implements DeterministicSemanticRelation<A,C> {
    private final SemanticRelation<A, C> operand;
    Policy<A, C> policy;

    public ToDetermistic(SemanticRelation<A, C> operand, Policy<A, C> policy) {
        this.operand = operand;
        this.policy = policy;
    }

    public static <A, C> ToDetermistic<A, C> randomPolicy(SemanticRelation<A, C> relation) {
        return ToDetermistic.randomPolicy(relation, System.nanoTime());
    }

    public static <A, C> ToDetermistic<A, C> randomPolicy(SemanticRelation<A, C> relation, long seed) {
        return new ToDetermistic<>(relation, new RandomPolicy<>(seed));
    }

    public static <A, C> ToDetermistic<A, C> firstPolicy(SemanticRelation<A, C> relation) {
        return new ToDetermistic<>(relation, new FunctionalPolicy<>(ToDetermistic::firstPolicy, ToDetermistic::firstPolicy));
    }

    public static <A, C> ToDetermistic<A, C> lastPolicy(SemanticRelation<A, C> relation) {
        return new ToDetermistic<>(relation, new FunctionalPolicy<>(ToDetermistic::lastPolicy, ToDetermistic::lastPolicy));
    }

    public static <A, C> ToDetermistic<A, C> anyPolicy(SemanticRelation<A, C> relation) {
        return new ToDetermistic<>(relation, new FunctionalPolicy<>(ToDetermistic::anyPolicy, ToDetermistic::anyPolicy));
    }

    @Override
    public Optional<C> initial() {
        return policy.chooseConfiguration(operand.initial());
    }

    @Override
    public Optional<A> actions(C configuration) {
        return policy.chooseAction(operand.actions(configuration));
    }

    @Override
    public Optional<C> execute(A action, C configuration) {
        return policy.chooseConfiguration(operand.execute(action, configuration));
    }

    @Override
    public boolean actionsIsPure() {
        return DeterministicSemanticRelation.super.actionsIsPure();
    }


    public interface Policy<A, C> {
        Optional<A> chooseAction(List<A> actions);
        Optional<C> chooseConfiguration(List<C> configurations);
    }

    public static class RandomPolicy<A, C> implements Policy<A, C> {
        Random random;
        public RandomPolicy(long seed) {
            this.random = new Random(seed);
        }
        @Override
        public Optional<A> chooseAction(List<A> actions) {
            return randomPolicy(actions);
        }

        @Override
        public Optional<C> chooseConfiguration(List<C> configurations) {
            return randomPolicy(configurations);
        }

        public <X> Optional<X> randomPolicy(List<X> list) {
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(random.nextInt(list.size())));
        }
    }

    public static class FunctionalPolicy<A, C> implements Policy<A, C> {
        Function<List<A>, Optional<A>> actionPolicy;
        Function<List<C>, Optional<C>> configurationPolicy;

        public FunctionalPolicy(Function<List<A>, Optional<A>> actionPolicy, Function<List<C>, Optional<C>> configurationPolicy) {
            this.actionPolicy = actionPolicy;
            this.configurationPolicy = configurationPolicy;
        }

        @Override
        public Optional<A> chooseAction(List<A> actions) {
            return actionPolicy.apply(actions);
        }

        @Override
        public Optional<C> chooseConfiguration(List<C> configurations) {
            return configurationPolicy.apply(configurations);
        }
    }

    public static <X> Optional<X> firstPolicy(List<X> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    public static <X> Optional<X> lastPolicy(List<X> list) {
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getLast());
    }

    public static <X> Optional<X> anyPolicy(List<X> list) {
        return list.isEmpty() ? Optional.empty() : list.stream().findAny();
    }
}
