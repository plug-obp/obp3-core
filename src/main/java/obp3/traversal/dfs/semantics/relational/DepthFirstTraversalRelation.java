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
        // if we did not reach the depth bound, and we have at least one more neighbour check it against the known
        var depthBound = configuration.getModel().getDepthBound();
        if (   (depthBound < 0 || depthBound >= configuration.stackSize())
            && neighboursIterator.hasNext())
        {
            V vertex = neighboursIterator.peek();
            return Optional.of(configuration.knows(vertex) ?
                    new KnownConfigurationAction<>(frame.vertex(), vertex, configuration) :
                    new UnknownConfigurationAction<>(frame.vertex(), vertex, configuration));
        }
        // if no more neighbours of the previous source, backtrack to get a new one from the stack
        return Optional.of(new BacktrackAction<>(frame.vertex(), frame, configuration));
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
            case UnknownConfigurationAction<V, A> (var _, var vertex, var _)  -> {
                //advance the neighbours iterator
                configuration.peek().neighbours().next();
                //discover the vertex
                configuration.discover(vertex);
                return Optional.of(configuration);
            }
        }
    }
}
