package z2mc.traversal.dft;

import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.RootedGraphFunctional;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import org.junit.jupiter.api.Test;
import z2mc.traversal.AbstractTraversalTest;
import z2mc.traversal.RootedGraphExamples;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Depth-first traversal tests: the shared traversal contract plus callbacks, reduction and depth bound. */
abstract class AbstractDFTTest extends AbstractTraversalTest {

    protected abstract DepthFirstTraversal.Algorithm algorithm();

    @Override
    protected Set<?> traverse(IRootedGraph<Integer> graph) {
        return new DepthFirstTraversal<Integer, Integer>(algorithm(), graph).runAlone().getKnown();
    }

    @Override
    protected Set<?> traverseUntilCheck(IRootedGraph<Integer> graph, int terminationCheck) {
        int[] checks = {0};
        return new DepthFirstTraversal<Integer, Integer>(algorithm(), graph)
                .run(_ -> ++checks[0] >= terminationCheck)
                .getKnown();
    }

    @Test void tree4_keepsNoKnownSet() {
        var result = new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.tree4).runAlone();
        assertEquals(0, result.getKnown().size());
    }

    @Test void tree4_entersEveryVertex() {
        var visited = new ArrayList<Integer>();
        var result = new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.tree4,
                FunctionalDFTCallbacksModel.onEntry((_, t, _) -> {
                    visited.add(t);
                    return false;
                })).runAlone();
        assertEquals(0, result.getKnown().size());
        assertEquals(List.of(1, 2, 3, 4), visited);
    }

    @Test void sharing_onKnown() {
        var rediscovered = new ArrayList<Integer>();
        new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.sharing_3,
                FunctionalDFTCallbacksModel.onKnown((_, v, _) -> {
                    rediscovered.add(v);
                    return false;
                })).runAlone();
        assertEquals(List.of(1, 3), rediscovered);
    }

    @Test void sharing_onEntry() {
        var discovered = new ArrayList<Integer>();
        new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.sharing_3,
                FunctionalDFTCallbacksModel.onEntry((_, v, _) -> {
                    discovered.add(v);
                    return false;
                })).runAlone();
        assertEquals(List.of(1, 2, 3, 4, 5), discovered);
    }

    @Test void sharing_onExit() {
        var exited = new ArrayList<Integer>();
        new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.sharing_3,
                FunctionalDFTCallbacksModel.onExit((v, _, _) -> {
                    exited.add(v);
                    return false;
                })).runAlone();
        assertEquals(List.of(3, 2, 5, 4, 1), exited);
    }

    @Test void sharing_reduced() {
        var result = new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.sharing_3,
                (Integer v) -> v % 3).runAlone();
        //the known set contains the reduced vertices, not the graph vertices
        assertEquals(Set.of(0, 1, 2), result.getKnown());
    }

    @Test void sharing_reducedVertexOk() {
        var result = new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.sharing_3,
                (Integer v) -> v % 3,
                FunctionalDFTCallbacksModel.onEntry((_, v, c) -> {
                    var rv = ((DFTConfigurationSetDeque<Integer, Integer>) c).reducedVertex;
                    assertEquals(rv, c.getModel().reduce(v));
                    return false;
                })).runAlone();
        assertEquals(Set.of(0, 1, 2), result.getKnown());
    }

    @Test void twoRootsTwoGraphsDepthBound0() {
        assertEquals(Set.of(), depthBounded(0));
    }

    @Test void twoRootsTwoGraphsDepthBound1() {
        assertEquals(Set.of(1, 4), depthBounded(1));
    }

    @Test void twoRootsTwoGraphsDepthBound2() {
        assertEquals(Set.of(1, 2, 3, 4, 5), depthBounded(2));
    }

    private Set<Integer> depthBounded(int bound) {
        var result = new DepthFirstTraversal<Integer, Integer>(algorithm(), RootedGraphExamples.twoRootsTwoGraphs,
                bound, FunctionalDFTCallbacksModel.none()).runAlone();
        return new HashSet<>((Set<Integer>) (Set<?>) result.getKnown());
    }

    /** Entry callbacks fire exactly once per reachable vertex, over an existing edge (null source for roots). */
    @Test void everySmallGraphEntersEachReachableVertexOnceOverExistingEdges() {
        int n = 3;
        for (int edges = 0; edges < (1 << (n * n)); edges++) {
            var adjacency = new ArrayList<List<Integer>>();
            for (int i = 0; i < n; i++) {
                var out = new ArrayList<Integer>();
                for (int j = 0; j < n; j++) if ((edges & (1 << (n * i + j))) != 0) out.add(j);
                adjacency.add(out);
            }
            for (int rootMask = 0; rootMask < (1 << n); rootMask++) {
                var roots = new ArrayList<Integer>();
                for (int i = 0; i < n; i++) if ((rootMask & (1 << i)) != 0) roots.add(i);
                IRootedGraph<Integer> g = new RootedGraphFunctional<>(roots::iterator, v -> adjacency.get(v).iterator());
                var entered = new ArrayList<Integer>();
                var context = "edges=" + edges + " roots=" + rootMask;
                var result = new DepthFirstTraversal<Integer, Integer>(algorithm(), g,
                        FunctionalDFTCallbacksModel.onEntry((source, target, _) -> {
                            entered.add(target);
                            assertTrue(source == null ? roots.contains(target) : adjacency.get(source).contains(target),
                                    "nonexistent entry edge " + source + "->" + target + " " + context);
                            return false;
                        })).runAlone();
                assertEquals(new HashSet<Object>(result.getKnown()), new HashSet<Object>(entered), context);
                assertEquals(result.getKnown().size(), entered.size(), "entered exactly once " + context);
            }
        }
    }
}
