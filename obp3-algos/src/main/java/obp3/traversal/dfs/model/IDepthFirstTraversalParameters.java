package obp3.traversal.dfs.model;

import obp3.runtime.sli.IRootedGraph;

public interface IDepthFirstTraversalParameters<V, A> {
    IRootedGraph<V> getGraph();

    int getDepthBound();

    A reduce(V v);
    boolean hasReduction();

    IDepthFirstTraversalCallbacksModel<V, A> callbacks();

    default boolean hasCallbacks() { return true; }

    default boolean deterministicProduct() {
        return true;
    }
}
