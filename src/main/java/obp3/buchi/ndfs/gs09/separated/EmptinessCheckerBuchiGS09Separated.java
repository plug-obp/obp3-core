package obp3.buchi.ndfs.gs09.separated;

import obp3.IExecutable;
import obp3.buchi.ndfs.EmptinessCheckerAnswer;
import obp3.buchi.ndfs.gs09.VertexColor;
import obp3.sli.core.IRootedGraph;
import obp3.sli.core.operators.ReRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.DepthFirstTraversalParameters;
import obp3.traversal.dfs.model.FunctionalDFTCallbacksModel;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;
import obp3.traversal.dfs.semantics.DepthFirstTraversalDo;
import obp3.traversal.dfs.semantics.DepthFirstTraversalRelational;
import obp3.traversal.dfs.semantics.DepthFirstTraversalWhile;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

/***
 * The improved nested DFS algorithm from [1], Figure 1.
 * [1] Gaiser, Andreas, and Stefan Schwoon.
 * "Comparison of algorithms for checking emptiness on Büchi automata."
 * arXiv preprint arXiv:0910.3766 (2009).
 * https://arxiv.org/pdf/0910.3766.pdf
 * This version defines and uses callback specific data (memory variable),
 * as opposed to the more optimized version EmptinessCheckerBuchiGS09, which piggybacks on the DFT known and stack.
 */
public class EmptinessCheckerBuchiGS09Separated<V, A> implements IExecutable<EmptinessCheckerAnswer<V>> {
    IExecutable<IDepthFirstTraversalConfiguration<V, A>> traversal;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    BooleanSupplier hasToTerminateSupplier;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();
    record Memory<X>(Map<X, VertexColor> colorMap, Deque<Boolean[]> allMyChildreAreRedStack) { }
    Memory<V> memory = new Memory<>(new HashMap<>(), new ArrayDeque<>());

    public EmptinessCheckerBuchiGS09Separated(
            IRootedGraph<V> graph,
            Predicate<V> acceptingPredicate
    ) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, null, acceptingPredicate);
    }

    public EmptinessCheckerBuchiGS09Separated(
            DepthFirstTraversal.Algorithm traversalAlgorithm,
            IRootedGraph<V> graph,
            Function<V, A> reducer,
            Predicate<V> acceptingPredicate) {
        //the first DFT checks the accepting predicate in postorder (on_exit)
        traversal = new DepthFirstTraversal<>(
                traversalAlgorithm,
                graph,
                reducer,
                new FunctionalDFTCallbacksModel<>(this::onEntryBlue, this::onKnownBlue, this::onExitBlue)
        );
        this.traversalAlgorithm = traversalAlgorithm;
        this.graph = graph;
        this.reducer = reducer;
        this.acceptingPredicate = acceptingPredicate;
    }

    boolean onEntryBlue(V source, V target, IDepthFirstTraversalConfiguration<V, A> config) {
        memory.colorMap.put(target, VertexColor.CYAN);
        memory.allMyChildreAreRedStack.push(new Boolean[] { true });
        return false;
    }

    boolean hasLoop(V source, V target, IDepthFirstTraversalConfiguration<V, A> configuration) {
        //if target is not on the stack continue
        if (!getColor(target).equals(VertexColor.CYAN)) return false;
        //    source is accepting
        // or target is accepting
        if (    acceptingPredicate.test(source)
                ||  acceptingPredicate.test(target)) {
            result.holds = false;
            result.witness = target;
            result.trace.add(target);
            var stackI = configuration.getStack();
            while (stackI.hasNext()) {
                var x = stackI.next().vertex();
                if (x == null) break;
                result.trace.add(x);
            }
            return true;
        }
        return false;
    }

    boolean onKnownBlue(V source, V target, IDepthFirstTraversalConfiguration<V, A> config) {
        if (hasLoop(source, target, config)) return true;
        //if (target) is not red, the tell its parent (source) it has at least one non-red child
        if (!getColor(target).equals(VertexColor.RED)) {
            memory.allMyChildreAreRedStack.peek()[0] = false;
        }
        return false;
    }

    boolean onExitBlue(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frameI, IDepthFirstTraversalConfiguration<V, A> configuration) {
        var allRed = memory.allMyChildreAreRedStack.pop();
        //if all my children are red, make myself red
        if (allRed[0]) {
            memory.colorMap.put(vertex, VertexColor.RED);
            return false;
        }
        //if n is an accepting state dfs_red
        if (acceptingPredicate.test(vertex)) {
            dfsRed(vertex, configuration);
            if (result.holds) {
                memory.colorMap.put(vertex, VertexColor.RED);
                return false;
            }
            var stackI = configuration.getStack();
            result.trace.add(vertex);
            while (stackI.hasNext()) {
                var x = stackI.next().vertex();
                if (x == null) break;
                result.trace.add(x);
            }
            return true;
        }
        memory.colorMap.put(vertex, VertexColor.BLUE);
        //if I'm not red tell my parent that I'm not
        if (memory.allMyChildreAreRedStack.isEmpty()) return false;
        memory.allMyChildreAreRedStack.peek()[0] = false;
        return false;
    }

    static class DFSRedConfiguration<V, A> extends DFTConfigurationSetDeque<V, A> {
        Map<V, VertexColor> colorMap;
        public DFSRedConfiguration(
                IDepthFirstTraversalParameters<V, A> model,
                Set<V> known,
                Map<V, VertexColor> colorMap) {
            super(model, known, new ArrayDeque<>());
            this.colorMap = colorMap;
        }

        @Override
        public boolean knows(V vertex) {
            return !colorMap.getOrDefault(vertex, VertexColor.WHITE).equals(VertexColor.BLUE);
        }

        @Override
        public void add(V vertex) {
            super.add(vertex);
            colorMap.put(vertex, VertexColor.RED);
        }
    }
    void dfsRed(V vertex, IDepthFirstTraversalConfiguration<V, A> configuration) {
        var rerooted = new ReRootedGraph<>(graph, graph.neighbours(vertex));
        var redModel = new DepthFirstTraversalParameters<>(
                rerooted, reducer, FunctionalDFTCallbacksModel.onKnown(this::onKnownRed)
        );
        var redConfig = new DFSRedConfiguration<>(
                redModel,
                (Set<V>)configuration.getKnown(),
                memory.colorMap
        );
        var dfsRed = switch (traversalAlgorithm) {
            case WHILE ->
                    new DepthFirstTraversalWhile<>(redConfig);
            case RELATIONAL ->
                    new DepthFirstTraversalRelational<>(redConfig);
            case DO ->
                    new DepthFirstTraversalDo<>(redConfig);
        };
        dfsRed.run(hasToTerminateSupplier);
    }

    boolean onKnownRed(V source, V target, IDepthFirstTraversalConfiguration<V, A> configuration) {
        if (getColor(target).equals(VertexColor.CYAN)) {
            result.holds = false;
            result.witness = target;
            result.trace.add(target);
            var stackI = configuration.getStack();
            while (stackI.hasNext()) {
                var x = stackI.next().vertex();
                if (x == null) break;
                result.trace.add(x);
            }
            return true;
        }
        return false;
    }

    private VertexColor getColor(V target) {
        return memory.colorMap.getOrDefault(target, VertexColor.WHITE);
    }

    @Override
    public EmptinessCheckerAnswer<V> run(BooleanSupplier hasToTerminateSupplier) {
        this.hasToTerminateSupplier = hasToTerminateSupplier;
        traversal.run(hasToTerminateSupplier);
        result.trace = result.trace.reversed();
        return result;
    }
}
