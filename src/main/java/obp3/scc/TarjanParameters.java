package obp3.scc;

import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

public class TarjanParameters<V> implements IDepthFirstTraversalParameters<V, V> {
    private IRootedGraph<V> graph;

    public TarjanParameters(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public IRootedGraph<V> getGraph() {
        return graph;
    }

    @Override
    public V reduce(V v) {
        return v;
    }

    @Override
    public IDepthFirstTraversalCallbacksModel<V, V> callbacks() {
        return new TarjanCallbacks<>();
    }
}
