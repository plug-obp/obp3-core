package obp3.traversal.dfs.semantics.relational.actions;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

public record UnknownConfigurationAction<V, A>(
        V source,
        V vertex,
        IDepthFirstTraversalConfiguration<V, A> configuration) implements DepthFirstTraversalAction<V, A> {
}
