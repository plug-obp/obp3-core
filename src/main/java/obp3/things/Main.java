package obp3.things;

import obp3.IExecutable;
import obp3.IRootedGraph;
import obp3.traversal.bfs.BreadthFirstTraversalRelational;
import obp3.traversal.bfs.BreadthFirstTraversalWhile;
import obp3.traversal.bfs.BreadthFirstTraversalDo;
import obp3.traversal.bfs.BreadthFirstTraversalDoFlat;
import obp3.traversal.dfs.DepthFirstTraversalDo;
import obp3.traversal.dfs.DepthFirstTraversalRelational;
import obp3.traversal.dfs.DepthFirstTraversalWhile;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;

import static java.lang.System.out;

public class Main {

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
        traversal(DictionaryRootedGraph.example1(), DepthFirstTraversalRelational::new);

        out.println("- Depth First Traversal");

        limitedRandomTraversal(limit, width, seed, DepthFirstTraversalWhile::new);
        limitedRandomTraversal(limit, width, seed, DepthFirstTraversalRelational::new);
        limitedRandomTraversal(limit, width, seed, DepthFirstTraversalDo::new);

        out.println("- Breadth First Traversal");

        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalRelational::new);
        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalDo::new);
        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalDoFlat::new);
        limitedRandomTraversal(limit, width, seed, BreadthFirstTraversalWhile::new);
    }
}