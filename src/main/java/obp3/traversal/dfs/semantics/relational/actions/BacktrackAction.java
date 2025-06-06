package obp3.traversal.dfs.semantics.relational.actions;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

public record BacktrackAction<V, A>(
        V vertex,
        IDepthFirstTraversalConfiguration.StackFrame<V> frame,
        IDepthFirstTraversalConfiguration<V, A> configuration) implements DepthFirstTraversalAction<V, A> {
}
