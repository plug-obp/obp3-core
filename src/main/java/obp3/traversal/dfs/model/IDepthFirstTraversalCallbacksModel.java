package obp3.traversal.dfs.model;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

public interface IDepthFirstTraversalCallbacksModel<V, A> {
    //source, node, canonical
    ///{@code onKnown} called the first time the node is discovered,
    /// the arguments are
    /// - the source vertex, from which the current was reached
    /// - the current vertex, which was just discovered
    /// - the canonized vertex
    boolean onEntry(V source, V vertex, A canonical);

    /// {@code onKnown} - is called on sharing-links and back-loops
    /// the arguments are
    /// - the source vertex, from which the current was reached
    /// - the current vertex, which was just discovered
    /// - the canonized vertex
    boolean onKnown(V source, V vertex, A canonical);

    /// {@code onExit} called when exiting a node during backtracking
    boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame);
}
