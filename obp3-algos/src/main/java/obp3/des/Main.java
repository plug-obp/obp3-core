package obp3.des;

import obp3.Sequencer;
import obp3.des.exemples.pingpong.PingPong;
import obp3.sli.core.operators.SemanticRelation2RootedGraph;
import obp3.sli.core.operators.ToDetermistic;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;

import java.util.function.Function;

public class Main {
    public static void main(String[] args) {

        var semantics = new DESSemantics<>(PingPong.initial(10, 2, 1));
        var deterministic = ToDetermistic.randomPolicy(semantics, System.nanoTime());
        var sequencer = new Sequencer<>(deterministic);
        sequencer.runAlone();

        System.out.println("-------------------------------------");

        semantics = new DESSemantics<>(PingPong.initial(10, 2, 1));
        var rr = new SemanticRelation2RootedGraph<>(semantics);
        var dfs =
                new DepthFirstTraversalRelational<>(
                        new DFTConfigurationSetDeque<>(
                                new DepthFirstTraversalParameters<>(rr, Function.identity())));
        var r = dfs.runAlone();
        System.out.println("DFS: " + r.getKnown().size() + " configurations");
    }
}
