package z2mc.traversal.dft;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDFTTree {
    <V> IExecutable<IDepthFirstTraversalConfiguration<V, V>> simpleDFS(
            IRootedGraph<V> graph) {
        return new DepthFirstTraversal<>(graph);
    }

    @Test
    void testTree4EmptyKnown() {
        var dfs = simpleDFS(RootedGraphExamples.tree4);
        var result = dfs.runAlone();
        assertEquals(0, result.getKnown().size());
    }

    @Test void twoTree4VisitAll() {
        var visited = new ArrayList<Integer>();
        var dfs = new DepthFirstTraversal<>(
                DepthFirstTraversal.Algorithm.WHILE,
                RootedGraphExamples.tree4,
                FunctionalDFTCallbacksModel.onEntry((_, t, _) -> {
                    visited.add(t);
                    return false;
                }));
        var result = dfs.runAlone();
        assertEquals(0, result.getKnown().size());
        assertEquals(List.of(1, 2, 3, 4), visited);
    }
}
