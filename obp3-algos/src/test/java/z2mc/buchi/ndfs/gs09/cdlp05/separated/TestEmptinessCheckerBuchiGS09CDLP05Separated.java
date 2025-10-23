package z2mc.buchi.ndfs.gs09.cdlp05.separated;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.buchi.ndfs.gs09.cdlp05.separated.EmptinessCheckerBuchiGS09CDLP05Separated;
import obp3.sli.core.IRootedGraph;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestEmptinessCheckerBuchiGS09CDLP05Separated {
    EmptinessCheckerAnswer<Integer> ndfs(IRootedGraph<Integer> graph, Predicate<Integer> predicate) {
        return new EmptinessCheckerBuchiGS09CDLP05Separated<>(graph, predicate).runAlone();
    }

    @Test
    void twoRootsTwoGraphs() {
        var witness = ndfs(RootedGraphExamples.twoRootsTwoGraphs, (_) -> false);
        assertTrue(witness.holds);
        witness = ndfs(RootedGraphExamples.twoRootsTwoGraphs, (v) -> v.equals(4));
        assertTrue(witness.holds);
        witness = ndfs(RootedGraphExamples.twoRootsTwoGraphs, (v) -> v.equals(5));
        assertEquals(3, witness.trace.size());
        assertEquals(List.of(4,5,5), witness.trace);
    }

    @Test
    void rootCycle3_1() {
        var witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(1));
        assertEquals(4, witness.trace.size());
        assertEquals(List.of(1, 2, 3, 1), witness.trace);
    }

    @Test
    void rootCycle3_2() {
        var witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(2));
        assertEquals(4, witness.trace.size());
        assertEquals(List.of(1,2,3,1), witness.trace);
    }

    @Test
    void rootCycle3_3() {
        var witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(3));
        assertEquals(4, witness.trace.size());
        assertEquals(List.of(1,2,3,1), witness.trace);
    }

    @Test
    void rootCycle3_none() {
        var witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(4));
        assertTrue(witness.holds);
    }

    @Test
    void sharing_2() {
        var witness = ndfs(RootedGraphExamples.sharing_2, (v) -> v.equals(4));
        assertEquals(6, witness.trace.size());
        assertEquals(List.of(1,4,5,2,3,1), witness.trace);
    }

    @Test
    void lasso_1_3() {
        var witness = ndfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(1));
        assertTrue(witness.holds);

        witness = ndfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(2));
        assertEquals(5, witness.trace.size());
        assertEquals(List.of(1,2,3,4,2), witness.trace);

        witness = ndfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(3));
        assertEquals(5, witness.trace.size());
        assertEquals(List.of(1,2,3,4,2), witness.trace);

        witness = ndfs(RootedGraphExamples.lasso_1_3, (v) -> v.equals(4));
        assertEquals(5, witness.trace.size());
        assertEquals(List.of(1,2,3,4,2), witness.trace);
    }
}
