package obp3.sli.core;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class RootedGraphFunctional<V> implements IRootedGraph<V> {
    Supplier<Iterator<V>> rootsSupplier;
    Function<V, Iterator<V>> neighboursFunction;

    public RootedGraphFunctional(Supplier<Iterator<V>> rootsSupplier, Function<V, Iterator<V>> neighboursFunction) {
        this.rootsSupplier = rootsSupplier;
        this.neighboursFunction = neighboursFunction;
    }

    @Override
    public Iterator<V> roots() {
        return rootsSupplier.get();
    }

    @Override
    public Iterator<V> neighbours(V v) {
        return neighboursFunction.apply(v);
    }
}
