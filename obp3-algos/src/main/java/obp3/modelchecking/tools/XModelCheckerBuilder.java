package obp3.modelchecking.tools;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.EmptinessCheckerStatus;
import obp3.runtime.IExecutable;
import obp3.sli.core.operators.product.Product;

public class XModelCheckerBuilder<MA, MC, PA, PC> extends ModelCheckerBuilderWithPropertyBase<MA, MC, PA, PC, XModelCheckerBuilder<MA, MC, PA, PC>> {
    private BuchiModelCheckerModel.BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm;
    private boolean isBuchi;

    public XModelCheckerBuilder<MA, MC, PA, PC> emptinessCheckerAlgorithm(BuchiModelCheckerModel.BuchiEmptinessCheckerAlgorithm emptinessCheckerAlgorithm) {
        this.emptinessCheckerAlgorithm = emptinessCheckerAlgorithm;
        return this;
    }

    public XModelCheckerBuilder<MA, MC, PA, PC> buchi(boolean isBuchi) {
        this.isBuchi = isBuchi;
        return this;
    }

    public IExecutable<EmptinessCheckerStatus, EmptinessCheckerAnswer<Product<MC, PC>>> modelChecker() {
        if (modelSemantics == null) {
            throw new IllegalStateException("modelSemantics is required for [Safety|Buchi]ModelChecker");
        }
        if (propertySemanticsProvider == null) {
            throw new IllegalStateException("propertySemantics is required for [Safety|Buchi]ModelChecker");
        }
        if (acceptingPredicateForProduct == null) {
            throw new IllegalStateException("acceptingPredicateForProduct is required for [Safety|Buchi]ModelChecker");
        }

        if (isBuchi) {
            return new BuchiModelCheckerModel.BuchiModelCheckerBuilder<MA, MC, PA, PC>()
                    .modelSemantics(modelSemantics)
                    .atomicPropositionEvaluator(atomicPropositionEvaluator)
                    .propertySemantics(propertySemanticsProvider)
                    .acceptingPredicateForProduct(acceptingPredicateForProduct)
                    .emptinessCheckerAlgorithm(emptinessCheckerAlgorithm)
                    .traversalStrategy(traversalStrategy)
                    .depthBound(depthBound)
                    .reducer(reducer)
                    .build()
                    .modelChecker();
        }
        return new SafetyModelCheckerModel.SafetyModelCheckerBuilder<MA, MC, PA, PC>()
                .modelSemantics(modelSemantics)
                .atomicPropositionEvaluator(atomicPropositionEvaluator)
                .propertySemantics(propertySemanticsProvider)
                .acceptingPredicateForProduct(acceptingPredicateForProduct)
                .traversalStrategy(traversalStrategy)
                .depthBound(depthBound)
                .reducer(reducer)
                .build()
                .modelChecker();
    }

}
