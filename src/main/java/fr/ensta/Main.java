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
        RootedGraph<?> lrg;

        lrg = new LimitedRandomGraph(1000000, 30, seed);
        var simple = simpleBFS(lrg);
        out.println("simple analyzed : " + simple[1] + " configurations in " + simple[0] + " ms");

        lrg = new LimitedRandomGraph(1000000, 30, seed);
        var doB = doBFS(lrg);
        out.println("do analyzed : " + doB[1] + " configurations in " + doB[0] + " ms");

        lrg = new LimitedRandomGraph(1000000, 30, seed);
        var rel = relationalBFS(lrg);
        out.println("rel analyzed : " + rel[1] + " configurations in " + rel[0] + " ms");
    }
}