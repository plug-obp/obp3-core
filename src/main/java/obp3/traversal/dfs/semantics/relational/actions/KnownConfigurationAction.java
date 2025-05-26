package obp3.traversal.dfs.semantics.relational.actions;

public record KnownConfigurationAction<V, A>(V source, V vertex, A reducedVertex) implements DepthFirstTraversalAction<V, A> {
}
