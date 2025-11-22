package obp3.traversal.dfs.defaults.domain;

import obp3.runtime.sli.IRootedGraph;
import obp3.utils.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.*;

public class DFTConfiguration4TreeDeque<V, A> implements IDepthFirstTraversalConfiguration<V, A> {
    private final IDepthFirstTraversalParameters<V, A> model;

    private final Deque<IDepthFirstTraversalConfiguration.StackFrame<V>> stack;

    public DFTConfiguration4TreeDeque(
            IDepthFirstTraversalParameters<V, A> model,
            Deque<IDepthFirstTraversalConfiguration.StackFrame<V>> stack) {
        this.model = model;
        this.stack = stack;
    }

    public DFTConfiguration4TreeDeque(IDepthFirstTraversalParameters<V, A> model) {
        this(model, new ArrayDeque<>());
    }

    public static <X, Y> DFTConfiguration4TreeDeque<X, Y> initial(IDepthFirstTraversalParameters<X, Y> model) {
        return new DFTConfiguration4TreeDeque<>(model);
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> initial() {
        this.stack.clear();
        this.stack.push(new IDepthFirstTraversalConfiguration.StackFrame<>(null, new PeekableIterator<>(model.getGraph().roots())));
        return this;
    }

    @Override
    public IDepthFirstTraversalParameters<V, A> getModel() {
        return model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DFTConfiguration4TreeDeque<?, ?> other)) return false;
        return Objects.equals(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + "stack=" + stack + ')';
    }

    public IRootedGraph<V> getGraph() {
        return getModel().getGraph();
    }

    @Override
    public IDepthFirstTraversalConfiguration.StackFrame<V> peek() {
        return stack.peek();
    }
    @Override
    public IDepthFirstTraversalConfiguration.StackFrame<V> pop() {
        return stack.pop();
    }
    @Override
    public void push(IDepthFirstTraversalConfiguration.StackFrame<V> frame) {
        stack.push(frame);
    }
    @Override
    public Iterator<IDepthFirstTraversalConfiguration.StackFrame<V>> getStack() {
        return stack.iterator();
    }
    @Override
    public int stackSize() {
        return stack.size();
    }

    /// For a tree always return false. This works if the rooted-graph model is a tree only
    /// If the rooted graph has cycles the DFT will not terminate.
    /// if the rooted graph has sharing (is a DAG), we will explore a tree unfolding of the DAG (shared nodes traversed multiple times)
    @Override
    public boolean knows(V vertex) {
        return false;
    }
    @Override
    public void add(V vertex) {
        //nothing to do here
    }

    @Override
    public Set<Object> getKnown() { return Collections.emptySet(); }
}
