package obp3.modelchecking.buchi.ndfs.gs09;

import obp3.utils.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.*;

public class BuchiGS09BlueConfiguration<V, A> implements IDepthFirstTraversalConfiguration<V, A> {
    public static class StackFrame<V> extends IDepthFirstTraversalConfiguration.StackFrame<V> {
        public boolean allChildrenRed = false;
        public StackFrame(V vertex, PeekableIterator<V> neighbours) {
            super(vertex, neighbours);
        }
    }
    IDepthFirstTraversalParameters<V, A> model;
    Deque<IDepthFirstTraversalConfiguration.StackFrame<V>> stack = new ArrayDeque<>();
    Map<V, VertexColor> known;

    public BuchiGS09BlueConfiguration(IDepthFirstTraversalParameters<V, A> model) {
        this(model, new HashMap<>());
    }

    public BuchiGS09BlueConfiguration(IDepthFirstTraversalParameters<V, A> model, Map<V, VertexColor> known) {
        this.model = model;
        this.known = known;
    }

    @Override
    public IDepthFirstTraversalParameters<V, A> getModel() {
        return model;
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> initial() {
        this.known.clear();
        this.stack.clear();
        this.stack.push(new StackFrame<>(null, new PeekableIterator<>(model.getGraph().roots())));
        return this;
    }

    @Override
    public StackFrame<V> peek() {
        return (StackFrame<V>) stack.peek();
    }

    @Override
    public StackFrame<V> pop() {
        return (StackFrame<V>) stack.pop();
    }

    @Override
    public void push(IDepthFirstTraversalConfiguration.StackFrame<V> vertex) {
        stack.push(vertex);
    }

    @Override
    public Iterator<IDepthFirstTraversalConfiguration.StackFrame<V>> getStack() {
        return stack.iterator();
    }

    @Override
    public int stackSize() {
        return stack.size();
    }

    @Override
    public boolean knows(V vertex) {
        return known.containsKey(vertex);
    }

    public VertexColor getVertexColor(V vertex) {
        return known.get(vertex);
    }

    public void changeVertexColor(V vertex, VertexColor newColor) {
        known.put(vertex, newColor);
    }

    @Override
    public void add(V vertex) {
        known.put(vertex, VertexColor.CYAN);
    }

    @Override
    public Set getKnown() {
        return known.keySet();
    }

    @Override
    public void discover(V vertex) {
        add(vertex);
        push(
                new StackFrame<>(
                        vertex,
                        new PeekableIterator<>(getModel().getGraph().neighbours(vertex))));
    }
}
