package fr.ensta.traversal.bfs.relational;

import fr.ensta.DeterministicSemanticRelation;
import fr.ensta.PeekableIterator;
import fr.ensta.RootedGraph;

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
        return Optional.of(new BreadthFirstSearchConfiguration<>(null, new HashSet<>(), new ArrayDeque<>()));
    }

    @Override
    public Optional<BreadthFirstSearchAction<V>> actions(BreadthFirstSearchConfiguration<V> configuration) {
        var neighboursIterator = configuration.neighboursIterator();
        if (neighboursIterator == null) {
            return Optional.of(BreadthFirstSearchAction.initializeA());
        }
        // we have at least one more neighbour check it against the known
        if (neighboursIterator.hasNext()) {
            V vertex = neighboursIterator.peek();
            return Optional.of(configuration.known().contains(vertex) ?
                    BreadthFirstSearchAction.inKnownA() :
                    BreadthFirstSearchAction.notInKnownA(vertex));
        }
        // if no more neighbours of the previous source, get a new one from the frontier
        if (!configuration.frontier().isEmpty()) {
            return Optional.of(BreadthFirstSearchAction.discoverA());
        }
        // at end if the iterator is at end and the frontier is empty
        return Optional.empty();
    }

    @Override
    public Optional<BreadthFirstSearchConfiguration<V>> execute(BreadthFirstSearchAction<V> action, BreadthFirstSearchConfiguration<V> configuration) {
        if (action instanceof BreadthFirstSearchAction.InitializeAction) {
            return Optional.of(
                    new BreadthFirstSearchConfiguration<>(
                            new PeekableIterator<>(graph.roots()),
                            configuration.known(),
                            configuration.frontier()));
        }

        if (action instanceof BreadthFirstSearchAction.DiscoverAction) {
            V first = configuration.frontier().removeFirst();
            return Optional.of(
                    new BreadthFirstSearchConfiguration<>(
                            new PeekableIterator<>(graph.neighbours(first)),
                            configuration.known(),
                            configuration.frontier()));
        }

        if (action instanceof BreadthFirstSearchAction.InKnownAction<V>) {
            configuration.neighboursIterator().next();
            return Optional.of(configuration);
        }

        if (action instanceof BreadthFirstSearchAction.NotInKnownAction<V> v) {
            configuration.known().add(v.vertex);
            configuration.frontier().addLast(v.vertex);

            return Optional.of(configuration);
        }

        return Optional.empty();
    }
}