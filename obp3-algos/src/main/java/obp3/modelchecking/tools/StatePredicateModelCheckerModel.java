package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerExecutable;
import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
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
    public EmptinessCheckerExecutable<MC> modelChecker() {
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
    public static class StatePredicateModelBuilder<MA, MC> extends ModelCheckerBuilderBase<MA, MC, MC, StatePredicateModelBuilder<MA, MC>>{
        public StatePredicateModelCheckerModel<MA, MC> build() {
            return new StatePredicateModelCheckerModel<>(
                    modelSemantics, acceptingPredicate,
                    traversalStrategy, depthBound, reducer);
        }
    }
}

