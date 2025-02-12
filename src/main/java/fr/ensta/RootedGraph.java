package fr.ensta;

import java.util.Iterator;

public interface RootedGraph<V> {
    Iterator<V> roots();

    Iterator<V> neighbours(V v);
}
