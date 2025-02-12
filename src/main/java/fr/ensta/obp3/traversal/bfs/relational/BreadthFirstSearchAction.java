package fr.ensta.obp3.traversal.bfs.relational;

public sealed interface BreadthFirstSearchAction<V>{ }

record DiscoverNeighboursAction<V>(V vertex) implements BreadthFirstSearchAction<V>{ }
record KnownConfigurationAction<V>(V vertex) implements BreadthFirstSearchAction<V>{ }
record UnknownConfigurationAction<V>(V vertex) implements BreadthFirstSearchAction<V>{ }
record EndAction<V>() implements BreadthFirstSearchAction<V>{ }