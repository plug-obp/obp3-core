package obp3.traversal.dfs;

import obp3.sli.core.IRootedGraph;
import obp3.things.PeekableIterator;

import java.util.*;

public class DepthFirstTraversalConfiguration<V> implements IDepthFirstTraversalConfiguration<V> {
    //TODO: I put the rooted graph here, but actually, the configuration should link back to the inputs of the algorithm
    //the rooted graph should be part of the equality ?
    private final IRootedGraph<V> graph;

    private Set<V> known;
    private Deque<StackFrame<V>> stack;

    public DepthFirstTraversalConfiguration(IRootedGraph<V> graph, Set<V> known, Deque<StackFrame<V>> stack) {
        this.graph = graph;
        this.known = known;
        this.stack = stack;
    }

    public DepthFirstTraversalConfiguration(IRootedGraph<V> graph) {
        this(graph, new HashSet<>(), new ArrayDeque<>(Collections.singleton(new StackFrame<>(null, new PeekableIterator<>(graph.roots())))));
    }

    public static <X> DepthFirstTraversalConfiguration<X> initial(IRootedGraph<X> graph) {
             return new DepthFirstTraversalConfiguration<>(graph);
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

    @Override
    public IRootedGraph<V> getGraph() {
        return graph;
    }

    public StackFrame<V> peek() {
        return stack.peek();
    }
    public void pop() {
        stack.pop();
    }
    public void push(StackFrame<V> frame) {
        stack.push(frame);
    }

    public boolean knows(V vertex) {
        return known.contains(vertex);
    }
    public void add(V vertex) {
        known.add(vertex);
    }

    public Set<V> getKnown() { return known; }

    public Deque<StackFrame<V>> getStack() { return stack; }
}
