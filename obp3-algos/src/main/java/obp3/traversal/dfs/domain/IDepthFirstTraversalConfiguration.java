package obp3.traversal.dfs.domain;

import obp3.utils.PeekableIterator;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.Iterator;
import java.util.Set;

public interface IDepthFirstTraversalConfiguration<V, A> {
    class StackFrame<V> {
        V vertex;
        PeekableIterator<V> neighbours;
        public StackFrame(V vertex, PeekableIterator<V> neighbours) {
            this.vertex = vertex;
            this.neighbours = neighbours;
        }
        public V vertex() { return vertex; }
        public PeekableIterator<V> neighbours() { return neighbours; }
    }

    //access to the inputs
    IDepthFirstTraversalParameters<V, A> getModel();

    IDepthFirstTraversalConfiguration<V, A> initial();

    //Stack management
    StackFrame<V> peek();
    StackFrame<V> pop();
    void push(StackFrame<V> vertex);
    Iterator<StackFrame<V>> getStack();
    int stackSize();

    //Known management
    boolean knows(V vertex);
    void add(V vertex);

    default void discover(V vertex) {
        add(vertex);
        push(
                new StackFrame<>(
                        vertex,
                        new PeekableIterator<>(getModel().getGraph().neighbours(vertex))));
    }

    //retrieve a set representation of the known if possible
    Set<Object> getKnown();
}
