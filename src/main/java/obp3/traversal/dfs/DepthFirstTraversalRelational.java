package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.Sequencer;
import obp3.traversal.dfs.relational.DepthFirstTraversalRelation;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalRelational<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public DepthFirstTraversalRelational(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var relation = new DepthFirstTraversalRelation<>(graph);
        var sequencer = new Sequencer<>(relation);
        var configuration = sequencer.run(hasToTerminateSupplier);
        return configuration.map(DepthFirstTraversalConfiguration::getKnown).orElse(Set.of());
    }
}
