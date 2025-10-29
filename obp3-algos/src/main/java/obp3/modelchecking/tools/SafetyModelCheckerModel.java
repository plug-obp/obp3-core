package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.DependentSemanticRelation;
import obp3.runtime.sli.SemanticRelation;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.SemanticRelation2RootedGraph;
import obp3.sli.core.operators.product.Product;
import obp3.sli.core.operators.product.StepSynchronousProductSemantics;
import obp3.sli.core.operators.product.model.StepProductParameters;
import obp3.traversal.dfs.DepthFirstTraversal;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Safety model checker with property semantics - checks product of model and property states.
 * Requires all four type parameters: MA, MC, PA, PC.
 */
public record SafetyModelCheckerModel<MA, MC, PA, PC>(
        SemanticRelation<MA, MC> modelSemantics,
        DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics,
        Predicate<Product<MC, PC>> acceptingPredicateForProduct,
        DepthFirstTraversal.Algorithm traversalStrategy,
        int depthBound,
        Function<Product<MC, PC>, ?> reducer) implements ModelCheckerModel<Product<MC, PC>> {

    @Override
    public IExecutable<EmptinessCheckerAnswer<Product<MC, PC>>> modelChecker() {
        var product = new StepSynchronousProductSemantics<>(new StepProductParameters<>(modelSemantics, propertySemantics));
        var rootedGraph = new SemanticRelation2RootedGraph<>(product);

        return new SafetyDepthFirstTraversal<>(
                this.traversalStrategy,
                rootedGraph,
                this.depthBound,
                this.reducer,
                this.acceptingPredicateForProduct);
    }

    public static <MA, MC, PA, PC> SafetyModelCheckerBuilder<MA, MC, PA, PC> builder() {
        return new SafetyModelCheckerBuilder<>();
    }

    public static class SafetyModelCheckerBuilder<MA, MC, PA, PC> {
        DepthFirstTraversal.Algorithm traversalStrategy;
        private SemanticRelation<MA, MC> modelSemantics;
        private DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics;
        private Predicate<Product<MC, PC>> acceptingPredicateForProduct;
        private int depthBound = -1;
        private Function<Product<MC, PC>, ?> reducer = Function.identity();

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
            this.modelSemantics = modelSemantics;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> propertySemantics(DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics) {
            this.propertySemantics = propertySemantics;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> acceptingPredicateForProduct(Predicate<Product<MC, PC>> acceptingPredicate) {
            this.acceptingPredicateForProduct = acceptingPredicate;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> traversalStrategy(DepthFirstTraversal.Algorithm traversalStrategy) {
            this.traversalStrategy = traversalStrategy;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> unbounded() {
            this.depthBound = -1;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> bounded(int bound) {
            if (bound <= 0) {
                throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
            }
            this.depthBound = bound;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> reducer(Function<Product<MC, PC>, ?> reducer) {
            this.reducer = reducer;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> identityReducer() {
            this.reducer = Function.identity();
            return this;
        }

        public SafetyModelCheckerModel<MA, MC, PA, PC> build() {
            return new SafetyModelCheckerModel<>(
                    modelSemantics, propertySemantics, acceptingPredicateForProduct,
                    traversalStrategy, depthBound, reducer);
        }
    }
}

