package fr.ensta.obp3.bench;

import fr.ensta.LimitedRandomGraph;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearchDoFlat;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearchRelational;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearchWhile;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
public class BFSBench {

    long limit = 10000;
    int width = 30;
    long seed = System.nanoTime();

    LimitedRandomGraph lrg = new LimitedRandomGraph(limit, width, seed);

    @Benchmark
    public void simpleBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstSearchWhile<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void doBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstSearchDoFlat<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void doFlatBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstSearchDoFlat<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void relationalBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstSearchRelational<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
}
