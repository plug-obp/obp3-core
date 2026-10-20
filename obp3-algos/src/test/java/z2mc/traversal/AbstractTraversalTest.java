package z2mc.traversal;

import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract shared by every breadth-first (BFT) and depth-first (DFT) traversal variant:
 * a complete run computes exactly the vertices reachable from the roots, expands each
 * reachable vertex once, and a termination request returns a partial result.
 * The expected values come from hand-written examples and from a transitive closure
 * computed in this class, independently of the traversal code.
 * Subclasses only say how to run their variant; none of the tests uses a reducer.
 * Graphs declared as trees are left to the subclasses: a depth-first traversal keeps no known set for them.
 */
public abstract class AbstractTraversalTest {

    private static final int N = 3;

    /** Runs the traversal to completion and returns the known vertices. */
    protected abstract Set<?> traverse(IRootedGraph<Integer> graph);

    /** Runs the traversal, requesting termination at the given (1-based) termination check. */
    protected abstract Set<?> traverseUntilCheck(IRootedGraph<Integer> graph, int terminationCheck);

    private record Example(String name, IRootedGraph<Integer> graph, Set<Integer> reachable) {}

    private static List<Example> examples() {
        return List.of(
                new Example("emptyGraph", RootedGraphExamples.emptyGraph, Set.of()),
                new Example("emptyRootGraph", RootedGraphExamples.emptyRootGraph, Set.of()),
                new Example("oneRootEmptyNeighboursGraph", RootedGraphExamples.oneRootEmptyNeighboursGraph, Set.of(1)),
                new Example("twoRootsEmptyNeighboursGraph", RootedGraphExamples.twoRootsEmptyNeighboursGraph, Set.of(1, 2)),
                new Example("twoIdenticalRoots", RootedGraphExamples.twoIdenticalRoots, Set.of(1)),
                new Example("line2", RootedGraphExamples.line2, Set.of(1, 2)),
                new Example("disconnectedGraph1", RootedGraphExamples.disconnectedGraph1, Set.of(1, 2, 3)),
                new Example("disconnectedGraph2", RootedGraphExamples.disconnectedGraph2, Set.of(4, 5)),
                new Example("twoRootsTwoGraphs", RootedGraphExamples.twoRootsTwoGraphs, Set.of(1, 2, 3, 4, 5)),
                new Example("rootCycle", RootedGraphExamples.rootCycle, Set.of(1, 2)),
                new Example("rootCycle3", RootedGraphExamples.rootCycle3, Set.of(1, 2, 3)),
                new Example("simpleCycle2", RootedGraphExamples.simpleCycle2, Set.of(1, 2)),
                new Example("lasso_1_1", RootedGraphExamples.lasso_1_1, Set.of(1, 2)),
                new Example("lasso_2_1", RootedGraphExamples.lasso_2_1, Set.of(1, 2, 3)),
                new Example("lasso_1_2", RootedGraphExamples.lasso_1_2, Set.of(1, 2, 3)),
                new Example("lasso_1_3", RootedGraphExamples.lasso_1_3, Set.of(1, 2, 3, 4)),
                new Example("sharing_2", RootedGraphExamples.sharing_2, Set.of(1, 2, 3, 4, 5)),
                new Example("sharing_3", RootedGraphExamples.sharing_3, Set.of(1, 2, 3, 4, 5))
        );
    }

    @TestFactory
    Stream<DynamicTest> exampleGraphsReachSet() {
        return examples().stream().map(e -> DynamicTest.dynamicTest(e.name(),
                () -> assertEquals(e.reachable(), new HashSet<Object>(traverse(e.graph())))));
    }

    /** A directed graph over {0..N-1} given by an adjacency bitmask, with its reachability closure. */
    private record SmallGraph(List<List<Integer>> adjacency, boolean[][] reach) {
        static SmallGraph of(int edges) {
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
            return new SmallGraph(adjacency, reach);
        }

        List<Integer> rootsOf(int mask) {
            return IntStream.range(0, N).filter(i -> (mask & (1 << i)) != 0).boxed().toList();
        }

        Set<Integer> reachableFrom(List<Integer> roots) {
            var result = new HashSet<Integer>();
            for (int r : roots) for (int j = 0; j < N; j++) if (reach[r][j]) result.add(j);
            return result;
        }
    }

    /** All directed graphs over three vertices (self-loops included) times all root subsets. */
    @Test
    void everySmallGraphReachesItsClosure() {
        for (int edges = 0; edges < (1 << (N * N)); edges++) {
            var sg = SmallGraph.of(edges);
            for (int rootMask = 0; rootMask < (1 << N); rootMask++) {
                var roots = sg.rootsOf(rootMask);
                IRootedGraph<Integer> g = new RootedGraphFunctional<>(roots::iterator, v -> sg.adjacency().get(v).iterator());
                assertEquals(sg.reachableFrom(roots), new HashSet<Object>(traverse(g)),
                        "edges=" + edges + " roots=" + rootMask);
            }
        }
    }

    /** Successors are enumerated exactly once for each reachable vertex and never for the others. */
    @Test
    void everyReachableVertexIsExpandedOnce() {
        for (int edges = 0; edges < (1 << (N * N)); edges++) {
            var sg = SmallGraph.of(edges);
            for (int rootMask = 0; rootMask < (1 << N); rootMask++) {
                var roots = sg.rootsOf(rootMask);
                var expansions = new HashMap<Integer, Integer>();
                IRootedGraph<Integer> g = new RootedGraphFunctional<>(roots::iterator, v -> {
                    expansions.merge(v, 1, Integer::sum);
                    return sg.adjacency().get(v).iterator();
                });
                var reachable = sg.reachableFrom(roots);
                traverse(g);
                for (int v = 0; v < N; v++) {
                    assertEquals(reachable.contains(v) ? 1 : 0, expansions.getOrDefault(v, 0),
                            "vertex=" + v + " edges=" + edges + " roots=" + rootMask);
                }
            }
        }
    }

    @Test
    void terminationReturnsPartialResultOfTheReachableSet() {
        var graph = RootedGraphExamples.sharing_3;
        var all = Set.of(1, 2, 3, 4, 5);
        var previous = -1;
        for (int check = 1; check <= 200; check++) {
            var known = new HashSet<Object>(traverseUntilCheck(graph, check));
            assertTrue(all.containsAll(known), "check=" + check + " known=" + known);
            assertTrue(known.size() >= previous, "known must not shrink as the run gets longer, check=" + check);
            previous = known.size();
        }
        assertEquals(all, new HashSet<Object>(traverseUntilCheck(graph, Integer.MAX_VALUE)));
    }

    @Test
    void terminationAtFirstCheckStopsBeforeCompletion() {
        assertTrue(traverseUntilCheck(RootedGraphExamples.sharing_3, 1).size() < 5);
    }

    @Test
    void longChainIsTraversedCompletely() {
        int length = 20_000;
        IRootedGraph<Integer> chain = new RootedGraphFunctional<>(
                () -> List.of(0).iterator(),
                v -> v + 1 < length ? List.of(v + 1).iterator() : List.<Integer>of().iterator());
        assertEquals(length, traverse(chain).size());
    }
}
