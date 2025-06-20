package obp3.buchi.ndfs.gs09.cdlp05;

import obp3.buchi.ndfs.gs09.VertexColor;
import obp3.things.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.*;
import java.util.function.Predicate;

public class BuchiGS09BlueConfiguration<V, A> implements IDepthFirstTraversalConfiguration<V, A> {
    public static class StackFrame<V> extends IDepthFirstTraversalConfiguration.StackFrame<V> {
        public boolean allChildrenRed = false;
        public StackFrame(V vertex, PeekableIterator<V> neighbours) {
            super(vertex, neighbours);
        }
    }
    IDepthFirstTraversalParameters<V, A> model;
    Deque<IDepthFirstTraversalConfiguration.StackFrame<V>> stack = new ArrayDeque<>();
    Map<V, WeightedColor> known;
    Predicate<V> acceptingPredicate;
    /// the number of accepting states from the source to the top of the stack
    int weight = 0;

    public BuchiGS09BlueConfiguration(IDepthFirstTraversalParameters<V, A> model, Predicate<V> acceptingPredicate) {
        this(model, acceptingPredicate, new HashMap<>());
    }

    public BuchiGS09BlueConfiguration(IDepthFirstTraversalParameters<V, A> model, Map<V, WeightedColor> known) {
        this.model = model;
        this.acceptingPredicate = null;
        this.known = known;
    }

    public BuchiGS09BlueConfiguration(IDepthFirstTraversalParameters<V, A> model, Predicate<V> acceptingPredicate, Map<V, WeightedColor> known) {
        this.model = model;
        this.acceptingPredicate = acceptingPredicate;
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

    public int getVertexWeight(V vertex) {
        return known.get(vertex).weight;
    }

    public VertexColor getVertexColor(V vertex) {
        return known.get(vertex).color;
    }

    public void changeVertexColor(V vertex, VertexColor newColor) {
        var weightedColor = known.get(vertex);
        if (weightedColor != null) {
            weightedColor.color = newColor;
            return;
        }
        known.put(vertex, new WeightedColor(newColor, 0));
    }

    @Override
    public void add(V vertex) {
        weight += acceptingPredicate.test(vertex) ? 1 : 0;
        known.put(vertex, new WeightedColor(VertexColor.CYAN, weight));
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
