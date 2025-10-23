package obp3.bench;

import obp3.things.LimitedRandomRootedGraph;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.semantics.DepthFirstTraversalDo;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
public class DFSBench {
    long limit = 10000;
    int width = 30;
    long seed = System.nanoTime();

    LimitedRandomRootedGraph lrg = new LimitedRandomRootedGraph(limit, width, seed);

    @Benchmark
    public void whileDFS(Blackhole blackhole) {
        var dfs = new DepthFirstTraversalWhile<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(lrg, Function.identity())));
        var known = dfs.runAlone();
        blackhole.consume(known);
    }
    @Benchmark
    public void doDFS(Blackhole blackhole) {
        var dfs = new DepthFirstTraversalDo<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(lrg, Function.identity())));
        var known = dfs.runAlone();
        blackhole.consume(known);
    }

    @Benchmark
    public void relationalDFS(Blackhole blackhole) {
        var dfs = new DepthFirstTraversalRelational<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(lrg, Function.identity())));
        var known = dfs.runAlone();
        blackhole.consume(known);
    }
}
