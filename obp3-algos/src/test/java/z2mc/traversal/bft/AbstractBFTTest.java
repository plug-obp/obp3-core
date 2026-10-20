package z2mc.traversal.bft;

import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;
import obp3.traversal.bfs.BreadthFirstTraversalConfiguration;
import org.junit.jupiter.api.Test;
import z2mc.traversal.AbstractTraversalTest;
import z2mc.traversal.RootedGraphExamples;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Breadth-first traversal tests: the shared traversal contract plus BFT configuration invariants. */
abstract class AbstractBFTTest extends AbstractTraversalTest {

    protected abstract IExecutable<BreadthFirstTraversalConfiguration<Integer>, Set<Integer>> breadthFirstTraversal(IRootedGraph<Integer> graph);

    @Override
    protected Set<?> traverse(IRootedGraph<Integer> graph) {
        return breadthFirstTraversal(graph).runAlone();
    }

    @Override
    protected Set<?> traverseUntilCheck(IRootedGraph<Integer> graph, int terminationCheck) {
        int[] checks = {0};
        return breadthFirstTraversal(graph).run(_ -> ++checks[0] >= terminationCheck);
    }

    @Test
    void treeGraphReachesAllVertices() {
        assertEquals(Set.of(1, 2, 3, 4), traverse(RootedGraphExamples.tree4));
    }

    @Test
    void knownOnlyGrowsAndFrontierIsKnownWithoutDuplicates() {
        int[] previousKnown = {0};
        breadthFirstTraversal(RootedGraphExamples.sharing_3).run(c -> {
            assertTrue(c.getKnown().size() >= previousKnown[0]);
            previousKnown[0] = c.getKnown().size();
            assertTrue(c.getKnown().containsAll(c.getFrontier()));
            assertEquals(c.getFrontier().size(), new HashSet<>(c.getFrontier()).size());
            return false;
        });
        assertEquals(5, previousKnown[0]);
    }
}
