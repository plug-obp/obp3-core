package obp3.traversal.bfs;

import obp3.things.PeekableIterator;

import java.util.*;

public class BreadthFirstTraversalConfiguration<V> {
    public PeekableIterator<V> neighbours;
    public Set<V> known;
    public Deque<V> frontier;

    public BreadthFirstTraversalConfiguration(PeekableIterator<V> neighbours, Set<V> known, Deque<V> frontier) {
        this.neighbours = neighbours;
        this.known = known;
        this.frontier = frontier;
    }

    public static <X> BreadthFirstTraversalConfiguration<X> initial(Iterator<X> iterator) {
        return new BreadthFirstTraversalConfiguration<>(
                    new PeekableIterator<>(iterator),
                    new HashSet<>(),
                    new LinkedList<>());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BreadthFirstTraversalConfiguration<?> other)) return false;
        return     Objects.equals(neighbours, other.neighbours)
                && Objects.equals(known, other.known)
                && Objects.equals(frontier, other.frontier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbours, known, frontier);
    }

    @Override
    public String toString() {
        return "BreadthFirstTraversalConfiguration(" +
                "neighbours=" + neighbours +
                ", known=" + known +
                ", frontier=" + frontier +
                ')';
    }

    public PeekableIterator<V> getNeighbours() { return neighbours; }

    public Set<V> getKnown() { return known; }

    public Deque<V> getFrontier() { return frontier; }
}
