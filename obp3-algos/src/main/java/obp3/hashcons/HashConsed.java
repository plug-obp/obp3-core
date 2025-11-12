package obp3.hashcons;

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
}

