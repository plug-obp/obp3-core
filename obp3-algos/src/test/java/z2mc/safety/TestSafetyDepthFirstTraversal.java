package z2mc.safety;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
import obp3.runtime.sli.IRootedGraph;
import obp3.runtime.sli.Step;
import obp3.traversal.dfs.DepthFirstTraversal;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSafetyDepthFirstTraversal {
    EmptinessCheckerAnswer<Integer> dfs(IRootedGraph<Integer> graph, Predicate<Integer> predicate) {
        var algo = new SafetyDepthFirstTraversal<>(DepthFirstTraversal.Algorithm.WHILE, graph, -1, Function.identity(), predicate);
        return algo.runAlone();
    }

    @Test
    void twoRootsTwoGraphs() {
        var witness = dfs(RootedGraphExamples.twoRootsTwoGraphs, (_) -> false);
        assertTrue(witness.holds);
        witness = dfs(RootedGraphExamples.twoRootsTwoGraphs, (v) -> v.equals(4));
        assertEquals(1, witness.trace.size());
        assertEquals(new Step<>(null, Optional.empty(), 4), witness.witness);
        witness = dfs(RootedGraphExamples.twoRootsTwoGraphs, (v) -> v.equals(5));
        assertEquals(new Step<>(4, Optional.empty(), 5), witness.witness);
        assertEquals(2, witness.trace.size());
        assertEquals(List.of(4,5), witness.trace);
    }

    @Test
    void rootCycle3() {
        var witness = dfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(1));
        assertEquals(new Step<>(null, Optional.empty(), 1), witness.witness);
        assertEquals(1, witness.trace.size());
        assertEquals(List.of(1), witness.trace);

        witness = dfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(2));
        assertEquals(new Step<>(1, Optional.empty(), 2), witness.witness);
        assertEquals(2, witness.trace.size());
        assertEquals(List.of(1,2), witness.trace);

        witness = dfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(3));
        assertEquals(new Step<>(2, Optional.empty(), 3), witness.witness);
        assertEquals(3, witness.trace.size());
        assertEquals(List.of(1,2,3), witness.trace);

        witness = dfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(4));
        assertTrue(witness.holds);
    }

    @Test
    void sharing_2() {
        var witness = dfs(RootedGraphExamples.sharing_2, (v) -> v.equals(4));
        assertEquals(new Step<>(1, Optional.empty(), 4), witness.witness);
        assertEquals(2, witness.trace.size());
        assertEquals(List.of(1,4), witness.trace);

        witness = dfs(RootedGraphExamples.sharing_2, (v) -> v.equals(3));
        assertEquals(new Step<>(2, Optional.empty(), 3), witness.witness);
        assertEquals(3, witness.trace.size());
        assertEquals(List.of(1,2,3), witness.trace);
    }

    @Test
    void lasso_1_3() {
        var witness = dfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(5));
        assertTrue(witness.holds);

        witness = dfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(3));
        assertEquals(new Step<>(2, Optional.empty(), 3), witness.witness);
        assertEquals(3, witness.trace.size());
        assertEquals(List.of(1,2,3), witness.trace);

        witness = dfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(4));
        assertEquals(new Step<>(3, Optional.empty(), 4), witness.witness);
        assertEquals(4, witness.trace.size());
        assertEquals(List.of(1,2,3,4), witness.trace);
    }
}
