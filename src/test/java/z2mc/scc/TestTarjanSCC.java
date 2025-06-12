package z2mc.scc;

import obp3.scc.TarjanConfigurationSCC;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTarjanSCC {

    @Test void emptyGraph() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.emptyGraph));

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(0, sccs.size());
    }

    @Test void emptyRootGraphOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.emptyRootGraph));

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(0, sccs.size());
    }

    @Test void oneRootEmptyNeighboursGraphOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.oneRootEmptyNeighboursGraph)
        );
        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1)), sccs);
    }

    @Test
    void sharing2ok() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.sharing_2));

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1, 2, 3, 4, 5)), sccs);
    }

    @Test
    void sharing3ok() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.sharing_3));

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1, 2, 3, 4, 5)), sccs);
    }

    @Test
    void simpleCycleOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.simpleCycle2));
        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1, 2)), sccs);
    }

    @Test void twoRootsEmptyNeighboursGraphOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.twoRootsEmptyNeighboursGraph)
        );
        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(Map.of(1, Set.of(1), 2, Set.of(2)), sccs);
    }

    @Test void twoIdenticalRootsOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.twoIdenticalRoots)
        );
        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(1, sccs.size());
        assertEquals(Map.of(1, Set.of(1)), sccs);
    }

    @Test void line2() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.line2)
        );
        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                    1, Set.of(1),
                    2, Set.of(2))
                , sccs);
    }

    @Test void lasso_1_1() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.lasso_1_1)
        );

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2))
                , sccs);
    }

    @Test void lasso_2_1() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.lasso_2_1)
        );

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
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
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.lasso_1_2)
        );

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                        1, Set.of(1),
                        2, Set.of(2,3))
                , sccs);
    }

    @Test void disconnectedGraph1Ok() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.disconnectedGraph1)
        );

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
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
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.disconnectedGraph2)
        );

        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
        var sccs = result.getStronglyConnectedComponents();
        assertEquals(2, sccs.size());
        assertEquals(
                Map.of(
                        4, Set.of(4),
                        5, Set.of(5))
                , sccs);
    }

    @Test void twoRootsTwoGraphsOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.twoRootsTwoGraphs)
        );
        var result = (TarjanConfigurationSCC<Integer>) dfs.runAlone();
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
