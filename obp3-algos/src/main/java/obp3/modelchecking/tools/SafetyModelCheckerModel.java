package obp3.modelchecking.tools;

import obp3.runtime.sli.DependentSemanticRelation;
import obp3.runtime.sli.SemanticRelation;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.product.Product;

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
        int depthBound,
        Function<Product<MC, PC>, ?> reducer) implements ModelChecker {

    public static <MA, MC, PA, PC> SafetyWithPropertyBuilder<MA, MC, PA, PC> builder() {
        return new SafetyWithPropertyBuilder<>();
    }

    public static class SafetyWithPropertyBuilder<MA, MC, PA, PC> {
        private SemanticRelation<MA, MC> modelSemantics;
        private DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics;
        private Predicate<Product<MC, PC>> acceptingPredicateForProduct;
        private int depthBound = -1;
        private Function<Product<MC, PC>, ?> reducer = Function.identity();

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> modelSemantics(SemanticRelation<MA, MC> modelSemantics) {
            this.modelSemantics = modelSemantics;
            return this;
        }

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> propertySemantics(DependentSemanticRelation<Step<MA, MC>, PA, PC> propertySemantics) {
            this.propertySemantics = propertySemantics;
            return this;
        }

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> acceptingPredicateForProduct(Predicate<Product<MC, PC>> acceptingPredicate) {
            this.acceptingPredicateForProduct = acceptingPredicate;
            return this;
        }

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> unbounded() {
            this.depthBound = -1;
            return this;
        }

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> bounded(int bound) {
            if (bound <= 0) {
                throw new IllegalArgumentException("Bound must be greater than 0, got: " + bound);
            }
            this.depthBound = bound;
            return this;
        }

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> reducer(Function<Product<MC, PC>, ?> reducer) {
            this.reducer = reducer;
            return this;
        }

        public SafetyWithPropertyBuilder<MA, MC, PA, PC> identityReducer() {
            this.reducer = Function.identity();
            return this;
        }

        public SafetyModelCheckerModel<MA, MC, PA, PC> build() {
            return new SafetyModelCheckerModel<>(modelSemantics, propertySemantics,
                    acceptingPredicateForProduct, depthBound, reducer);
        }
    }
}

