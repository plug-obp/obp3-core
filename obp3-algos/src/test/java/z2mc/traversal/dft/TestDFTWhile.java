package z2mc.traversal.dft;

import obp3.traversal.dfs.DepthFirstTraversal;

public class TestDFTWhile extends AbstractDFTTest {
    @Override
    protected DepthFirstTraversal.Algorithm algorithm() {
        return DepthFirstTraversal.Algorithm.WHILE;
    }
}
