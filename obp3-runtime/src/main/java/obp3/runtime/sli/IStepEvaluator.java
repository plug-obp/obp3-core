package obp3.runtime.sli;

public interface IStepEvaluator<E, A, C, V> {
    V evaluate(E exp, Step<A, C> step);
}
