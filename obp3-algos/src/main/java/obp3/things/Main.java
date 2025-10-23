package obp3.things;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.bfs.BreadthFirstTraversalRelational;
import obp3.traversal.bfs.BreadthFirstTraversalWhile;
import obp3.traversal.bfs.BreadthFirstTraversalDo;
import obp3.traversal.bfs.BreadthFirstTraversalDoFlat;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;
import obp3.traversal.dfs.semantics.DepthFirstTraversalDo;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;

import static java.lang.System.out;

public class Main {

    public static <V> void traversalDFS(IRootedGraph<V> graph,
                                        Function<
                                                IDepthFirstTraversalConfiguration<V, V>,
                                                IExecutable<IDepthFirstTraversalConfiguration<V, V>>
                                                > constructor) {
        var start = Instant.now();

        var exe = constructor.apply(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(graph, Function.identity())));
        var configuration = exe.runAlone();
        var size = configuration.getKnown().size();

        var finish = Instant.now();
        var duration = Duration.between(start, finish).toMillis();

        out.println("\t" + graph + " + " + exe.getClass().getSimpleName() + ": " + size + " configurations in " + duration + " ms");
    }

    public static void limitedRandomTraversalDFS(
            int limit, int width, long seed,
            Function<
                    IDepthFirstTraversalConfiguration<Long, Long>,
                    IExecutable<IDepthFirstTraversalConfiguration<Long, Long>>> constructor) {
        var graph = new LimitedRandomRootedGraph(limit, width, seed);
        traversalDFS(graph, constructor);
    }


    public static <V> void traversal(IRootedGraph<V> graph, Function<IRootedGraph<V>, IExecutable<Set<V>>> constructor) {
        var start = Instant.now();

        var exe = constructor.apply(graph);
        var known = exe.runAlone();
        var size = known.size();

        var finish = Instant.now();
        var duration = Duration.between(start, finish).toMillis();

        out.println("\t" + graph + " + " + exe.getClass().getSimpleName() + ": " + size + " configurations in " + duration + " ms");
    }

    public static void limitedRandomTraversal(int limit, int width, long seed, Function<IRootedGraph<Long>, IExecutable<Set<Long>>> constructor) {
        var graph = new LimitedRandomRootedGraph(limit, width, seed);
        traversal(graph, constructor);
    }

    public static void main(String[] args) {
        var limit = 1000000;
        var width = 30;
        var seed = System.nanoTime();

        traversal(DictionaryRootedGraph.example1(), BreadthFirstTraversalRelational::new);
        traversalDFS(DictionaryRootedGraph.example1(), DepthFirstTraversalRelational::new);

        out.println("- Depth First Traversal");

        limitedRandomTraversalDFS(limit, width, seed, DepthFirstTraversalWhile::new);
        limitedRandomTraversalDFS(limit, width, seed, DepthFirstTraversalRelational::new);
        limitedRandomTraversalDFS(limit, width, seed, DepthFirstTraversalDo::new);

        out.println("- Breadth First Traversal");

        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalRelational::new);
        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalDo::new);
        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalDoFlat::new);
        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalWhile::new);
    }
}