package obp3.traversal.dfs.semantics.relational;

import obp3.sli.core.DeterministicSemanticRelation;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.semantics.relational.actions.*;

import java.util.Optional;

public class DepthFirstTraversalRelation<V, A>
        implements DeterministicSemanticRelation<DepthFirstTraversalAction<V, A>, IDepthFirstTraversalConfiguration<V, A>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalRelation(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    @Override
    public Optional<IDepthFirstTraversalConfiguration<V, A>> initial() {
        return Optional.of(configuration.initial());
    }

    @Override
    public Optional<DepthFirstTraversalAction<V, A>> actions(IDepthFirstTraversalConfiguration<V, A> configuration) {
        var frame = configuration.peek();
        // at end if the stack is empty. Don't produce the end action
        if (frame == null) { return Optional.empty(); }
        var neighboursIterator = frame.neighbours();
        // we have at least one more neighbour check it against the known
        if (neighboursIterator.hasNext()) {
            V vertex = neighboursIterator.peek();
            var reduced_vertex = configuration.getModel().canonize(vertex);
            return Optional.of(configuration.knows(vertex, reduced_vertex) ?
                    new KnownConfigurationAction<>(frame.vertex(), vertex, reduced_vertex) :
                    new UnknownConfigurationAction<>(frame.vertex(), vertex, reduced_vertex));
        }
        // if no more neighbours of the previous source, backtrack to get a new one from the stack
        return Optional.of(new BacktrackAction<>(frame.vertex(), frame));
    }

    @Override
    public Optional<IDepthFirstTraversalConfiguration<V, A>> execute(DepthFirstTraversalAction<V, A> action, IDepthFirstTraversalConfiguration<V, A> configuration) {
        switch (action) {
            case BacktrackAction<V, A> _ -> {
                configuration.pop();
                return Optional.of(configuration);
            }
            case KnownConfigurationAction<V, A> _ -> {
                //only advance the neighbours iterator
                configuration.peek().neighbours().next();
                return Optional.of(configuration);
            }
            case UnknownConfigurationAction<V, A> (var _, var vertex, var reduced_vertex)  -> {
                //advance the neighbours iterator
                configuration.peek().neighbours().next();
                //discover the vertex
                configuration.discover(vertex, reduced_vertex);
                return Optional.of(configuration);
            }
        }
    }
}
