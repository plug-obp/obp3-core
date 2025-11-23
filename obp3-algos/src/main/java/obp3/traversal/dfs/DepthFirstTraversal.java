package obp3.traversal.dfs;

import obp3.utils.Either;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.operators.product.Product;
import obp3.traversal.dfs.defaults.domain.DFTConfiguration4TreeDeque;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;
import obp3.traversal.dfs.semantics.DepthFirstTraversalDo;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DepthFirstTraversal<V, A> implements IExecutable<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>, IDepthFirstTraversalConfiguration<V, A>> {
    public enum Algorithm {
        WHILE,
        RELATIONAL,
        DO
    }

    final IExecutable<?, IDepthFirstTraversalConfiguration<V, A>> algorithm;

    public DepthFirstTraversal(IRootedGraph<V> graph) {
        this(Algorithm.WHILE, graph, HashSet::new, null, null);
    }

    public DepthFirstTraversal(IRootedGraph<V> graph, Function<V, A> reducer) {
        this(Algorithm.WHILE, graph, reducer, null);
    }

    public DepthFirstTraversal(IRootedGraph<V> graph, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(Algorithm.WHILE, graph,HashSet::new, null, callbacksModel);
    }

    public DepthFirstTraversal(IRootedGraph<V> graph, Function<V, A> reducer, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(Algorithm.WHILE, graph, reducer, callbacksModel);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph) {
        this(algorithm, graph,HashSet::new, null, null);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph,HashSet::new, null, callbacksModel);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph, Supplier<Set<Object>> knownProvider, IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, knownProvider, null, callbacksModel);
    }

    public DepthFirstTraversal(Algorithm algorithm, IRootedGraph<V> graph, Function<V, A> reducer) {
        this(algorithm, graph, reducer, null);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            int depthBound,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, HashSet::new, depthBound, null, callbacksModel, true);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, HashSet::new, -1, reducer, callbacksModel, true);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            Supplier<Set<Object>> knownProvider,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, knownProvider, -1, reducer, callbacksModel, true);
    }

    public DepthFirstTraversal(
            Algorithm algorithm,
            IRootedGraph<V> graph,
            int depthBound,
            Function<V, A> reducer,
            IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this(algorithm, graph, HashSet::new, depthBound, reducer, callbacksModel, true);
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
            Supplier<Set<Object>> knownProvider,
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
        IDepthFirstTraversalConfiguration<V, A> configuration = null;
        if (graph.isTree()) {
            configuration = new DFTConfiguration4TreeDeque<>(model);
        } else {
            configuration = new DFTConfigurationSetDeque<>(model, knownProvider.get(), new ArrayDeque<>());
        }
        this.algorithm = switch (algorithm) {
            case WHILE ->
                    new DepthFirstTraversalWhile<>(configuration);
            case RELATIONAL ->
                    new DepthFirstTraversalRelational<>(configuration);
            case DO ->
                    new DepthFirstTraversalDo<>(configuration);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public IDepthFirstTraversalConfiguration<V, A> run(Predicate<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>> hasToTerminatePredicate) {
        if (algorithm instanceof DepthFirstTraversalRelational relational) {
            return relational.run(c -> hasToTerminatePredicate.test((Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>)c));
        }
        return algorithm.run(c -> hasToTerminatePredicate.test(Either.left((IDepthFirstTraversalConfiguration<V, A>) c)));
    }
}
