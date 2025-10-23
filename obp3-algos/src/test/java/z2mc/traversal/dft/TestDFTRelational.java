package z2mc.traversal.dft;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDFTRelational {
    <V> IExecutable<IDepthFirstTraversalConfiguration<V, V>> simpleDFS(
            IRootedGraph<V> graph) {
        return new DepthFirstTraversal<>(DepthFirstTraversal.Algorithm.RELATIONAL, graph);
    }

    @Test
    void testEmptyGraphWhile() {
        var dfs = simpleDFS(RootedGraphExamples.emptyGraph);
        var result = dfs.runAlone();
        assertEquals(0, result.getKnown().size());
    }

    @Test
    void testEmptyRootOnlyWhile() {
        var dfs = simpleDFS(RootedGraphExamples.emptyRootGraph);
        var result = dfs.runAlone();
        assertEquals(0, result.getKnown().size());
    }

    @Test void oneRootEmptyNeighboursGraphWhile() {
        var dfs = simpleDFS(RootedGraphExamples.oneRootEmptyNeighboursGraph);
        var result = dfs.runAlone();
        assertEquals(1, result.getKnown().size());
    }

    @Test void twoRootEmptyNeighboursGraphWhile() {
        var dfs = simpleDFS(RootedGraphExamples.twoRootsEmptyNeighboursGraph);
        var result = dfs.runAlone();
        assertEquals(2, result.getKnown().size());
    }

    @Test void disconnectedGraph1() {
        var dfs = simpleDFS(RootedGraphExamples.disconnectedGraph1);
        var result = dfs.runAlone();
        assertEquals(3, result.getKnown().size());
        assertEquals(Set.of(1, 2, 3), result.getKnown());
    }

    @Test void disconnectedGraph2() {
        var dfs = simpleDFS(RootedGraphExamples.disconnectedGraph2);
        var result = dfs.runAlone();
        assertEquals(2, result.getKnown().size());
        assertEquals(Set.of(4, 5), result.getKnown());
    }

    @Test void twoRootsTwoGraphs() {
        var dfs = simpleDFS(RootedGraphExamples.twoRootsTwoGraphs);
        var result = dfs.runAlone();
        assertEquals(5, result.getKnown().size());
        assertEquals(Set.of(1, 2, 3, 4, 5), result.getKnown());
    }

    @Test void rootCycle() {
        var dfs = simpleDFS(RootedGraphExamples.rootCycle);
        var result = dfs.runAlone();
        assertEquals(2, result.getKnown().size());
        assertEquals(Set.of(1, 2), result.getKnown());
    }

    @Test void rootCycle3() {
        var dfs = simpleDFS(RootedGraphExamples.rootCycle3);
        var result = dfs.runAlone();
        assertEquals(3, result.getKnown().size());
        assertEquals(Set.of(1, 2, 3), result.getKnown());
    }

    @Test void lasso_1_1() {
        var dfs = simpleDFS(RootedGraphExamples.lasso_1_1);
        var result = dfs.runAlone();
        assertEquals(2, result.getKnown().size());
        assertEquals(Set.of(1, 2), result.getKnown());
    }

    @Test void lasso_2_1() {
        var dfs = simpleDFS(RootedGraphExamples.lasso_2_1);
        var result = dfs.runAlone();
        assertEquals(3, result.getKnown().size());
        assertEquals(Set.of(1, 2, 3), result.getKnown());
    }

    @Test void lasso_1_2() {
        var dfs = simpleDFS(RootedGraphExamples.lasso_1_2);
        var result = dfs.runAlone();
        assertEquals(3, result.getKnown().size());
        assertEquals(Set.of(1, 2, 3), result.getKnown());
    }

    @Test void sharing_3() {
        var dfs = simpleDFS(RootedGraphExamples.sharing_3);
        var result = dfs.runAlone();
        assertEquals(5, result.getKnown().size());
        assertEquals(Set.of(1, 2, 4, 3, 5), result.getKnown());

    }

    @Test void sharing_onKnown() {
        var rediscoverd = new ArrayList<Integer>();
        var dfs = new DepthFirstTraversalRelational<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(
                                RootedGraphExamples.sharing_3,
                                Function.identity(),
                                FunctionalDFTCallbacksModel.onKnown(
                                        (_, v, _) -> {
                                            rediscoverd.add(v); return false;
                                        })
                        )
                ));
        dfs.runAlone();
        assertEquals(2, rediscoverd.size());
        assertEquals(List.of(1, 3), rediscoverd);
    }

    @Test void sharing_onEntry() {
        var discoverd = new ArrayList<Integer>();
        var dfs = new DepthFirstTraversalRelational<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(
                                RootedGraphExamples.sharing_3,
                                Function.identity(),
                                FunctionalDFTCallbacksModel.onEntry(
                                        (_, v, _) -> {
                                            discoverd.add(v); return false;
                                        })
                        )
                ));
        dfs.runAlone();
        assertEquals(5, discoverd.size());
        assertEquals(List.of(1, 2, 3, 4, 5), discoverd);
    }

    @Test void sharing_onExit() {
        var exited = new ArrayList<Integer>();
        var dfs = new DepthFirstTraversalRelational<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(
                                RootedGraphExamples.sharing_3,
                                Function.identity(),
                                FunctionalDFTCallbacksModel.onExit(
                                    (v, _, _) -> {
                                        exited.add(v); return false;
                                    })
                        )
                ));
        dfs.runAlone();
        assertEquals(5, exited.size());
        assertEquals(List.of(3, 2, 5, 4, 1), exited);
    }

    @Test void sharing_onExit1() {
        var exited = new ArrayList<Integer>();
        var dfs = new DepthFirstTraversal<>(
                            DepthFirstTraversal.Algorithm.RELATIONAL,
                            RootedGraphExamples.sharing_3,
                            Function.identity(),
                            FunctionalDFTCallbacksModel.onExit(
                                    (v, _, _) -> {
                                        exited.add(v); return false;
                                    })
                        );
        dfs.runAlone();
        assertEquals(5, exited.size());
        assertEquals(List.of(3, 2, 5, 4, 1), exited);
    }


    @Test void sharing_reducedVertexOk() {
        var dfs = new DepthFirstTraversalRelational<>(
                new DFTConfigurationSetDeque<>(
                        new DepthFirstTraversalParameters<>(
                                RootedGraphExamples.sharing_3,
                                (Integer v) -> v % 3,
                                FunctionalDFTCallbacksModel.onEntry(
                                        (_, v, c) -> {
                                            var rv = ((DFTConfigurationSetDeque<Integer, Integer>)c).reducedVertex;
                                            assertEquals(rv, c.getModel().reduce(v));
                                            return false;
                                        })
                        )
                )
        );
        var result = dfs.runAlone();
        assertEquals(3, result.getKnown().size());
        //note that the known contains the reduced vertices, not the graph vertices
        assertEquals(Set.of(0, 1, 2), result.getKnown());
    }

    @Test void sharing_reducedVertexOk1() {
        var dfs = new DepthFirstTraversal<>(
                        DepthFirstTraversal.Algorithm.RELATIONAL,
                        RootedGraphExamples.sharing_3,
                        (Integer v) -> v % 3,
                        FunctionalDFTCallbacksModel.onEntry(
                                (_, v, c) -> {
                                    var rv = ((DFTConfigurationSetDeque<Integer, Integer>)c).reducedVertex;
                                    assertEquals(rv, c.getModel().reduce(v));
                                    return false;
                                }));
        var result = dfs.runAlone();
        assertEquals(3, result.getKnown().size());
        //note that the known contains the reduced vertices, not the graph vertices
        assertEquals(Set.of(0, 1, 2), result.getKnown());
    }

    @Test void twoRootsTwoGraphsDepthBound0() {
        var dfs = new DepthFirstTraversal<>(DepthFirstTraversal.Algorithm.RELATIONAL, RootedGraphExamples.twoRootsTwoGraphs, 0, FunctionalDFTCallbacksModel.none());
        var result = dfs.runAlone();
        assertEquals(0, result.getKnown().size());
        assertEquals(Set.of(), result.getKnown());
    }

    @Test void twoRootsTwoGraphsDepthBound1() {
        var dfs = new DepthFirstTraversal<>(DepthFirstTraversal.Algorithm.RELATIONAL, RootedGraphExamples.twoRootsTwoGraphs, 1, FunctionalDFTCallbacksModel.none());
        var result = dfs.runAlone();
        assertEquals(2, result.getKnown().size());
        assertEquals(Set.of(1, 4), result.getKnown());
    }

    @Test void twoRootsTwoGraphsDepthBound2() {
        var dfs = new DepthFirstTraversal<>(DepthFirstTraversal.Algorithm.RELATIONAL, RootedGraphExamples.twoRootsTwoGraphs, 2, FunctionalDFTCallbacksModel.none());
        var result = dfs.runAlone();
        assertEquals(5, result.getKnown().size());
        assertEquals(Set.of(1,2,3,4,5), result.getKnown());
    }
}
