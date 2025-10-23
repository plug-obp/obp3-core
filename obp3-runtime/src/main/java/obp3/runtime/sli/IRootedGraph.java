package obp3.runtime.sli;

import java.util.Iterator;

public interface IRootedGraph<V> {
    Iterator<V> roots();

    Iterator<V> neighbours(V v);

    default boolean hasCycles() {
        return true;
    }

    default boolean hasSharing() {
        return true;
    }

    default boolean isTree() {
        return !hasCycles() && !hasSharing();
    }

    default boolean isDag() {
        return !hasCycles() && hasSharing();
    }
}
