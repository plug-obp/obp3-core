package obp3.traversal.dfs.relational;

import obp3.things.PeekableIterator;
import obp3.IRootedGraph;
import obp3.sli.core.DeterministicSemanticRelation;
import obp3.traversal.dfs.DepthFirstTraversalConfiguration;

import java.util.*;

public class DepthFirstTraversalRelation<V> implements DeterministicSemanticRelation<DepthFirstTraversalAction<V>, DepthFirstTraversalConfiguration<V>> {
    IRootedGraph<V> graph;

    public DepthFirstTraversalRelation(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Optional<DepthFirstTraversalConfiguration<V>> initial() {
        return Optional.of(DepthFirstTraversalConfiguration.initial(graph.roots()));
    }

    @Override
    public Optional<DepthFirstTraversalAction<V>> actions(DepthFirstTraversalConfiguration<V> configuration) {
        var frame = configuration.stack.peekFirst();
        // at end if the stack is empty. Don't produce the end action
        if (frame == null) { return Optional.empty(); }
        var neighboursIterator = frame.neighbours();
        // we have at least one more neighbour check it against the known
        if (neighboursIterator.hasNext()) {
            V vertex = neighboursIterator.peek();
            return Optional.of(configuration.known.contains(vertex) ?
                    new KnownConfigurationAction<>(vertex) :
                    new UnknownConfigurationAction<>(vertex));
        }
        // if no more neighbours of the previous source, backtrack to get a new one from the stack
        return Optional.of(new BacktrackAction<>(frame.vertex()));
    }

    @Override
    public Optional<DepthFirstTraversalConfiguration<V>> execute(DepthFirstTraversalAction<V> action, DepthFirstTraversalConfiguration<V> configuration) {
        switch (action) {
            case BacktrackAction<V> _ -> {
                configuration.stack.pop();
                return Optional.of(configuration);
            }
            case KnownConfigurationAction<V> _ -> {
                configuration.stack.peek().neighbours().next();
                return Optional.of(configuration);
            }
            case UnknownConfigurationAction<V> (var vertex) -> {
                configuration.known.add(vertex);

                configuration.stack.push(
                        new DepthFirstTraversalConfiguration.StackFrame<>(vertex,
                                new PeekableIterator<>(
                                        graph.neighbours(vertex))));
                return Optional.of(configuration);
            }
            case EndAction<V> _ -> {
                return Optional.empty();
            }
        }
    }
}
