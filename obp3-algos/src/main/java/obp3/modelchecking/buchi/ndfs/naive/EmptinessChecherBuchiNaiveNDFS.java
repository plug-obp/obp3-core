package obp3.modelchecking.buchi.ndfs.naive;

import obp3.runtime.IExecutable;
import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.runtime.sli.IRootedGraph;
import obp3.sli.core.operators.ReRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class EmptinessChecherBuchiNaiveNDFS<V, A> implements IExecutable<EmptinessCheckerAnswer<V>> {

    IExecutable<IDepthFirstTraversalConfiguration<V, A>> algorithm;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    int depthBound;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    BooleanSupplier hasToTerminateSupplier;

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
                FunctionalDFTCallbacksModel.onEntry((_, t, _) -> {
                    if (t.equals( target )) {
                        result.holds = false;
                        result.witness = target;
                        return true;
                    }
                    return false;
                })
        );

        var config = algo.run(hasToTerminateSupplier);
        if (result.holds) {
            return false;
        }
        result.addToTrace(config.getStack());
        return true;
    }

    @Override
    public EmptinessCheckerAnswer<V> run(BooleanSupplier hasToTerminateSupplier) {
        this.hasToTerminateSupplier = hasToTerminateSupplier;

        var config = algorithm.run(hasToTerminateSupplier);
        if (result.holds) {
            return result;
        }
        result.addToTrace(config.getStack());
        result.trace = result.trace.reversed();
        return result;
    }
}
