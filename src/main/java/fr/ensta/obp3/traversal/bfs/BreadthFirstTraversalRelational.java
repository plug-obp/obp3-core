package fr.ensta.obp3.traversal.bfs;

import fr.ensta.obp3.IExecutable;
import fr.ensta.obp3.IRootedGraph;
import fr.ensta.obp3.Sequencer;
import fr.ensta.obp3.traversal.bfs.relational.BreadthFirstTraversalRelation;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class BreadthFirstTraversalRelational<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public BreadthFirstTraversalRelational(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var relation = new BreadthFirstTraversalRelation<>(graph);
        var sequencer = new Sequencer<>(relation);
        var configuration = sequencer.run(hasToTerminateSupplier);
        return configuration.map(BreadthFirstTraversalConfiguration::getKnown).orElse(Set.of());
    }
}
