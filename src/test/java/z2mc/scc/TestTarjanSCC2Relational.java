package z2mc.scc;

import obp3.scc.tarjan2.TarjanCallbacks;
import obp3.scc.tarjan2.TarjanMemory;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTarjanSCC2Relational {

    TarjanMemory<Integer> tarjan(IRootedGraph<Integer> graph) {
        TarjanCallbacks<Integer> callbacks = new TarjanCallbacks<>();
        var dfs = new DepthFirstTraversalRelational<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(graph, Function.identity(), callbacks)));
        dfs.runAlone();
        return callbacks.memory;
    }
    @Test void emptyGraph() {
        var result = tarjan(RootedGraphExamples.emptyGraph);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(0, sccs.size());
    }

    @Test void emptyRootGraphOk() {
        var result = tarjan(RootedGraphExamples.emptyRootGraph);

        var sccs = result.getStronglyConnectedComponents();
        assertEquals(0, sccs.size());
    }

    @Test void oneRootEmptyNeighboursGraphOk() {
        var result = tarjan(RootedGraphExamples.oneRootEmptyNeighboursGraph);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1)), sccs);
    }

    @Test
    void sharing2ok() {
        var result = tarjan(RootedGraphExamples.sharing_2);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1, 2, 3, 4, 5)), sccs);
    }

    @Test
    void sharing3ok() {
        var result = tarjan(RootedGraphExamples.sharing_3);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1, 2, 3, 4, 5)), sccs);
    }

    @Test
    void simpleCycleOk() {
        var result = tarjan(RootedGraphExamples.simpleCycle2);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1, 2)), sccs);
    }

    @Test void twoRootsEmptyNeighboursGraphOk() {
        var result = tarjan(RootedGraphExamples.twoRootsEmptyNeighboursGraph);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(Map.of(1, Set.of(1), 2, Set.of(2)), sccs);
    }

    @Test void twoIdenticalRootsOk() {
        var result = tarjan(RootedGraphExamples.twoIdenticalRoots);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1)), sccs);
    }

    @Test void line2() {
        var result = tarjan(RootedGraphExamples.line2);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                    1, Set.of(1),
                    2, Set.of(2))
                , sccs);
    }

    @Test void lasso_1_1() {
        var result = tarjan(RootedGraphExamples.lasso_1_1);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2))
                , sccs);
    }

    @Test void lasso_2_1() {
        var result = tarjan(RootedGraphExamples.lasso_2_1);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(3, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2),
                        3, Set.of(3))
                , sccs);
    }

    @Test void lasso_1_2() {
        var result = tarjan(RootedGraphExamples.lasso_1_2);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2,3))
                , sccs);
    }

    @Test void disconnectedGraph1Ok() {
        var result = tarjan(RootedGraphExamples.disconnectedGraph1);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(3, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2),
                        3, Set.of(3))
                , sccs);
    }

    @Test void disconnectedGraph2Ok() {
        var result = tarjan(RootedGraphExamples.disconnectedGraph2);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                        4, Set.of(4),
                        5, Set.of(5))
                , sccs);
    }

    @Test void twoRootsTwoGraphsOk() {
        var result = tarjan(RootedGraphExamples.twoRootsTwoGraphs);
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(5, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2),
                        3, Set.of(3),
                        4, Set.of(4),
                        5, Set.of(5))
                , sccs);
    }
}
