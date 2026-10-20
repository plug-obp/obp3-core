package z2mc.traversal.bft;

import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;
import obp3.traversal.bfs.BreadthFirstTraversalDoFlat;
import obp3.traversal.bfs.BreadthFirstTraversalConfiguration;

import java.util.Set;

public class TestBFTDoFlat extends AbstractBFTTest {
    @Override
    protected IExecutable<BreadthFirstTraversalConfiguration<Integer>, Set<Integer>> breadthFirstTraversal(IRootedGraph<Integer> graph) {
        return new BreadthFirstTraversalDoFlat<>(graph);
    }
}
