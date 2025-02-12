package fr.ensta.obp3.traversal.bfs.relational;

import fr.ensta.obp3.sli.core.DeterministicSemanticRelation;
import fr.ensta.PeekableIterator;
import fr.ensta.obp3.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Optional;

public class BreadthFirstSearchRelation<V>
        implements
        DeterministicSemanticRelation<BreadthFirstSearchAction<V>, BreadthFirstSearchConfiguration<V>> {

    RootedGraph<V> graph;

    public BreadthFirstSearchRelation(RootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Optional<BreadthFirstSearchConfiguration<V>> initial() {
        return Optional.of(new BreadthFirstSearchConfiguration<>(new PeekableIterator<>(graph.roots()), new HashSet<>(), new ArrayDeque<>()));
    }

    @Override
    public Optional<BreadthFirstSearchAction<V>> actions(BreadthFirstSearchConfiguration<V> configuration) {
        var neighboursIterator = configuration.neighboursIterator();
        // we have at least one more neighbour check it against the known
        if (neighboursIterator.hasNext()) {
            V vertex = neighboursIterator.peek();
            return Optional.of(configuration.known().contains(vertex) ?
                    new KnownConfigurationAction<>(vertex) :
                    new UnknownConfigurationAction<>(vertex));
        }
        // if no more neighbours of the previous source, get a new one from the frontier
        if (!configuration.frontier().isEmpty()) {
            return Optional.of(new DiscoverNeighboursAction<>(configuration.frontier().peekFirst()));
        }
        // at end if the iterator is at end and the frontier is empty
        return Optional.empty();
    }

    @Override
    public Optional<BreadthFirstSearchConfiguration<V>> execute(BreadthFirstSearchAction<V> action, BreadthFirstSearchConfiguration<V> configuration) {
        switch (action) {
            case DiscoverNeighboursAction<V> _ -> {
                V first = configuration.frontier().removeFirst();
                return Optional.of(
                        new BreadthFirstSearchConfiguration<>(
                                new PeekableIterator<>(graph.neighbours(first)),
                                configuration.known(),
                                configuration.frontier()));
            }
            case KnownConfigurationAction<V> _ -> {
                configuration.neighboursIterator().next();
                return Optional.of(configuration);
            }
            case UnknownConfigurationAction<V> (var vertex) -> {
                configuration.known().add(vertex);
                configuration.frontier().addLast(vertex);

                return Optional.of(configuration);
            }
            case EndAction<V> _ -> { return Optional.empty(); }
        }
    }
}