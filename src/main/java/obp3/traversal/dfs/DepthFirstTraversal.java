package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;
import obp3.traversal.dfs.semantics.DepthFirstTraversalDo;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class DepthFirstTraversal<V, A> implements IExecutable<IDepthFirstTraversalConfiguration<V, A>> {
    public enum Algorithm {
        WHILE,
        RELATIONAL,
        DO
    }

    final IExecutable<IDepthFirstTraversalConfiguration<V, A>> algorithm;

    public DepthFirstTraversal(IRootedGraph<V> graph) {
        this(Algorithm.WHILE, graph, null, null);
    }

    public DepthFirstTraversal(IRootedGraph<V> graph, Function<V, A> reducer) {
        this(Algorithm.WHILE, graph, reducer, null);
    }

    public DepthFirstTraversal(IRootedGraph<V> graph, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(Algorithm.WHILE, graph, null, callbacksModel);
    }

    public DepthFirstTraversal(IRootedGraph<V> graph, Function<V, A> reducer, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(Algorithm.WHILE, graph, reducer, callbacksModel);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph) {
        this(algorithm, graph, null, null);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, null, callbacksModel);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph, Function<V, A> reducer) {
        this(algorithm, graph, reducer, null);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            int depthBound,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, depthBound, null, callbacksModel, true);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, -1, reducer, callbacksModel, true);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            int depthBound,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, depthBound, reducer, callbacksModel, true);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
           IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.algorithm = switch (algorithm) {
            case WHILE ->
                    new DepthFirstTraversalWhile<>(configuration);
            case RELATIONAL ->
                    new DepthFirstTraversalRelational<>(configuration);
            case DO ->
                    new DepthFirstTraversalDo<>(configuration);
        };
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            int depthBound,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel,
            boolean deterministicProduct) {
        var model = new DepthFirstTraversalParameters<>(
                graph,
                depthBound,
                reducer,
                callbacksModel == null ? FunctionalDFTCallbacksModel.none() : callbacksModel,
                deterministicProduct);
        IDepthFirstTraversalConfiguration<V, A> configuration = new DFTConfigurationSetDeque<>(model);
        this.algorithm = switch (algorithm) {
            case WHILE ->
                    new DepthFirstTraversalWhile<>(configuration);
            case RELATIONAL ->
                    new DepthFirstTraversalRelational<>(configuration);
            case DO ->
                    new DepthFirstTraversalDo<>(configuration);
        };
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> run(BooleanSupplier hasToTerminateSupplier) {
        return algorithm.run(hasToTerminateSupplier);
    }
}
