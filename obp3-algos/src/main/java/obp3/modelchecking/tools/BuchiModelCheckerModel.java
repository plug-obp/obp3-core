package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.buchi.ndfs.cvwy92.EmptinessCheckerBuchiCVWY92Algo2;
import obp3.modelchecking.buchi.ndfs.gs09.EmptinessCheckerBuchiGS09;
import obp3.modelchecking.buchi.ndfs.gs09.cdlp05.EmptinessCheckerBuchiGS09CDLP05;
import obp3.modelchecking.buchi.ndfs.gs09.cdlp05.separated.EmptinessCheckerBuchiGS09CDLP05Separated;
import obp3.modelchecking.buchi.ndfs.gs09.separated.EmptinessCheckerBuchiGS09Separated;
import obp3.modelchecking.buchi.ndfs.naive.EmptinessChecherBuchiNaiveNDFS;
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
 * Buchi model checker - requires property semantics and accepting predicate for Buchi acceptance.
 * Requires all four type parameters: MA, MC, PA, PC.
 */
public record BuchiModelCheckerModel<MA, MC, PA, PC>(
        SemanticRelation<MA, MC> modelSemantics,
        DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics,
        Predicate<Product<MC, PC>> acceptingPredicateForProduct,
        BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm,
        DepthFirstTraversal.Algorithm traversalStrategy,
        int depthBound,
        Function<Product<MC, PC>, ?> reducer) implements ModelCheckerModel<Product<MC, PC>> {

    public static enum BuchiEmptinessCheckerAlgorithm {
        NAIVE,
        GS09,
        GS09_SEPARATED,
        GS09_CDLP05,
        GS09_CDLP05_SEPARATED,
        CVWY92Algo2
    }

    @Override
    public IExecutable<EmptinessCheckerAnswer<Product<MC, PC>>> modelChecker() {
        var product = new StepSynchronousProductSemantics<>(new StepProductParameters<>(modelSemantics, propertySemantics));
        var rootedGraph = new SemanticRelation2RootedGraph<>(product);

        switch (this.emptinessCheckerAlgorithm) {
            case NAIVE:
                return new EmptinessChecherBuchiNaiveNDFS<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        this.acceptingPredicateForProduct);
            case GS09:
                return new EmptinessCheckerBuchiGS09<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        this.acceptingPredicateForProduct);
            case GS09_SEPARATED:
                return new EmptinessCheckerBuchiGS09Separated<>(
                    this.traversalStrategy,
                    rootedGraph,
                    this.depthBound,
                    this.reducer,
                    this.acceptingPredicateForProduct);
            case GS09_CDLP05:
                return new EmptinessCheckerBuchiGS09CDLP05<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        this.acceptingPredicateForProduct);
            case GS09_CDLP05_SEPARATED:
                return new EmptinessCheckerBuchiGS09CDLP05Separated<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        this.acceptingPredicateForProduct);
            case CVWY92Algo2:
                return new EmptinessCheckerBuchiCVWY92Algo2<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        this.acceptingPredicateForProduct);
        }
        return null;
    }

    public static <MA, MC, PA, PC> BuchiBuilder<MA, MC, PA, PC> builder() {
        return new BuchiBuilder<>();
    }

    public static class BuchiBuilder<MA, MC, PA, PC> {
        private SemanticRelation<MA, MC> modelSemantics;
        private DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics;
        private Predicate<Product<MC, PC>> acceptingPredicateForProduct;
        BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm;
        DepthFirstTraversal.Algorithm traversalStrategy;
        private int depthBound = -1;
        private Function<Product<MC, PC>, ?> reducer = Function.identity();

        public BuchiBuilder<MA, MC, PA, PC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
            this.modelSemantics = modelSemantics;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> propertySemantics(DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics) {
            this.propertySemantics = propertySemantics;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> acceptingPredicateForProduct(Predicate<Product<MC, PC>> acceptingPredicate) {
            this.acceptingPredicateForProduct = acceptingPredicate;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> emptinessCheckerAlgorithm(BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm) {
            this.emptinessCheckerAlgorithm = emptinessCheckerAlgorithm;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> traversalStrategy(DepthFirstTraversal.Algorithm traversalStrategy) {
            this.traversalStrategy = traversalStrategy;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> unbounded() {
            this.depthBound = -1;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> bounded(int bound) {
            if (bound <= 0) {
                throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
            }
            this.depthBound = bound;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> reducer(Function<Product<MC, PC>, ?> reducer) {
            this.reducer = reducer;
            return this;
        }

        public BuchiBuilder<MA, MC, PA, PC> identityReducer() {
            this.reducer = Function.identity();
            return this;
        }

        public BuchiModelCheckerModel<MA, MC, PA, PC> build() {
            return new BuchiModelCheckerModel<>(
                    modelSemantics, propertySemantics, acceptingPredicateForProduct,
                    emptinessCheckerAlgorithm, traversalStrategy, depthBound, reducer);
        }
    }
}

