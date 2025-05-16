package obp3.traversal.dfs;

import obp3.things.PeekableIterator;

import java.util.Set;

public interface IDepthFirstTraversalConfiguration<V, A> {
    record StackFrame<V>(V vertex, PeekableIterator<V> neighbours) { }

    //access to the inputs
    IDepthFirstTraversalParameters<V, A> getModel();

    //Stack management
    StackFrame<V> peek();
    void pop();
    void push(StackFrame<V> vertex);


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
    Set<V> getKnown();
}
