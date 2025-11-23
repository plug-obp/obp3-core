package obp3.hashcons;

import obp3.utils.Hashable;

import java.util.function.Supplier;

public interface HashConsed<T> {
    T node();
    int tag();
    int hashKey();
    default boolean isHashConsed() {
        return true;
    }

    record FunctionalHashConsed<T>(
            Supplier<T> nodeSupplier,
            Supplier<Integer> tagSupplier,
            Supplier<Integer> hashKeySupplier) implements HashConsed<T> {

        @Override
        public T node() {
            return nodeSupplier.get();
        }

        @Override
        public int tag() {
            return tagSupplier.get();
        }

        @Override
        public int hashKey() {
            return hashKeySupplier.get();
        }
    }

    static <T> boolean equalityFunction (T a, T b) {
        return a == b || ((a instanceof HashConsed<?> ahc && (b instanceof HashConsed<?> bhc)) ?
                ahc.tag() == bhc.tag() :
                a.equals(b));
    }
    static <T> int hashCodeFunction(T o) {
        return o instanceof HashConsed<?> hc ? hc.hashKey() : o.hashCode();
    }

    static <T> Hashable<T> hashable() {
        return Hashable.from(HashConsed::equalityFunction, HashConsed::hashCodeFunction);
    }
}

