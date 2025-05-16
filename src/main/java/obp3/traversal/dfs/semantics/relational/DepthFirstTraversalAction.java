package obp3.traversal.dfs.semantics.relational;

public sealed interface DepthFirstTraversalAction<V, A> { }

record BacktrackAction<V, A>(V vertex) implements DepthFirstTraversalAction<V, A> { }
record KnownConfigurationAction<V, A>(V vertex, A reducedVertex) implements DepthFirstTraversalAction<V, A> { }
record UnknownConfigurationAction<V, A>(V vertex, A reducedVertex) implements DepthFirstTraversalAction<V, A> { }
record EndAction<V, A>() implements DepthFirstTraversalAction<V, A> { }