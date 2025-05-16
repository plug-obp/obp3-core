package z2mc.traversal.dft;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDFTWhile {
    <V> IExecutable<Set<V>> simpleDFS(
            IRootedGraph<V> graph,
            Function<IDepthFirstTraversalConfiguration<V, V>, IExecutable<Set<V>>> constructor) {
        return constructor.apply(
                new DFTConfigurationSetDeque<>(
                    new DepthFirstTraversalParameters<>(graph, Function.identity())));
    }

    @Test
    void testEmptyGraphWhile() {
        var dfs = simpleDFS(RootedGraphExamples.emptyGraph, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(0, result.size());
    }

    @Test
    void testEmptyRootOnlyWhile() {
        var dfs = simpleDFS(RootedGraphExamples.emptyRootGraph, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(0, result.size());
    }

    @Test void oneRootEmptyNeighboursGraphWhile() {
        var dfs = simpleDFS(RootedGraphExamples.oneRootEmptyNeighboursGraph, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(1, result.size());
    }

    @Test void twoRootEmptyNeighboursGraphWhile() {
        var dfs = simpleDFS(RootedGraphExamples.twoRootsEmptyNeighboursGraph, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(2, result.size());
    }

    @Test void disconnectedGraph1() {
        var dfs = simpleDFS(RootedGraphExamples.disconnectedGraph1, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(3, result.size());
        assertEquals(Set.of(1, 2, 3), result);
    }

    @Test void disconnectedGraph2() {
        var dfs = simpleDFS(RootedGraphExamples.disconnectedGraph2, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(2, result.size());
        assertEquals(Set.of(4, 5), result);
    }

    @Test void twoRootsTwoGraphs() {
        var dfs = simpleDFS(RootedGraphExamples.twoRootsTwoGraphs, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(5, result.size());
        assertEquals(Set.of(1, 2, 3, 4, 5), result);
    }

    @Test void rootCycle() {
        var dfs = simpleDFS(RootedGraphExamples.rootCycle, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(2, result.size());
        assertEquals(Set.of(1, 2), result);
    }

    @Test void rootCycle3() {
        var dfs = simpleDFS(RootedGraphExamples.rootCycle3, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(3, result.size());
        assertEquals(Set.of(1, 2, 3), result);
    }

    @Test void lasso_1_1() {
        var dfs = simpleDFS(RootedGraphExamples.lasso_1_1, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(2, result.size());
        assertEquals(Set.of(1, 2), result);
    }

    @Test void lasso_2_1() {
        var dfs = simpleDFS(RootedGraphExamples.lasso_2_1, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(3, result.size());
        assertEquals(Set.of(1, 2, 3), result);
    }

    @Test void lasso_1_2() {
        var dfs = simpleDFS(RootedGraphExamples.lasso_1_2, DepthFirstTraversalWhile::new);
        var result = dfs.runAlone();
        assertEquals(3, result.size());
        assertEquals(Set.of(1, 2, 3), result);
    }
}
