package fr.ensta.obp3;

import java.util.Iterator;

public interface IRootedGraph<V> {
    Iterator<V> roots();

    Iterator<V> neighbours(V v);
}
