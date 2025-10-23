package obp3.sli.core;

import obp3.sli.core.operators.product.Step;

public interface IStepEvaluator<E, A, C, V> {
    V evaluate(E exp, Step<A, C> step);
}
