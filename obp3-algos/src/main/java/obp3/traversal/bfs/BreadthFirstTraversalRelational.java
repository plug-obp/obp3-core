package obp3.traversal.bfs;

import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;
import obp3.Sequencer;
import obp3.traversal.bfs.relational.BreadthFirstTraversalRelation;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class BreadthFirstTraversalRelational<V> implements IExecutable<BreadthFirstTraversalConfiguration<V>, Set<V>> {
    IRootedGraph<V> graph;

    public BreadthFirstTraversalRelational(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(Predicate<BreadthFirstTraversalConfiguration<V>> hasToTerminatePredicate) {
        var relation = new BreadthFirstTraversalRelation<>(graph);
        var sequencer = new Sequencer<>(relation);
        var configuration = sequencer.run(hasToTerminatePredicate);
        return configuration.map(BreadthFirstTraversalConfiguration::getKnown).orElse(Set.of());
    }
}
