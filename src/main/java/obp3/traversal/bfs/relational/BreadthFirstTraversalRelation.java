package obp3.traversal.bfs.relational;

import obp3.things.PeekableIterator;
import obp3.sli.core.IRootedGraph;
import obp3.sli.core.DeterministicSemanticRelation;
import obp3.traversal.bfs.BreadthFirstTraversalConfiguration;

import java.util.Optional;

public class BreadthFirstTraversalRelation<V>
        implements
        DeterministicSemanticRelation<BreadthFirstTraversalAction<V>, BreadthFirstTraversalConfiguration<V>> {

    IRootedGraph<V> graph;

    public BreadthFirstTraversalRelation(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Optional<BreadthFirstTraversalConfiguration<V>> initial() {
        return Optional.of(BreadthFirstTraversalConfiguration.initial(graph.roots()));
    }

    @Override
    public Optional<BreadthFirstTraversalAction<V>> actions(BreadthFirstTraversalConfiguration<V> configuration) {
        var neighbours = configuration.neighbours;
        // we have at least one more neighbour check it against the known
        if (neighbours.hasNext()) {
            V vertex = neighbours.peek();
            return Optional.of(configuration.known.contains(vertex) ?
                    new KnownConfigurationAction<>(vertex) :
                    new UnknownConfigurationAction<>(vertex));
        }
        // if no more neighbours of the previous source, get a new one from the frontier
        if (!configuration.frontier.isEmpty()) {
            return Optional.of(new DiscoverNeighboursAction<>(configuration.frontier.peekFirst()));
        }
        // at end if the iterator is at end and the frontier is empty
        return Optional.empty();
    }

    @Override
    public Optional<BreadthFirstTraversalConfiguration<V>> execute(BreadthFirstTraversalAction<V> action, BreadthFirstTraversalConfiguration<V> configuration) {
        switch (action) {
            case DiscoverNeighboursAction<V> _ -> {
                V first = configuration.frontier.removeFirst();
                configuration.neighbours = new PeekableIterator<>(graph.neighbours(first));
                return Optional.of(configuration);
            }
            case KnownConfigurationAction<V> _ -> {
                //only advance the neighbours iterator
                configuration.neighbours.next();
                return Optional.of(configuration);
            }
            case UnknownConfigurationAction<V>(var vertex) -> {
                //advance the neighbours iterator
                configuration.neighbours.next();
                //discover the vertex
                configuration.known.add(vertex);
                configuration.frontier.addLast(vertex);

                return Optional.of(configuration);
            }
            case EndAction<V> _ -> {
                return Optional.empty();
            }
        }
    }
}