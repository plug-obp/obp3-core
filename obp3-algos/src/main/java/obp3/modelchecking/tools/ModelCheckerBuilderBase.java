package obp3.modelchecking.tools;

import obp3.runtime.sli.SemanticRelation;
import obp3.runtime.sli.Step;
import obp3.traversal.dfs.DepthFirstTraversal;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class ModelCheckerBuilderBase<MA, MC, RT, SELF extends ModelCheckerBuilderBase<MA, MC, RT, SELF>> {
    SemanticRelation<MA, MC> modelSemantics;
    Predicate<MC> acceptingPredicate;
    DepthFirstTraversal.Algorithm traversalStrategy;
    int depthBound = -1;
    Function<RT, ?> reducer = Function.identity();

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    public SELF modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
        this.modelSemantics = modelSemantics;
        return self();
    }

    public SELF acceptingPredicate(Predicate<MC> acceptingPredicate) {
        this.acceptingPredicate = acceptingPredicate;
        return self();
    }

    public SELF traversalStrategy(DepthFirstTraversal.Algorithm traversalStrategy) {
        this.traversalStrategy = traversalStrategy;
        return self();
    }


    public SELF depthBound(int bound) {
        this.depthBound = bound;
        return self();
    }

    public SELF unbounded() {
        this.depthBound = -1;
        return self();
    }

    public SELF bounded(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
        }
        this.depthBound = bound;
        return self();
    }

    public SELF reducer(Function<RT, ?> reducer) {
        this.reducer = reducer;
        return self();
    }

    public SELF identityReducer() {
        this.reducer = Function.identity();
        return self();
    }
}
