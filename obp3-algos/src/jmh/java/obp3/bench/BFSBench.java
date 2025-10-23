package obp3.bench;

import obp3.things.LimitedRandomRootedGraph;
import obp3.traversal.bfs.BreadthFirstTraversalDoFlat;
import obp3.traversal.bfs.BreadthFirstTraversalRelational;
import obp3.traversal.bfs.BreadthFirstTraversalWhile;
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

    LimitedRandomRootedGraph lrg = new LimitedRandomRootedGraph(limit, width, seed);

    @Benchmark
    public void simpleBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstTraversalWhile<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void doBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstTraversalDoFlat<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void doFlatBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstTraversalDoFlat<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void relationalBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstTraversalRelational<>(lrg);
        var known = bfs.runAlone();
        blackhole.consume(known);
    }
}
