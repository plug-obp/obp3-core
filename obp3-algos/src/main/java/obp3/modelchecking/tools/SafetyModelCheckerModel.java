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

import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Safety model checker with property semantics - checks product of model and property states.
 * Requires all four type parameters: MA, MC, PA, PC.
 */
public record SafetyModelCheckerModel<MA, MC, PA, PC>(
        SemanticRelation<MA, MC> modelSemantics,
        BiPredicate<String, Step<MA,MC>> atomicPropositionEvaluator,
        Function<BiPredicate<String, Step<MA,MC>>, DependentSemanticRelation<Step<MA, MC>, PA, PC>> propertySemanticsProvider,
        BiPredicate<Product<MC, PC>, Product<SemanticRelation<MA, MC>, DependentSemanticRelation<Step<MA, MC>, PA, PC>>> acceptingPredicateForProduct,
        DepthFirstTraversal.Algorithm traversalStrategy,
        int depthBound,
        Function<Product<MC, PC>, ?> reducer) implements ModelCheckerModel<Product<MC, PC>> {

    @Override
    public IExecutable<EmptinessCheckerAnswer<Product<MC, PC>>> modelChecker() {
        BiPredicate<String, Step<MA, MC>> atomEvaluator = (s, step) -> StepSynchronousProductSemantics.evaluateAtom(s, step, this.atomicPropositionEvaluator);
        var propertySemantics = this.propertySemanticsProvider.apply(atomEvaluator);
        var product = new StepSynchronousProductSemantics<>(new StepProductParameters<>(modelSemantics, propertySemantics));
        var rootedGraph = new SemanticRelation2RootedGraph<>(product);

        return new SafetyDepthFirstTraversal<>(
                this.traversalStrategy,
                rootedGraph,
                this.depthBound,
                this.reducer,
                (c) -> this.acceptingPredicateForProduct.test(c, new Product<>(modelSemantics, propertySemantics)));
    }

    public static <MA, MC, PA, PC> SafetyModelCheckerBuilder<MA, MC, PA, PC> builder() {
        return new SafetyModelCheckerBuilder<>();
    }

    public static class SafetyModelCheckerBuilder<MA, MC, PA, PC> {
        DepthFirstTraversal.Algorithm traversalStrategy;
        private SemanticRelation<MA, MC> modelSemantics;
        private BiPredicate<String, Step<MA,MC>> atomicPropositionEvaluator;
        private Function<BiPredicate<String, Step<MA,MC>>, DependentSemanticRelation<Step<MA, MC>, PA, PC>> propertySemanticsProvider;
        BiPredicate<Product<MC, PC>, Product<SemanticRelation<MA, MC>, DependentSemanticRelation<Step<MA, MC>, PA, PC>>> acceptingPredicateForProduct;
        private int depthBound = -1;
        private Function<Product<MC, PC>, ?> reducer = Function.identity();

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
            this.modelSemantics = modelSemantics;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> atomicPropositionEvaluator(BiPredicate<String, Step<MA,MC>> atomicPropositionEvaluator) {
            this.atomicPropositionEvaluator = atomicPropositionEvaluator;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> propertySemantics(
                Function<BiPredicate<String, Step<MA,MC>>, DependentSemanticRelation<Step<MA, MC>, PA, PC>> propertySemanticsProvider) {
            this.propertySemanticsProvider = propertySemanticsProvider;
            return this;
        }

        public SafetyModelCheckerBuilder<MA, MC, PA, PC> acceptingPredicateForProduct(BiPredicate<Product<MC, PC>, Product<SemanticRelation<MA, MC>, DependentSemanticRelation<Step<MA, MC>, PA, PC>>> acceptingPredicateForProduct) {
            this.acceptingPredicateForProduct = acceptingPredicateForProduct;
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
                    modelSemantics, atomicPropositionEvaluator, propertySemanticsProvider, acceptingPredicateForProduct,
                    traversalStrategy, depthBound, reducer);
        }
    }
}

