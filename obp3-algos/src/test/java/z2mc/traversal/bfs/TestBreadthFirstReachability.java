package z2mc.traversal.bfs;

import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;
import obp3.traversal.bfs.BreadthFirstTraversalDo;
import obp3.traversal.bfs.BreadthFirstTraversalDoFlat;
import obp3.traversal.bfs.BreadthFirstTraversalRelational;
import obp3.traversal.bfs.BreadthFirstTraversalWhile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exhaustive check on all directed graphs over three vertices (self-loops included) and all
 * root subsets. The expected set comes from a transitive closure computed here, independently
 * of the traversal code.
 */
class TestBreadthFirstReachability {

    private static final int N = 3;

    private static void forAllGraphsAndRoots(Function<IRootedGraph<Integer>, Set<Integer>> variant, String name) {
        for (int edges = 0; edges < (1 << (N * N)); edges++) {
            var adjacency = new ArrayList<List<Integer>>();
            boolean[][] reach = new boolean[N][N];
            for (int i = 0; i < N; i++) {
                var out = new ArrayList<Integer>();
                for (int j = 0; j < N; j++) {
                    if ((edges & (1 << (N * i + j))) != 0) {
                        out.add(j);
                        reach[i][j] = true;
                    }
                }
                reach[i][i] = true;
                adjacency.add(out);
            }
            for (int k = 0; k < N; k++)
                for (int i = 0; i < N; i++)
                    for (int j = 0; j < N; j++)
                        reach[i][j] |= reach[i][k] && reach[k][j];

            for (int roots = 0; roots < (1 << N); roots++) {
                var initial = new ArrayList<Integer>();
                var expected = new HashSet<Integer>();
                for (int i = 0; i < N; i++) {
                    if ((roots & (1 << i)) == 0) continue;
                    initial.add(i);
                    for (int j = 0; j < N; j++) if (reach[i][j]) expected.add(j);
                }
                IRootedGraph<Integer> g = new RootedGraphFunctional<>(initial::iterator, v -> adjacency.get(v).iterator());
                final int e = edges, r = roots;
                assertEquals(expected, variant.apply(g), () -> name + " edges=" + e + " roots=" + r);
            }
        }
    }

    @Test
    void whileVariantMatchesClosure() {
        forAllGraphsAndRoots(g -> new BreadthFirstTraversalWhile<>(g).runAlone(), "While");
    }

    @Test
    void doVariantMatchesClosure() {
        forAllGraphsAndRoots(g -> new BreadthFirstTraversalDo<>(g).runAlone(), "Do");
    }

    @Test
    void doFlatVariantMatchesClosure() {
        forAllGraphsAndRoots(g -> new BreadthFirstTraversalDoFlat<>(g).runAlone(), "DoFlat");
    }

    @Test
    void relationalVariantMatchesClosure() {
        forAllGraphsAndRoots(g -> new BreadthFirstTraversalRelational<>(g).runAlone(), "Relational");
    }
}
