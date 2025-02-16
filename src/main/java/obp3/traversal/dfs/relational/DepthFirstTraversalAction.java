package obp3.traversal.dfs.relational;

public sealed interface DepthFirstTraversalAction<V> { }

record BacktrackAction<V>(V vertex) implements DepthFirstTraversalAction<V> { }
record KnownConfigurationAction<V>(V vertex) implements DepthFirstTraversalAction<V> { }
record UnknownConfigurationAction<V>(V vertex) implements DepthFirstTraversalAction<V> { }
record EndAction<V>() implements DepthFirstTraversalAction<V> { }