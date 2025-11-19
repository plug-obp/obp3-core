package obp3.modelchecking.buchi.ndfs.naive;

import obp3.Either;
import obp3.runtime.IExecutable;
import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.runtime.sli.IRootedGraph;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.ReRootedGraph;
import obp3.sli.core.operators.product.Product;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class EmptinessChecherBuchiNaiveNDFS<V, A> implements IExecutable<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>, EmptinessCheckerAnswer<V>> {

    IExecutable<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>, IDepthFirstTraversalConfiguration<V, A>> algorithm;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    int depthBound;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    Predicate<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>> hasToTerminatePredicate;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();

    public EmptinessChecherBuchiNaiveNDFS(
            IRootedGraph<V> graph,
            Predicate<V> acceptingPredicate
    ) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, -1, null, acceptingPredicate);
    }

    public EmptinessChecherBuchiNaiveNDFS(
            DepthFirstTraversal.Algorithm traversalAlgorithm,
            IRootedGraph<V> graph,
            int depthBound,
            Function<V, A> reducer,
            Predicate<V> acceptingPredicate) {
        algorithm = new DepthFirstTraversal<>(
                traversalAlgorithm,
                graph,
                depthBound,
                reducer,
                FunctionalDFTCallbacksModel.onEntry(this::onEntry)
        );
        this.traversalAlgorithm = traversalAlgorithm;
        this.graph = graph;
        this.depthBound = depthBound;
        this.reducer = reducer;
        this.acceptingPredicate = acceptingPredicate;
    }

    boolean onEntry(V source, V target, IDepthFirstTraversalConfiguration<V, A> configuration) {
        //if not a buchi accepting-state return false
        if (!acceptingPredicate.test(target)) {
            return false;
        }
        //if a buchi accepting state, look for a cycle
        var rerooted = new ReRootedGraph<>(graph, graph.neighbours(target));

        var algo = new DepthFirstTraversal<>(
                traversalAlgorithm,
                rerooted,
                depthBound,
                reducer,
                FunctionalDFTCallbacksModel.onEntry((s, t, _) -> {
                    if (t.equals( target )) {
                        result.holds = false;
                        result.witness = new Step<>(s==null ? target : s, Optional.empty(), t);
                        return true;
                    }
                    return false;
                })
        );

        var config = algo.run(hasToTerminatePredicate);
        if (result.holds) {
            return false;
        }
        result.addToTrace(config.getStack());
        return true;
    }

    @Override
    public EmptinessCheckerAnswer<V> run(Predicate<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>> hasToTerminatePredicate) {
        this.hasToTerminatePredicate = hasToTerminatePredicate;

        var config = algorithm.run(hasToTerminatePredicate);
        if (result.holds) {
            return result;
        }
        result.addToTrace(config.getStack());
        result.trace = result.trace.reversed();
        return result;
    }
}
