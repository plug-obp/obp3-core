package fr.ensta.obp3.bench;

import fr.ensta.LimitedRandomGraph;
import fr.ensta.obp3.Sequencer;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearch;
import fr.ensta.obp3.traversal.bfs.BreadthFirstSearchDoFlat;
import fr.ensta.obp3.traversal.bfs.relational.BreadthFirstSearchRelation;
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
        var bfs = new BreadthFirstSearch<>(lrg);
        var known = bfs.run();
        blackhole.consume(known);
    }
    @Benchmark
    public void doBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstSearchDoFlat<>(lrg);
        var known = bfs.run();
        blackhole.consume(known);
    }
    @Benchmark
    public void doFlatBFS(Blackhole blackhole) {
        var bfs = new BreadthFirstSearchDoFlat<>(lrg);
        var known = bfs.run();
        blackhole.consume(known);
    }
    @Benchmark
    public void relationalBFS(Blackhole blackhole) {
        var algo0 = new BreadthFirstSearchRelation<>(lrg);
        var sequencer0 = new Sequencer<>(algo0);
        var configuration = sequencer0.run();
        blackhole.consume(configuration);
    }
}
