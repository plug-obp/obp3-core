package fr.ensta.obp3.traversal.dfs;

import fr.ensta.obp3.IExecutable;
import fr.ensta.obp3.IRootedGraph;
import fr.ensta.obp3.Sequencer;
import fr.ensta.obp3.traversal.dfs.relational.DepthFirstTraversalRelation;

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
