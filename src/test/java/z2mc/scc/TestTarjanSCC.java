package z2mc.scc;

import obp3.scc.TarjanConfigurationSCC;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTarjanSCC {

    @Test
    void sharing_reducedVertexOk() {
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

    //test RootedGraphExamples.twoIdenticalRoots
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


}
