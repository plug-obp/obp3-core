package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.SemanticRelation;
import obp3.sli.core.operators.SemanticRelation2RootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Safety model checker without property semantics - only checks model states directly.
 * Only requires MA and MC type parameters.
 */
public record StatePredicateModelCheckerModel<MA, MC>(
        SemanticRelation<MA, MC> modelSemantics,
        Predicate<MC> acceptingPredicate,
        DepthFirstTraversal.Algorithm traversalStrategy,
        int depthBound,
        Function<MC, ?> reducer) implements ModelCheckerModel<MC> {

    @Override
    public IExecutable<EmptinessCheckerAnswer<MC>> modelChecker() {
        var rootedGraph = new SemanticRelation2RootedGraph<>(this.modelSemantics);
        return new SafetyDepthFirstTraversal<>(
                this.traversalStrategy,
                rootedGraph,
                this.depthBound,
                this.reducer,
                this.acceptingPredicate);
    }

    public static <MA, MC> StatePredicateModelBuilder<MA, MC> builder() {
        return new StatePredicateModelBuilder<>();
    }

    public static class StatePredicateModelBuilder<MA, MC> {
        private SemanticRelation<MA, MC> modelSemantics;
        private Predicate<MC> acceptingPredicate;
        DepthFirstTraversal.Algorithm traversalStrategy;
        private int depthBound = -1;
        private Function<MC, MC> reducer = Function.identity();

        public StatePredicateModelBuilder<MA, MC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
            this.modelSemantics = modelSemantics;
            return this;
        }

        public StatePredicateModelBuilder<MA, MC> acceptingPredicate(Predicate<MC> acceptingPredicate) {
            this.acceptingPredicate = acceptingPredicate;
            return this;
        }

        public StatePredicateModelBuilder<MA, MC> traversalStrategy(DepthFirstTraversal.Algorithm traversalStrategy) {
            this.traversalStrategy = traversalStrategy;
            return this;
        }

        public StatePredicateModelBuilder<MA, MC> unbounded() {
            this.depthBound = -1;
            return this;
        }

        public StatePredicateModelBuilder<MA, MC> bounded(int bound) {
            if (bound <= 0) {
                throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
            }
            this.depthBound = bound;
            return this;
        }

        public StatePredicateModelBuilder<MA, MC> reducer(Function<MC, MC> reducer) {
            this.reducer = reducer;
            return this;
        }

        public StatePredicateModelBuilder<MA, MC> identityReducer() {
            this.reducer = Function.identity();
            return this;
        }

        public StatePredicateModelCheckerModel<MA, MC> build() {
            return new StatePredicateModelCheckerModel<>(
                    modelSemantics, acceptingPredicate,
                    traversalStrategy, depthBound, reducer);
        }
    }
}

