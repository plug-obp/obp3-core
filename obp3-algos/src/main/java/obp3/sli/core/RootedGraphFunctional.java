package obp3.sli.core;

import obp3.runtime.sli.IRootedGraph;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class RootedGraphFunctional<V> implements IRootedGraph<V> {
    Supplier<Iterator<V>> rootsSupplier;
    Function<V, Iterator<V>> neighboursFunction;
    boolean hasCycles;
    boolean hasSharing;

    public RootedGraphFunctional(Supplier<Iterator<V>> rootsSupplier, Function<V, Iterator<V>> neighboursFunction) {
        this(rootsSupplier, neighboursFunction, true, true);
    }

    public RootedGraphFunctional(
            Supplier<Iterator<V>> rootsSupplier,
            Function<V, Iterator<V>> neighboursFunction,
            boolean hasCycles, boolean hasSharing) {
        this.rootsSupplier = rootsSupplier;
        this.neighboursFunction = neighboursFunction;
        this.hasCycles = hasCycles;
        this.hasSharing = hasSharing;
    }

    @Override
    public Iterator<V> roots() {
        return rootsSupplier.get();
    }

    @Override
    public Iterator<V> neighbours(V v) {
        return neighboursFunction.apply(v);
    }

    @Override
    public boolean hasCycles() {
        return hasCycles;
    }

    @Override
    public boolean hasSharing() {
        return hasSharing;
    }
}
