package obp3.modelchecking.safety;

import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.EmptinessCheckerExecutable;
import obp3.modelchecking.EmptinessCheckerStatus;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.product.Product;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import obp3.utils.Either;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SafetyDepthFirstTraversal<V, A> implements EmptinessCheckerExecutable<V> {
    IExecutable<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>, IDepthFirstTraversalConfiguration<V, A>> algorithm;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    int depthBound;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    Predicate<EmptinessCheckerStatus> hasToTerminatePredicate;

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
            result.witness = new Step<>(source, Optional.empty(), target);
            result.addToTrace(configuration.getStack());
            return true;
        }
        return false;
    }

    private final EmptinessCheckerStatus status = new EmptinessCheckerStatus();

    @Override
    public EmptinessCheckerAnswer<V> run(Predicate<EmptinessCheckerStatus> hasToTerminatePredicate) {
        this.hasToTerminatePredicate = hasToTerminatePredicate;
        algorithm.run((c) -> EmptinessCheckerStatus.statusCallback(status, c, hasToTerminatePredicate));
        result.trace = result.trace.reversed();
        return result;
    }
}
