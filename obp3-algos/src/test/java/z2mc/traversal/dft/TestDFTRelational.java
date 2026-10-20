package z2mc.traversal.dft;

import obp3.traversal.dfs.DepthFirstTraversal;

public class TestDFTRelational extends AbstractDFTTest {
    @Override
    protected DepthFirstTraversal.Algorithm algorithm() {
        return DepthFirstTraversal.Algorithm.RELATIONAL;
    }
}
