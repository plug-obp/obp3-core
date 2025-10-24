package obp3.modelchecking.tools;

import obp3.runtime.sli.SemanticRelation;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Safety model checker without property semantics - only checks model states directly.
 * Only requires MA and MC type parameters.
 */
public record StatePredicateModelCheckerModel<MA, MC>(
        SemanticRelation<MA, MC> modelSemantics,
        Predicate<MC> acceptingPredicate,
        int depthBound,
        Function<MC, ?> reducer) implements ModelChecker {

    public static <MA, MC> SafetyBuilder<MA, MC> builder() {
        return new SafetyBuilder<>();
    }

    public static class SafetyBuilder<MA, MC> {
        private SemanticRelation<MA, MC> modelSemantics;
        private Predicate<MC> acceptingPredicate;
        private int depthBound = -1;
        private Function<MC, MC> reducer = Function.identity();

        public SafetyBuilder<MA, MC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
            this.modelSemantics = modelSemantics;
            return this;
        }

        public SafetyBuilder<MA, MC> acceptingPredicate(Predicate<MC> acceptingPredicate) {
            this.acceptingPredicate = acceptingPredicate;
            return this;
        }

        public SafetyBuilder<MA, MC> unbounded() {
            this.depthBound = -1;
            return this;
        }

        public SafetyBuilder<MA, MC> bounded(int bound) {
            if (bound <= 0) {
                throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
            }
            this.depthBound = bound;
            return this;
        }

        public SafetyBuilder<MA, MC> reducer(Function<MC, MC> reducer) {
            this.reducer = reducer;
            return this;
        }

        public SafetyBuilder<MA, MC> identityReducer() {
            this.reducer = Function.identity();
            return this;
        }

        public StatePredicateModelCheckerModel<MA, MC> build() {
            return new StatePredicateModelCheckerModel<>(modelSemantics, acceptingPredicate, depthBound, reducer);
        }
    }
}

