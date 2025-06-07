package z2mc.scc;

import obp3.scc.TarjanConfigurationSCC;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;
import org.junit.jupiter.api.Test;
import z2mc.traversal.dft.RootedGraphExamples;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTarjanSCC {

    @Test
    void sharing_reducedVertexOk() {
        var dfs = new DepthFirstTraversalWhile<>(
                new TarjanConfigurationSCC<>(RootedGraphExamples.sharing_3));

        var result = dfs.runAlone();
        assertEquals(5, result.getKnown().size());
    }
}
