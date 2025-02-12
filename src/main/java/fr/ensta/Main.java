package fr.ensta;

import fr.ensta.obp3.RootedGraph;
import fr.ensta.obp3.Sequencer;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearch;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearchDo;
import fr.ensta.obp3.traversal.bfs.relational.BreadthFirstSearchRelation;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.System.out;

public class Main {


    public static <I> long timeIt(Consumer<I> consumer, I value) {
        var start = Instant.now();
        consumer.accept(value);
        var finish = Instant.now();
        return Duration.between(start, finish).toMillis();
    }

    public static long [] relationalBFS(RootedGraph<?> graph) {
        var configs = new int[1];
        var time = timeIt((g) -> {
            var algo0 = new BreadthFirstSearchRelation<>(g);
            var sequencer0 = new Sequencer<>(algo0);
            var configuration = sequencer0.run();
            configs[0] = configuration.map(c -> c.known().size()).orElse(0);
        }, graph);

        return new long[] { time, configs[0] };
    }

    public static long [] simpleBFS(RootedGraph<?> graph) {
        var configs = new int[1];
        var time = timeIt((g) -> {
            var bfs = new BreadthFirstSearch<>(g);
            var known = bfs.run();
            configs[0] = known.size();
        }, graph);

        return new long[] { time, configs[0] };
    }

    public static long [] doBFS(RootedGraph<?> graph) {
        var configs = new int[1];
        var time = timeIt((g) -> {
            var bfs = new BreadthFirstSearchDo<>(g);
            var known = bfs.run();
            configs[0] = known.size();
        }, graph);

        return new long[] { time, configs[0] };
    }

    public static void main(String[] args) {
        var seed = System.nanoTime();
        var lrg = new LimitedRandomGraph(1000000, 30, seed);

        var rel = relationalBFS(lrg);
        out.println("rel analyzed : " + rel[1] + " configurations in " + rel[0] + " ms");

        lrg = new LimitedRandomGraph(1000000, 30, seed);
        var simple = simpleBFS(lrg);
        out.println("simple analyzed : " + simple[1] + " configurations in " + simple[0] + " ms");


        var graph = new HashMap<Integer, Integer[]>() {{
            put(1, new Integer[]{1, 2});
            put(2, new Integer[]{2, 3, 1});
        }};
        var drg = new DictionaryRootedGraph<>(new Integer[]{2}, graph);
        rel = relationalBFS(drg);
        out.println("rel analyzed : " + rel[1] + " configurations in " + rel[0] + " ms");

        for (int i = 1; i < 10; i++) {

            int finalI = i * 100000;
            var lamrg = new RootedGraph<Integer>() {

                @Override
                public Iterator<Integer> roots() {
                    return Arrays.stream(new Integer[]{1}).iterator();
                }

                @Override
                public Iterator<Integer> neighbours(Integer o) {
                    if (o > finalI) return Collections.emptyIterator();
                    return Arrays.stream(new Integer[]{o + 1, o + 2}).iterator();
                }
            };

            out.println(finalI);
            rel = relationalBFS(lamrg);
            out.println("rel analyzed : " + rel[1] + " configurations in " + rel[0] + " ms");

            simple = simpleBFS(lamrg);
            out.println("simple analyzed : " + simple[1] + " configurations in " + simple[0] + " ms");
        }
    }
}