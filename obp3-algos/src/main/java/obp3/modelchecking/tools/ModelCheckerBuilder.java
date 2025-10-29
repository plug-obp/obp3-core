package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.DependentSemanticRelation;
import obp3.runtime.sli.SemanticRelation;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.product.Product;
import obp3.traversal.dfs.DepthFirstTraversal;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Unified builder for creating model checker configurations.
 * Can build three types of models:
 * 1. SafetyModelCheckerModel - safety checking without property semantics
 * 2. SafetyWithPropertyModelCheckerModel - safety checking with property semantics
 * 3. BuchiModelCheckerModel - Buchi acceptance checking
 */
public class ModelCheckerBuilder<MA, MC, PA, PC> {
    private SemanticRelation<MA, MC> modelSemantics;
    private DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics;
    private Predicate<MC> acceptingPredicateForModel;
    private Predicate<Product<MC, PC>> acceptingPredicateForProduct;
    BuchiModelCheckerModel.BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm = BuchiModelCheckerModel.BuchiEmptinessCheckerAlgorithm.GS09_CDLP05_SEPARATED;
    private DepthFirstTraversal.Algorithm traversalStrategy = DepthFirstTraversal.Algorithm.WHILE;
    private boolean isBuchi = false;
    private int depthBound = -1;
    private Function reducer = Function.identity();

    public static <MA, MC, PA, PC> ModelCheckerBuilder<MA, MC, PA, PC> builder() {
        return new ModelCheckerBuilder<>();
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
        this.modelSemantics = modelSemantics;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> propertySemantics(DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics) {
        this.propertySemantics = propertySemantics;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> acceptingPredicateForModel(Predicate<MC> acceptingPredicate) {
        this.acceptingPredicateForModel = acceptingPredicate;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> acceptingPredicateForProduct(Predicate<Product<MC, PC>> acceptingPredicate) {
        this.acceptingPredicateForProduct = acceptingPredicate;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> emptinessCheckerAlgorithm(BuchiModelCheckerModel.BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm) {
        this.emptinessCheckerAlgorithm = emptinessCheckerAlgorithm;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> traversalStrategy(DepthFirstTraversal.Algorithm traversalStrategy) {
        this.traversalStrategy = traversalStrategy;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> buchi(boolean isBuchi) {
        this.isBuchi = isBuchi;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> unbounded() {
        this.depthBound = -1;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> bounded(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
        }
        this.depthBound = bound;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> depthBound(int bound) {
        this.depthBound = bound;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> reducer(Function reducer) {
        this.reducer = reducer;
        return this;
    }

    public ModelCheckerBuilder<MA, MC, PA, PC> identityReducer() {
        this.reducer = Function.identity();
        return this;
    }

    /**
     * Build a SafetyModelCheckerModel (no property semantics).
     * Compromise: Type safety is checked at runtime rather than compile time.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public StatePredicateModelCheckerModel<MA, MC> buildSafety() {
        if (propertySemantics != null) {
            throw new IllegalStateException("Cannot build SafetyModelCheckerModel when propertySemantics is set. Use buildSafetyWithProperty() instead.");
        }
        if (acceptingPredicateForModel == null) {
            throw new IllegalStateException("acceptingPredicateForModel is required for SafetyModelCheckerModel");
        }
        return new StatePredicateModelCheckerModel<>(
                modelSemantics,
                acceptingPredicateForModel,
                traversalStrategy,
                depthBound,
                reducer
        );
    }

    /**
     * Build a SafetyWithPropertyModelCheckerModel.
     * Compromise: Type safety is checked at runtime rather than compile time.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SafetyModelCheckerModel<MA, MC, PA, PC> buildSafetyWithProperty() {
        if (propertySemantics == null) {
            throw new IllegalStateException("propertySemantics is required for SafetyWithPropertyModelCheckerModel");
        }
        if (acceptingPredicateForProduct == null) {
            throw new IllegalStateException("acceptingPredicateForProduct is required for SafetyWithPropertyModelCheckerModel");
        }
        if (isBuchi) {
            throw new IllegalStateException("Cannot build SafetyWithPropertyModelCheckerModel when buchi is true. Use buildBuchi() instead.");
        }
        return new SafetyModelCheckerModel<>(
                modelSemantics,
                propertySemantics,
                acceptingPredicateForProduct,
                traversalStrategy,
                depthBound,
                reducer
        );
    }

    /**
     * Build a BuchiModelCheckerModel.
     * Compromise: Type safety is checked at runtime rather than compile time.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BuchiModelCheckerModel<MA, MC, PA, PC> buildBuchi() {
        if (propertySemantics == null) {
            throw new IllegalStateException("propertySemantics is required for BuchiModelCheckerModel");
        }
        if (acceptingPredicateForProduct == null) {
            throw new IllegalStateException("acceptingPredicateForProduct is required for BuchiModelCheckerModel");
        }
        if (!isBuchi) {
            throw new IllegalStateException("buchi must be set to true for BuchiModelCheckerModel. Call buchi(true).");
        }
        return new BuchiModelCheckerModel<>(
                modelSemantics,
                propertySemantics,
                acceptingPredicateForProduct,
                emptinessCheckerAlgorithm,
                traversalStrategy,
                depthBound,
                reducer
        );
    }

    public IExecutable<EmptinessCheckerAnswer<?>> modelChecker() {
        if (modelSemantics == null) {
            throw new IllegalStateException("modelSemantics is required for [Safety|Buchi]ModelChecker");
        }
        if (this.propertySemantics == null) {
            if (this.acceptingPredicateForModel == null) {
                throw new IllegalStateException("acceptingPredicateForModel is required for StatePredicateModelChecker");
            }
            var mcModel = this.buildSafety();
            return (IExecutable)mcModel.modelChecker();
        }
        if (acceptingPredicateForProduct == null) {
            throw new IllegalStateException("acceptingPredicateForProduct is required for [Safety|Buchi]ModelChecker");
        }
        if (isBuchi) {
            var mcModel = this.buildBuchi();
            return (IExecutable)mcModel.modelChecker();
        }
        var mcModel = this.buildSafetyWithProperty();
        return (IExecutable)mcModel.modelChecker();
    }
}
