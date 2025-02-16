package obp3.traversal.bfs.relational;

public sealed interface BreadthFirstTraversalAction<V>{ }

record DiscoverNeighboursAction<V>(V vertex) implements BreadthFirstTraversalAction<V> { }
record KnownConfigurationAction<V>(V vertex) implements BreadthFirstTraversalAction<V> { }
record UnknownConfigurationAction<V>(V vertex) implements BreadthFirstTraversalAction<V> { }
record EndAction<V>() implements BreadthFirstTraversalAction<V> { }