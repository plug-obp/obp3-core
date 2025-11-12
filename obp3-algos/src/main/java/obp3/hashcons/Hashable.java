package obp3.hashcons;

import java.util.Objects;

public interface Hashable<T> {
    default boolean equal(T x, T y) {
        return Objects.equals(x, y);
    }
    default int hash(T x) {
        return x.hashCode();
    }
}
