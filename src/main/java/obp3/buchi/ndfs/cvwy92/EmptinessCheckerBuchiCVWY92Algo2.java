package obp3.buchi.ndfs.cvwy92;

import obp3.IExecutable;
import obp3.buchi.ndfs.EmptinessCheckerAnswer;
import obp3.sli.core.IRootedGraph;
import obp3.sli.core.operators.ReRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

/***
 * CVWY92_Algorithm2 is the algorithm 2 from [1].
 * The recursive pseudocode seems to be:
 {@code
 dfs₁(s, k₁ = ∅, k₂ = ∅)
 k₁ = k₁ ∪ { s }
 for t ∈ next(s) do
 if t ∉ k₁ then
 dfs₁(t, k₁, k₂)
 end if
 end for
 if s ∈ accepting then
 dfs₂(s, s, k₂)
 end if

 dfs₂(s, seed, k₂)
 k₂ = k₂ ∪ { s }
 if seed ∈ next(s) then
 report violation
 end if
 for t ∈ next(s) do
 if t ∉ k₂ then
 dfs₂ (t, k₂)
 end if
 end for
 }
 * [1] Courcoubetis, Costas, Moshe Vardi, Pierre Wolper, and Mihalis Yannakakis.
 * "Memory-efficient algorithms for the verification of temporal properties."
 * Formal methods in system design 1, no. 2 (1992): 275-288.
 */
public class EmptinessCheckerBuchiCVWY92Algo2<V, A> implements IExecutable <EmptinessCheckerAnswer<V>>{
    IExecutable<IDepthFirstTraversalConfiguration<V, A>> algorithm;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    BooleanSupplier hasToTerminateSupplier;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();


    public EmptinessCheckerBuchiCVWY92Algo2(
            IRootedGraph<V> graph,
            Predicate<V> acceptingPredicate
    ) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, null, acceptingPredicate);
    }

    public EmptinessCheckerBuchiCVWY92Algo2(
            DepthFirstTraversal.Algorithm traversalAlgorithm,
            IRootedGraph<V> graph,
            Function<V, A> reducer,
            Predicate<V> acceptingPredicate) {
        //the first DFT checks the accepting predicate in postorder (on_exit)
        algorithm = new DepthFirstTraversal<>(
                traversalAlgorithm,
                graph,
                reducer,
                FunctionalDFTCallbacksModel.onExit(this::onExit)
        );
        this.traversalAlgorithm = traversalAlgorithm;
        this.graph = graph;
        this.reducer = reducer;
        this.acceptingPredicate = acceptingPredicate;
    }

    boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame, IDepthFirstTraversalConfiguration<V, A> configuration) {
        //if not a buchi accepting-state return false
        if (!acceptingPredicate.test(vertex)) {
            return false;
        }

        //the second starts from the seed.
        var rerooted = new ReRootedGraph<>(graph, List.of(vertex).iterator());

        //the second DFS checks the accepting predicate in preorder (on_entry)
        var algo = new DepthFirstTraversal<>(
                traversalAlgorithm,
                rerooted,
                reducer,
                FunctionalDFTCallbacksModel.onEntry(
                        (_, v, _) ->
                        {
                            //if seed ∈ next(s) then report violation
                            var neighboursI = graph.neighbours(v);
                            while (neighboursI.hasNext()) {
                                var neighbour = neighboursI.next();
                                if (neighbour.equals(vertex)) {
                                    result.holds = false;
                                    result.witness = vertex;
                                    return true;
                                }
                            }
                            return false;
                        })
        );

        var config = algo.run(hasToTerminateSupplier);
        if (result.holds) return false;
        result.trace.add(vertex);
        var stackI = config.getStack();
        while (stackI.hasNext()) {
            var v = stackI.next().vertex();
            if (v == null) break;
            result.trace.add(v);
        }

        return true;
    }

    @Override
    public EmptinessCheckerAnswer<V> run(BooleanSupplier hasToTerminateSupplier) {
        this.hasToTerminateSupplier = hasToTerminateSupplier;

        var config = algorithm.run(hasToTerminateSupplier);
        if (result.holds) return result;
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
