package obp3.traversal.dfs.model;

import obp3.runtime.sli.IRootedGraph;

import java.util.function.Function;

public class DepthFirstTraversalParameters<V, A> implements IDepthFirstTraversalParameters<V, A> {

    private final IRootedGraph<V> graph;
    private final int depthBound;
    private final Function<V, A> reducer;
    private final IDepthFirstTraversalCallbacksModel<V, A> callbacksModel;
    private final boolean deterministicProduct;

    public DepthFirstTraversalParameters(IRootedGraph<V> graph, int depthBound, Function<V,A> reducer, IDepthFirstTraversalCallbacksModel<V,A> callbacksModel) {
        this(graph, depthBound, reducer, callbacksModel, true);
    }

    @Override
    public boolean deterministicProduct() {
        return deterministicProduct;
    }

    public DepthFirstTraversalParameters(IRootedGraph<V> graph) {
        this(graph, (Function<V, A>)null);
    }

    public DepthFirstTraversalParameters(IRootedGraph<V> graph, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(graph, null, callbacksModel);
    }

    public DepthFirstTraversalParameters(IRootedGraph<V> graph, Function<V, A> reducer) {
        this(graph, reducer, FunctionalDFTCallbacksModel.none());
    }

    public DepthFirstTraversalParameters(IRootedGraph<V> graph, int depthBound, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(graph, depthBound, null, callbacksModel, true);
    }

    public DepthFirstTraversalParameters(IRootedGraph<V> graph, Function<V, A> reducer, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(graph, -1, reducer, callbacksModel, true);
    }

    public DepthFirstTraversalParameters(
            IRootedGraph<V> graph,
            int depthBound,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel,
            boolean deterministicProduct) {
        this.graph = graph;
        this.depthBound = depthBound;
        this.reducer = reducer;
        this.callbacksModel = callbacksModel;
        this.deterministicProduct = deterministicProduct;
    }

    @Override
    public IRootedGraph<V> getGraph() {
        return graph;
    }

    @Override
    public int getDepthBound() {
        return depthBound;
    }

    @Override
    public A reduce(V v) {
        return reducer.apply(v);
    }

    @Override
    public boolean hasReduction() {
        return reducer != null && reducer != Function.identity();
    }

    @Override
    public boolean hasCallbacks() {
        return callbacksModel != null && callbacksModel != FunctionalDFTCallbacksModel.none();
    }
    @Override
    public IDepthFirstTraversalCallbacksModel<V, A> callbacks() {
        return callbacksModel;
    }
}
