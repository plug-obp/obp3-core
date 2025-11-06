package obp3.modelchecking.tools;

import obp3.runtime.sli.SemanticRelation;
import obp3.traversal.dfs.DepthFirstTraversal;

import java.util.function.Function;
import java.util.function.Predicate;

public class ModelCheckerBuilderBase<MA, MC, RT> {
    SemanticRelation<MA, MC> modelSemantics;
    Predicate<MC> acceptingPredicate;
    DepthFirstTraversal.Algorithm traversalStrategy;
    int depthBound = -1;
    Function<RT, ?> reducer = Function.identity();

    public ModelCheckerBuilderBase<MA, MC, RT> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
        this.modelSemantics = modelSemantics;
        return this;
    }

    public ModelCheckerBuilderBase<MA, MC, RT> acceptingPredicate(Predicate<MC> acceptingPredicate) {
        this.acceptingPredicate = acceptingPredicate;
        return this;
    }

    public ModelCheckerBuilderBase<MA, MC, RT> traversalStrategy(DepthFirstTraversal.Algorithm traversalStrategy) {
        this.traversalStrategy = traversalStrategy;
        return this;
    }

    public ModelCheckerBuilderBase<MA, MC, RT> unbounded() {
        this.depthBound = -1;
        return this;
    }

    public ModelCheckerBuilderBase<MA, MC, RT> bounded(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
        }
        this.depthBound = bound;
        return this;
    }

    public ModelCheckerBuilderBase<MA, MC, RT> reducer(Function<RT, ?> reducer) {
        this.reducer = reducer;
        return this;
    }

    public ModelCheckerBuilderBase<MA, MC, RT> identityReducer() {
        this.reducer = Function.identity();
        return this;
    }

}
