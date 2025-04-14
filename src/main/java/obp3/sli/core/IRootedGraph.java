package obp3.sli.core;

import java.util.Iterator;

public interface IRootedGraph<V> {
    Iterator<V> roots();

    Iterator<V> neighbours(V v);
}
