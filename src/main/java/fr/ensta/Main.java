package fr.ensta;

import fr.ensta.traversal.bfs.BreadthFirstSearch;
import fr.ensta.traversal.bfs.relational.BreadthFirstSearchRelation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static java.lang.System.out;

class DictionaryRootedGraph<V> implements RootedGraph<V> {
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

public class Main {
    public static void main(String[] args) {

        var graph = new HashMap<Integer, Integer[]>() {{
            put(1, new Integer[]{1, 2});
            put(2, new Integer[]{2, 3, 1});
        }};

        var drg = new DictionaryRootedGraph<>(new Integer[]{2}, graph);

        var algo = new BreadthFirstSearchRelation<>(drg);

        var sequencer = new Sequencer<>(algo);

        var value = sequencer.run();

        out.println(value);

        for (int i = 1; i < 10; i++) {

            int finalI = i;
            var lamrg = new RootedGraph<Integer>() {

                @Override
                public Iterator<Integer> roots() {
                    return Arrays.stream(new Integer[]{1}).iterator();
                }

                @Override
                public Iterator<Integer> neighbours(Integer o) {
                    if (o > finalI * 100000) return Collections.emptyIterator();
                    return Arrays.stream(new Integer[]{o + 1, o + 2}).iterator();
                }
            };

            Instant start = Instant.now();

            algo = new BreadthFirstSearchRelation<>(lamrg);

            sequencer = new Sequencer<>(algo);

            value = sequencer.run();

            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            out.println(i * 100000 + " :---- " + timeElapsed + "ms");


            start = Instant.now();
            var bfs = new BreadthFirstSearch<>(lamrg);
            var kv = bfs.run();
            finish = Instant.now();
            timeElapsed = Duration.between(start, finish).toMillis();
            out.println(i * 100000 + " ---- " + timeElapsed + "ms");

        }
    }
}