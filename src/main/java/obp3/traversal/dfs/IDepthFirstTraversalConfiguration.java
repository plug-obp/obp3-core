package obp3.traversal.dfs;

import obp3.sli.core.IRootedGraph;
import obp3.things.PeekableIterator;

import java.util.Set;

public interface IDepthFirstTraversalConfiguration<V> {
    record StackFrame<V>(V vertex, PeekableIterator<V> neighbours) { }

    IRootedGraph<V> getGraph();

    StackFrame<V> peek();
    void pop();
    void push(StackFrame<V> vertex);


    boolean knows(V vertex);
    void add(V vertex);
    default void discover(V vertex) {
        add(vertex);
        push(
                new StackFrame<>(
                        vertex,
                        new PeekableIterator<>(getGraph().neighbours(vertex))));
    }

    Set<V> getKnown();
}
