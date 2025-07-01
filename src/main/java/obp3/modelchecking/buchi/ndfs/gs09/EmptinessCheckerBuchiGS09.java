package obp3.modelchecking.buchi.ndfs.gs09;

import obp3.IExecutable;
import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.sli.core.IRootedGraph;
import obp3.sli.core.operators.ReRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

/***
 * The improved nested DFS algorithm from [1], Figure 1.
 * [1] Gaiser, Andreas, and Stefan Schwoon.
 * "Comparison of algorithms for checking emptiness on Büchi automata."
 * arXiv preprint arXiv:0910.3766 (2009).
 * https://arxiv.org/pdf/0910.3766.pdf
 *
 * the pseudocode:
 dfs₁(s, k = ∅)
 k = k ∪ { s→cyan }
 allRed = true
 for t ∈ next(s) do
 t.color = k @ t
 if t.color = cyan ∧ (s ∈ A ∨ t ∈ A) then
 report cycle
 end if
 if t ∉ k then
 dfs₁(t, k)
 end if
 if t.color ≠ red then
 allRed = false
 end if
 end for
 if allRed then
 k = k ∪ { s→red}
 else if s ∈ A then
 dfs₂(s, k)
 k = k ∪ { s→red}
 else
 k = k ∪ { s→blue}
 end if

 dfs₂(s, k)
 for t ∈ next(s) do
 t.color = k @ t
 if t.color = cyan then
 report cycle
 if t.color = blue then
 k = k ∪ { t→red}
 dfs₂ (t, k)
 end if
 end for
 */

public class EmptinessCheckerBuchiGS09<V, A> implements IExecutable<EmptinessCheckerAnswer<V>>  {
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    int depthBound;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    BooleanSupplier hasToTerminateSupplier;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();

    IExecutable<IDepthFirstTraversalConfiguration<V, A>> executable;

    public EmptinessCheckerBuchiGS09(
            IRootedGraph<V> graph,
            Predicate<V> acceptingPredicate
    ) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, -1, null, acceptingPredicate);
    }

    public EmptinessCheckerBuchiGS09(
        DepthFirstTraversal.Algorithm traversalAlgorithm,
        IRootedGraph<V> graph,
        int depthBound,
        Function<V, A> reducer,
        Predicate<V> acceptingPredicate) {
        this.traversalAlgorithm = traversalAlgorithm;
        this.graph = graph;
        this.depthBound = depthBound;
        this.reducer = reducer;
        this.acceptingPredicate = acceptingPredicate;

        var blueCallbacks = new FunctionalDFTCallbacksModel<>(
                this::onEntryBlue,
                this::onKnownBlue,
                this::onExitBlue
        );
        var model = new DepthFirstTraversalParameters<>(graph, reducer, blueCallbacks);
        var configuration = new BuchiGS09BlueConfiguration<>(model);

        executable = new DepthFirstTraversal<>(traversalAlgorithm, configuration);
    }

    boolean hasLoop(V source, V target, BuchiGS09BlueConfiguration<V, A> configuration) {
        //if target is not on the stack continue
        if (!configuration.getVertexColor(target).equals(VertexColor.CYAN)) return false;
        //    source is accepting
        // or target is accepting
        if (    acceptingPredicate.test(source)
            ||  acceptingPredicate.test(target)) {
            result.holds = false;
            result.witness = target;
            result.addToTrace(target, configuration.getStack());
            return true;
        }
        return false;
    }

    boolean onEntryBlue(V source, V target, IDepthFirstTraversalConfiguration<V, A> config) {
        var configuration = (BuchiGS09BlueConfiguration<V, A>)config;
        //add an allRed field to the current frame
        configuration.peek().allChildrenRed = true;
        return false;
    }

    boolean onKnownBlue(V source, V target, IDepthFirstTraversalConfiguration<V, A> config) {
        var configuration = (BuchiGS09BlueConfiguration<V, A>)config;
        if (hasLoop(source, target, configuration)) {return true;}
        //if (target) is not red,
        //then tell its parent (source) it has at least one non-red child
        if (!configuration.getVertexColor(target).equals(VertexColor.RED)) {
            configuration.peek().allChildrenRed = false;
        }
        return false;
    }

    boolean onExitBlue(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frameI, IDepthFirstTraversalConfiguration<V, A> config) {
        var configuration = (BuchiGS09BlueConfiguration<V, A>)config;
        var frame = (BuchiGS09BlueConfiguration.StackFrame<V>)frameI;
        //if all my children are red, make myself red
        if (frame.allChildrenRed) {
            configuration.changeVertexColor(vertex, VertexColor.RED);
            return false;
        }
        //if vertex is an accepting state dfs_red
        if (acceptingPredicate.test(vertex)) {
            dfsRed(vertex, configuration);
            if (result.holds) {
                configuration.changeVertexColor(vertex, VertexColor.RED);
                return false;
            }
            result.addToTrace(vertex, configuration.getStack());
            return true;
        }
        configuration.changeVertexColor(vertex, VertexColor.BLUE);
        //if I'm not red, tell my parent that I'm not
        if (!configuration.getStack().hasNext()) return false;
        configuration.peek().allChildrenRed = false;
        return false;
    }

    void dfsRed(V vertex, BuchiGS09BlueConfiguration<V, A> configuration) {
        var redCallbacks = FunctionalDFTCallbacksModel.onKnown(this::onKnownRed);
        var rerooted = new ReRootedGraph<>(graph, graph.neighbours(vertex));
        var model = new DepthFirstTraversalParameters<>(rerooted, depthBound, reducer, redCallbacks);
        var redConfig = new BuchiGS09RedConfiguration<>(model, configuration.known);
        var dfsRed = new DepthFirstTraversal<>(traversalAlgorithm, redConfig);
        dfsRed.run(hasToTerminateSupplier);
    }

    boolean onKnownRed(V source, V target, IDepthFirstTraversalConfiguration<V, A> config) {
        var configuration = (BuchiGS09RedConfiguration<V, A>)config;
        if (configuration.getVertexColor(target).equals(VertexColor.CYAN)) {
            result.holds = false;
            result.witness = target;
            result.addToTrace(target, configuration.getStack());
            return true;
        }
        return false;
    }

    @Override
    public EmptinessCheckerAnswer<V> run(BooleanSupplier hasToTerminateSupplier) {
        this.hasToTerminateSupplier = hasToTerminateSupplier;
        executable.run(hasToTerminateSupplier);
        result.trace = result.trace.reversed();
        return result;
    }
}
