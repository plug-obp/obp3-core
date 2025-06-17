package obp3.buchi.ndfs.naive;

import obp3.IExecutable;
import obp3.buchi.ndfs.EmptinessCheckerAnswer;
import obp3.sli.core.IRootedGraph;
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
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    BooleanSupplier hasToTerminateSupplier;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();

    public EmptinessChecherBuchiNaiveNDFS(
            IRootedGraph<V> graph,
            Predicate<V> acceptingPredicate
    ) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, null, acceptingPredicate);
    }

    public EmptinessChecherBuchiNaiveNDFS(
            DepthFirstTraversal.Algorithm traversalAlgorithm,
            IRootedGraph<V> graph,
            Function<V, A> reducer,
            Predicate<V> acceptingPredicate) {
        algorithm = new DepthFirstTraversal<>(
                traversalAlgorithm,
                graph,
                reducer,
                FunctionalDFTCallbacksModel.onEntry(this::onEntry)
        );
        this.traversalAlgorithm = traversalAlgorithm;
        this.graph = graph;
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
        var stackI = config.getStack();
        while (stackI.hasNext()) {
            var vertex = stackI.next().vertex();
            if (vertex == null) break;
            result.trace.add(vertex);
        }

        return true;
    }

    @Override
    public EmptinessCheckerAnswer<V> run(BooleanSupplier hasToTerminateSupplier) {
        this.hasToTerminateSupplier = hasToTerminateSupplier;

        var config = algorithm.run(hasToTerminateSupplier);
        if (result.holds) {
            return result;
        }
        var stackI = config.getStack();
        while (stackI.hasNext()) {
            var vertex = stackI.next().vertex();
            if (vertex == null) break;
            result.trace.add(vertex);
        }
        result.trace = result.trace.reversed();
        return result;
    }
}
