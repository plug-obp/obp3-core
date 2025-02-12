package fr.ensta.obp3.traversal.bfs;

import fr.ensta.obp3.IExecutionController;
import fr.ensta.obp3.RootedGraph;
import fr.ensta.obp3.Sequencer;
import fr.ensta.obp3.traversal.bfs.relational.BreadthFirstSearchConfiguration;
import fr.ensta.obp3.traversal.bfs.relational.BreadthFirstSearchRelation;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class BreadthFirstSearchRelational<V> implements IExecutionController<Set<V>> {
    RootedGraph<V> graph;

    public BreadthFirstSearchRelational(RootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var relation = new BreadthFirstSearchRelation<>(graph);
        var sequencer = new Sequencer<>(relation);
        var configuration = sequencer.run(hasToTerminateSupplier);
        return configuration.map(BreadthFirstSearchConfiguration::known).orElse(Set.of());
    }
}
