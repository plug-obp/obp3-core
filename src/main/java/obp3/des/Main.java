package obp3.des;

import obp3.Sequencer;
import obp3.des.exemples.pingpong.PingPong;
import obp3.sli.core.operators.SemanticRelation2RootedGraph;
import obp3.sli.core.operators.ToDetermistic;
import obp3.traversal.dfs.DepthFirstTraversalRelational;

public class Main {
    public static void main(String[] args) {

        var semantics = new DESSemantics<>(PingPong.initial(10, 2, 1));
        var deterministic = ToDetermistic.randomPolicy(semantics, System.nanoTime());
        var sequencer = new Sequencer<>(deterministic);
        sequencer.runAlone();

        System.out.println("-------------------------------------");

        semantics = new DESSemantics<>(PingPong.initial(5, 2));
        var rr = new SemanticRelation2RootedGraph<>(semantics);
        var dfs = new DepthFirstTraversalRelational<>(rr);
        var r = dfs.runAlone();
        System.out.println("DFS: " + r.size() + " configurations");
    }
}
