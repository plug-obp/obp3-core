package fr.ensta;

import fr.ensta.obp3.IRootedGraph;

import java.util.*;

/**  A dictionary-based rooted graph:
 {@snippet :
  * var graph = new HashMap<Integer, Integer[]>() {{
  * put(1, new Integer[]{1, 2});
  * put(2, new Integer[]{2, 3, 1});
  * }};
  * var drg = new DictionaryRootedGraph<>(new Integer[]{2}, graph);
  *}
 */

public class DictionaryRootedGraph<V> implements IRootedGraph<V> {
    V[] roots;
    Map<V, V[]> dictionary;

    public DictionaryRootedGraph(V[] roots, Map<V, V[]> dictionary) {
        this.roots = roots;
        this.dictionary = dictionary;
    }

    @Override
    public Iterator<V> roots() {
        return Arrays.stream(this.roots).iterator();
    }

    @Override
    public Iterator<V> neighbours(V v) {
        var neighbors = this.dictionary.get(v);
        if (neighbors == null) return Collections.emptyIterator();

        return Arrays.stream(neighbors).iterator();
    }

    public final static DictionaryRootedGraph<Integer> example1() {
        var graph = new HashMap<Integer, Integer[]>() {{
            put(1, new Integer[]{1, 2, 4});
            put(2, new Integer[]{3, 2});
            put(3, new Integer[]{1, 3});
            put(4, new Integer[]{2, 5});
            put(5, new Integer[0]);
            put(6, new Integer[]{4});
        }};
        var drg = new DictionaryRootedGraph<>(new Integer[]{1, 6}, graph);
        return drg;
    }
}
