package z2mc.traversal.dft;

import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exhaustive check on all directed graphs over three vertices (self-loops included) and all
 * root subsets, for every DFS algorithm. The expected set comes from a transitive closure
 * computed here, independently of the traversal code. The entry callback must fire exactly
 * once per reachable vertex, and every reported entry edge must exist in the graph.
 */
class TestDFTReachability {

    private static final int N = 3;

    private static void check(DepthFirstTraversal.Algorithm algorithm) {
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

                var entered = new ArrayList<Integer>();
                var badEdges = new ArrayList<String>();
                var callbacks = FunctionalDFTCallbacksModel.<Integer, Integer>onEntry((source, target, c) -> {
                    entered.add(target);
                    if (source != null && !adjacency.get(source).contains(target)) {
                        badEdges.add(source + "->" + target);
                    }
                    return false;
                });
                var result = new DepthFirstTraversal<Integer, Integer>(algorithm, g, callbacks).runAlone();

                var context = algorithm + " edges=" + edges + " roots=" + roots;
                assertEquals(expected, new HashSet<Object>(result.getKnown()), context);
                assertEquals(expected, new HashSet<>(entered), context + " entered");
                assertEquals(expected.size(), entered.size(), context + " entered exactly once");
                assertTrue(badEdges.isEmpty(), context + " nonexistent entry edges " + badEdges);
            }
        }
    }

    @Test
    void whileVariantMatchesClosure() {
        check(DepthFirstTraversal.Algorithm.WHILE);
    }

    @Test
    void doVariantMatchesClosure() {
        check(DepthFirstTraversal.Algorithm.DO);
    }

    @Test
    void relationalVariantMatchesClosure() {
        check(DepthFirstTraversal.Algorithm.RELATIONAL);
    }
}
