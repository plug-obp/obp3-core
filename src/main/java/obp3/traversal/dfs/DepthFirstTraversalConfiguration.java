package obp3.traversal.dfs;

import obp3.things.PeekableIterator;

import java.util.*;

public class DepthFirstTraversalConfiguration<V> {

    public record StackFrame<V>(V vertex, PeekableIterator<V> neighbours) { }

    public Set<V> known;
    public Deque<StackFrame<V>> stack;

    public DepthFirstTraversalConfiguration(Set<V> known, Deque<StackFrame<V>> stack) {
        this.known = known;
        this.stack = stack;
    }

    public static <X> DepthFirstTraversalConfiguration<X> initial(Iterator<X> iterator) {
             return new DepthFirstTraversalConfiguration<>(
                     new HashSet<>(),
                     new ArrayDeque<>(
                            Collections.singleton(
                                    new DepthFirstTraversalConfiguration.StackFrame<>(
                                            null,
                                            new PeekableIterator<>(iterator)))));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepthFirstTraversalConfiguration<?> other)) return false;
        return     Objects.equals(known, other.known)
                && Objects.equals(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(known, stack);
    }

    @Override
    public String toString() {
        return "DepthFirstTraversalConfiguration(" +
                "known=" + known +
                ", frontier=" + stack +
                ')';
    }

    public Set<V> getKnown() { return known; }

    public Deque<StackFrame<V>> getStack() { return stack; }
}
