package obp3.traversal.dfs.semantics.relational.actions;

public record UnknownConfigurationAction<V, A>(V source, V vertex, A reducedVertex) implements DepthFirstTraversalAction<V, A> {
}
