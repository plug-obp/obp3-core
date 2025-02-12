package fr.ensta;

import fr.ensta.obp3.RootedGraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**  A dictionary-based rooted graph:
    {@snippet :
        var graph = new HashMap<Integer, Integer[]>() {{
            put(1, new Integer[]{1, 2});
            put(2, new Integer[]{2, 3, 1});
        }};
        var drg = new DictionaryRootedGraph<>(new Integer[]{2}, graph);
    }
 */

public class DictionaryRootedGraph<V> implements RootedGraph<V> {
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
}
