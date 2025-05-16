package obp3.traversal.dfs.relational;

import obp3.sli.core.DeterministicSemanticRelation;
import obp3.traversal.dfs.DepthFirstTraversalConfiguration;
import obp3.traversal.dfs.IDepthFirstTraversalParameters;

import java.util.Optional;

public class DepthFirstTraversalRelation<V, A> implements DeterministicSemanticRelation<DepthFirstTraversalAction<V>, DepthFirstTraversalConfiguration<V, A>> {
    IDepthFirstTraversalParameters<V, A> model;

    public DepthFirstTraversalRelation(IDepthFirstTraversalParameters<V, A> model) {
        this.model = model;
    }

    @Override
    public Optional<DepthFirstTraversalConfiguration<V, A>> initial() {
        return Optional.of(DepthFirstTraversalConfiguration.initial(model));
    }

    @Override
    public Optional<DepthFirstTraversalAction<V>> actions(DepthFirstTraversalConfiguration<V, A> configuration) {
        var frame = configuration.peek();
        // at end if the stack is empty. Don't produce the end action
        if (frame == null) { return Optional.empty(); }
        var neighboursIterator = frame.neighbours();
        // we have at least one more neighbour check it against the known
        if (neighboursIterator.hasNext()) {
            V vertex = neighboursIterator.peek();
            return Optional.of(configuration.knows(vertex) ?
                    new KnownConfigurationAction<>(vertex) :
                    new UnknownConfigurationAction<>(vertex));
        }
        // if no more neighbours of the previous source, backtrack to get a new one from the stack
        return Optional.of(new BacktrackAction<>(frame.vertex()));
    }

    @Override
    public Optional<DepthFirstTraversalConfiguration<V, A>> execute(DepthFirstTraversalAction<V> action, DepthFirstTraversalConfiguration<V, A> configuration) {
        switch (action) {
            case BacktrackAction<V> _ -> {
                configuration.pop();
                return Optional.of(configuration);
            }
            case KnownConfigurationAction<V> _ -> {
                configuration.peek().neighbours().next();
                return Optional.of(configuration);
            }
            case UnknownConfigurationAction<V> (var vertex) -> {
                configuration.discover(vertex);
                return Optional.of(configuration);
            }
            case EndAction<V> _ -> {
                return Optional.empty();
            }
        }
    }
}
