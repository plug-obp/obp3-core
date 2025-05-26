package obp3.traversal.dfs.semantics.relational.actions;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

public record BacktrackAction<V, A>(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame) implements DepthFirstTraversalAction<V, A> {
}
