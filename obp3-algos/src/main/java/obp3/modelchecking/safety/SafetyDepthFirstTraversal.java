package obp3.modelchecking.safety;

import obp3.runtime.IExecutable;
import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.runtime.sli.IRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

public class SafetyDepthFirstTraversal<V, A> implements IExecutable<EmptinessCheckerAnswer<V>> {
    IExecutable<IDepthFirstTraversalConfiguration<V, A>> algorithm;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    int depthBound;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    BooleanSupplier hasToTerminateSupplier;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();

    public SafetyDepthFirstTraversal(
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
        //if not a accepting-state return false
        //if an accepting state, done
        if (acceptingPredicate.test(target)) {
            result.holds = false;
            result.witness = target;
            result.addToTrace(configuration.getStack());
            return true;
        }
        return false;
    }

    @Override
    public EmptinessCheckerAnswer<V> run(BooleanSupplier hasToTerminateSupplier) {
        this.hasToTerminateSupplier = hasToTerminateSupplier;
        algorithm.run(hasToTerminateSupplier);
        result.trace = result.trace.reversed();
        return result;
    }
}
