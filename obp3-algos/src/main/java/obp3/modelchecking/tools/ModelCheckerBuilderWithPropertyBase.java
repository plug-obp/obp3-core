package obp3.modelchecking.tools;

import obp3.runtime.sli.DependentSemanticRelation;
import obp3.runtime.sli.SemanticRelation;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.product.Product;

import java.util.function.BiPredicate;
import java.util.function.Function;

public class ModelCheckerBuilderWithPropertyBase<MA, MC, PA, PC> extends ModelCheckerBuilderBase<MA, MC, Product<MC, PC>> {
    BiPredicate<String, Step<MA, MC>> atomicPropositionEvaluator;
    Function<BiPredicate<String, Step<MA, MC>>, DependentSemanticRelation<Step<MA, MC>, PA, PC>> propertySemanticsProvider;
    BiPredicate<Product<MC, PC>, Product<SemanticRelation<MA, MC>, DependentSemanticRelation<Step<MA, MC>, PA, PC>>> acceptingPredicateForProduct;

    public ModelCheckerBuilderWithPropertyBase<MA, MC, PA, PC> atomicPropositionEvaluator(BiPredicate<String, Step<MA, MC>> atomicPropositionEvaluator) {
        this.atomicPropositionEvaluator = atomicPropositionEvaluator;
        return this;
    }

    public ModelCheckerBuilderWithPropertyBase<MA, MC, PA, PC> propertySemantics(
            Function<BiPredicate<String, Step<MA, MC>>, DependentSemanticRelation<Step<MA, MC>, PA, PC>> propertySemanticsProvider) {
        this.propertySemanticsProvider = propertySemanticsProvider;
        return this;
    }

    public ModelCheckerBuilderWithPropertyBase<MA, MC, PA, PC> acceptingPredicateForProduct(BiPredicate<Product<MC, PC>, Product<SemanticRelation<MA, MC>, DependentSemanticRelation<Step<MA, MC>, PA, PC>>> acceptingPredicateForProduct) {
        this.acceptingPredicateForProduct = acceptingPredicateForProduct;
        return this;
    }
}
