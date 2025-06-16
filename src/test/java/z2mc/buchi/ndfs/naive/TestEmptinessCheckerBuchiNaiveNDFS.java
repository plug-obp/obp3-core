package z2mc.buchi.ndfs.naive;

import obp3.buchi.ndfs.naive.EmptinessChecherBuchiNaiveNDFS;
import obp3.sli.core.IRootedGraph;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEmptinessCheckerBuchiNaiveNDFS {
    List<Integer> ndfs(IRootedGraph<Integer> graph, Predicate<Integer> predicate) {
        return new EmptinessChecherBuchiNaiveNDFS<>(graph, predicate).runAlone();
    }

    @Test
    void twoRootsTwoGraphs() {
        var witness = ndfs(RootedGraphExamples.twoRootsTwoGraphs, (_) -> false);
        assertEquals(0, witness.size());
        witness = ndfs(RootedGraphExamples.twoRootsTwoGraphs, (v) -> v.equals(4));
        assertEquals(0, witness.size());
        witness = ndfs(RootedGraphExamples.twoRootsTwoGraphs, (v) -> v.equals(5));
        assertEquals(3, witness.size());
        assertEquals(List.of(4,5,5), witness);
    }

    @Test
    void rootCycle3() {
        var witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(1));
        assertEquals(4, witness.size());
        assertEquals(List.of(1,2,3,1), witness);

        witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(2));
        assertEquals(5, witness.size());
        assertEquals(List.of(1,2,3,1,2), witness);

        witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(3));
        assertEquals(6, witness.size());
        assertEquals(List.of(1,2,3,1,2,3), witness);

        witness = ndfs(RootedGraphExamples.rootCycle3, (v) -> v.equals(4));
        assertEquals(0, witness.size());
    }

    @Test
    void sharing_2() {
        var witness = ndfs(RootedGraphExamples.sharing_2, (v) -> v.equals(4));
        assertEquals(7, witness.size());
        assertEquals(List.of(1,4,5,2,3,1,4), witness);
    }
}
