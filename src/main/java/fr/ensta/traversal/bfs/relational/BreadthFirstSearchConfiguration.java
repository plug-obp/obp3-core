package fr.ensta.traversal.bfs.relational;

import fr.ensta.PeekableIterator;

import java.util.Deque;
import java.util.Objects;
import java.util.Set;

public record BreadthFirstSearchConfiguration<V>(PeekableIterator<V> neighboursIterator, Set<V> known,
                                                 Deque<V> frontier) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BreadthFirstSearchConfiguration<?>(
                PeekableIterator<?> iterator, Set<?> known1, Deque<?> frontier1
        ))) return false;
        return Objects.equals(known, known1) && Objects.equals(frontier, frontier1) && Objects.equals(neighboursIterator, iterator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighboursIterator, known, frontier);
    }
}
