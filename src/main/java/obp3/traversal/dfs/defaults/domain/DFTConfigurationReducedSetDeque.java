package obp3.traversal.dfs.defaults.domain;

import obp3.sli.core.IRootedGraph;
import obp3.things.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.*;

public class DFTConfigurationReducedSetDeque<V, A> implements IDepthFirstTraversalConfiguration<V, A> {
    private final IDepthFirstTraversalParameters<V, A> model;

    private final Set<A> known;
    private final Deque<StackFrame<V>> stack;

    public DFTConfigurationReducedSetDeque(IDepthFirstTraversalParameters<V, A> model, Set<A> known, Deque<StackFrame<V>> stack) {
        this.model = model;
        this.known = known;
        this.stack = stack;
    }

    public DFTConfigurationReducedSetDeque(IDepthFirstTraversalParameters<V, A> model) {
        this(model, new HashSet<>(), new ArrayDeque<>());
    }

    public static <X, Y> DFTConfigurationSetDeque<X, Y> initial(IDepthFirstTraversalParameters<X, Y> model) {
        return new DFTConfigurationSetDeque<>(model);
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> initial() {
        this.known.clear();
        this.stack.clear();
        this.stack.push(new StackFrame<>(null, new PeekableIterator<>(model.getGraph().roots())));
        return this;
    }

    @Override
    public IDepthFirstTraversalParameters<V, A> getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DFTConfigurationReducedSetDeque<?, ?> other)) return false;
        return     Objects.equals(known, other.known)
                && Objects.equals(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(known, stack);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" +
                "known=" + known +
                ", frontier=" + stack +
                ')';
    }

    public IRootedGraph<V> getGraph() {
        return getModel().getGraph();
    }

    public StackFrame<V> peek() {
        return stack.peek();
    }
    public StackFrame<V> pop() {
        return stack.pop();
    }
    public void push(StackFrame<V> frame) {
        stack.push(frame);
    }
    /// The reductedVertex is a local-to-transition variable,
    /// that means that is computed when selecting the transition and using when executing the transition.
    /// it only exists as a local cache so that the canonize function is not called twice
    public A reducedVertex;
    @Override
    public boolean knows(V vertex) {
        reducedVertex = model.reduce(vertex);
        return known.contains(reducedVertex);
    }
    @Override
    public void add(V vertex) {
        known.add(reducedVertex);
    }

    @Override
    public Set<A> getKnown() { return known; }

    public Deque<StackFrame<V>> getStack() { return stack; }
}
