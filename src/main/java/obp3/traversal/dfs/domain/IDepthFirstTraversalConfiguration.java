package obp3.traversal.dfs.domain;

import obp3.things.PeekableIterator;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.Set;

public interface IDepthFirstTraversalConfiguration<V, A> {
    record StackFrame<V>(V vertex, PeekableIterator<V> neighbours) { }

    //access to the inputs
    IDepthFirstTraversalParameters<V, A> getModel();

    IDepthFirstTraversalConfiguration<V, A> initial();

    //Stack management
    StackFrame<V> peek();
    void pop();
    void push(StackFrame<V> vertex);


    //Known management
    boolean knows(V vertex, A reducedVertex);
    void add(V vertex, A reducedVertex);

    default void discover(V vertex, A reducedVertex) {
        add(vertex, reducedVertex);
        push(
                new StackFrame<>(
                        vertex,
                        new PeekableIterator<>(getModel().getGraph().neighbours(vertex))));
    }

    //retrieve a set representation of the known if possible
    Set getKnown();
}
