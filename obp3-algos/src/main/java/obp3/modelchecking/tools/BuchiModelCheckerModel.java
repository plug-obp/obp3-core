package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.buchi.ndfs.cvwy92.EmptinessCheckerBuchiCVWY92Algo2;
import obp3.modelchecking.buchi.ndfs.gs09.EmptinessCheckerBuchiGS09;
import obp3.modelchecking.buchi.ndfs.gs09.cdlp05.EmptinessCheckerBuchiGS09CDLP05;
import obp3.modelchecking.buchi.ndfs.gs09.cdlp05.separated.EmptinessCheckerBuchiGS09CDLP05Separated;
import obp3.modelchecking.buchi.ndfs.gs09.separated.EmptinessCheckerBuchiGS09Separated;
import obp3.modelchecking.buchi.ndfs.naive.EmptinessChecherBuchiNaiveNDFS;
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
import java.util.function.Predicate;

/**
 * Buchi model checker - requires property semantics and accepting predicate for Buchi acceptance.
 * Requires all four type parameters: MA, MC, PA, PC.
 */
public record BuchiModelCheckerModel<MA, MC, PA, PC>(
        SemanticRelation<MA, MC> modelSemantics,
        BiPredicate<String, Step<MA,MC>> atomicPropositionEvaluator,
        Function<BiPredicate<String, Step<MA,MC>>, DependentSemanticRelation<Step<MA, MC>, PA, PC>> propertySemanticsProvider,
        BiPredicate<Product<MC, PC>, Product<SemanticRelation<MA, MC>, DependentSemanticRelation<Step<MA, MC>, PA, PC>>> acceptingPredicateForProduct,
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
    public IExecutable<?, EmptinessCheckerAnswer<Product<MC, PC>>> modelChecker() {
        BiPredicate<String, Step<MA, MC>> atomEvaluator = (s, step) -> StepSynchronousProductSemantics.evaluateAtom(s, step, this.atomicPropositionEvaluator);
        var propertySemantics = this.propertySemanticsProvider.apply(atomEvaluator);
        var product = new StepSynchronousProductSemantics<>(new StepProductParameters<>(modelSemantics, propertySemantics));
        var rootedGraph = new SemanticRelation2RootedGraph<>(product);
        Predicate<Product<MC, PC>> acceptingPredicate = (c) -> this.acceptingPredicateForProduct.test(c, new Product<>(modelSemantics, propertySemantics));

        switch (this.emptinessCheckerAlgorithm) {
            case NAIVE:
                return new EmptinessChecherBuchiNaiveNDFS<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        acceptingPredicate);
            case GS09:
                return new EmptinessCheckerBuchiGS09<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        acceptingPredicate);
            case GS09_SEPARATED:
                return new EmptinessCheckerBuchiGS09Separated<>(
                    this.traversalStrategy,
                    rootedGraph,
                    this.depthBound,
                    this.reducer,
                    acceptingPredicate);
            case GS09_CDLP05:
                return new EmptinessCheckerBuchiGS09CDLP05<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        acceptingPredicate);
            case GS09_CDLP05_SEPARATED:
                return new EmptinessCheckerBuchiGS09CDLP05Separated<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        acceptingPredicate);
            case CVWY92Algo2:
                return new EmptinessCheckerBuchiCVWY92Algo2<>(
                        this.traversalStrategy,
                        rootedGraph,
                        this.depthBound,
                        this.reducer,
                        acceptingPredicate);
        }
        return null;
    }

    public <MA, MC, PA, PC> BuchiModelCheckerBuilder<MA, MC, PA, PC> builder() {
        return new BuchiModelCheckerBuilder<>();
    }

    public static class BuchiModelCheckerBuilder<MA, MC, PA, PC> extends ModelCheckerBuilderWithPropertyBase<MA, MC, PA, PC> {
        private BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm;

        public BuchiModelCheckerBuilder<MA, MC, PA, PC> emptinessCheckerAlgorithm(BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm) {
            this.emptinessCheckerAlgorithm = emptinessCheckerAlgorithm;
            return this;
        }

        public BuchiModelCheckerModel<MA, MC, PA, PC> build() {
            return new BuchiModelCheckerModel<>(
                    modelSemantics, atomicPropositionEvaluator, propertySemanticsProvider, acceptingPredicateForProduct,
                    emptinessCheckerAlgorithm, traversalStrategy, depthBound, reducer);
        }
    }
}

