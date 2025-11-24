package obp3.modelchecking.buchi.ndfs.gs09.cdlp05.separated;

import obp3.modelchecking.EmptinessCheckerExecutable;
import obp3.modelchecking.EmptinessCheckerStatus;
import obp3.utils.Either;
import obp3.runtime.IExecutable;
import obp3.modelchecking.EmptinessCheckerAnswer;
import obp3.modelchecking.buchi.ndfs.gs09.VertexColor;
import obp3.modelchecking.buchi.ndfs.gs09.cdlp05.WeightedColor;
import obp3.runtime.sli.IRootedGraph;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.ReRootedGraph;
import obp3.sli.core.operators.product.Product;
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
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The improved nested DFS algorithm from [1], Figure 1 with the optimization from [2] (Sec. 4.2).
 * The number of accepting states accumulated from the source to the current node is associated with all nodes on the stack.
 * This way dfs blue can detect all recursive loops with accepting states.
 * Dfs red is used only to analyze cross(sharing) links.
 *
 * This version defines and uses callback specific data (memory variable),
 *  * as opposed to the more optimized version EmptinessCheckerBuchiGS09, which piggybacks on the DFT known and stack.
 *
 * [1] Gaiser, Andreas, and Stefan Schwoon.
 * "Comparison of algorithms for checking emptiness on Büchi automata."
 * arXiv preprint arXiv:0910.3766 (2009).
 * https://arxiv.org/pdf/0910.3766.pdf
 *
 * [2] Couvreur, Jean-Michel, Alexandre Duret-Lutz, and Denis Poitrenaud.
 * "On-the-fly emptiness checks for generalized Büchi automata."
 * In International SPIN Workshop on Model Checking of Software,
 * pp. 169-184. Springer, Berlin, Heidelberg, 2005.
 *
 */

public class EmptinessCheckerBuchiGS09CDLP05Separated<V, A> implements EmptinessCheckerExecutable<V> {
    IExecutable<Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>, IDepthFirstTraversalConfiguration<V, A>> traversal;
    DepthFirstTraversal.Algorithm traversalAlgorithm;
    IRootedGraph<V> graph;
    int depthBound;
    Function<V, A> reducer;
    Predicate<V> acceptingPredicate;
    Predicate<EmptinessCheckerStatus> hasToTerminatePredicate;

    EmptinessCheckerAnswer<V> result = new EmptinessCheckerAnswer<>();
    record Memory<X>(int[] weight, Map<X, WeightedColor> colorMap, Deque<Boolean[]> allMyChildreAreRedStack) { }
    Memory<V> memory = new Memory<>(new int[]{0}, new HashMap<>(), new ArrayDeque<>());

    public EmptinessCheckerBuchiGS09CDLP05Separated(
            IRootedGraph<V> graph,
            Predicate<V> acceptingPredicate
    ) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, -1, null, acceptingPredicate);
    }

    public EmptinessCheckerBuchiGS09CDLP05Separated(
            DepthFirstTraversal.Algorithm traversalAlgorithm,
            IRootedGraph<V> graph,
            int depthBound,
            Function<V, A> reducer,
            Predicate<V> acceptingPredicate) {
        //the first DFT checks the accepting predicate in postorder (on_exit)
        traversal = new DepthFirstTraversal<>(
                traversalAlgorithm,
                graph,
                depthBound,
                reducer,
                new FunctionalDFTCallbacksModel<>(this::onEntryBlue, this::onKnownBlue, this::onExitBlue)
        );
        this.traversalAlgorithm = traversalAlgorithm;
        this.graph = graph;
        this.depthBound = depthBound;
        this.reducer = reducer;
        this.acceptingPredicate = acceptingPredicate;
    }

    boolean onEntryBlue(V source, V target, IDepthFirstTraversalConfiguration<V, A> config) {
        memory.weight[0] += acceptingPredicate.test(target) ? 1 : 0;
        memory.colorMap.put(target, new WeightedColor(VertexColor.CYAN, memory.weight[0]));
        memory.allMyChildreAreRedStack.push(new Boolean[] { true });
        return false;
    }

    boolean hasLoop(V source, V target, IDepthFirstTraversalConfiguration<V, A> configuration) {
        //if target is not on the stack continue
        if (!getColor(target).equals(VertexColor.CYAN)) return false;
        //    source is accepting
        // or target is accepting
        if (   getWeight(source) - getWeight(target) != 0
                || acceptingPredicate.test(source)
                ||  acceptingPredicate.test(target)) {
            result.holds = false;
            result.witness = new Step<>(source, Optional.empty(), target);
            result.addToTrace(target, configuration.getStack());
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
        memory.weight[0] -= acceptingPredicate.test(vertex) ? 1 : 0;
        var allRed = memory.allMyChildreAreRedStack.pop();
        //if all my children are red, make myself red
        if (allRed[0]) {
            changeVertexColor(vertex, VertexColor.RED);
            return false;
        }
        //if n is an accepting state dfs_red
        if (acceptingPredicate.test(vertex)) {
            dfsRed(vertex, configuration);
            if (result.holds) {
                changeVertexColor(vertex, VertexColor.RED);
                return false;
            }
            result.addToTrace(vertex, configuration.getStack());
            return true;
        }
        changeVertexColor(vertex, VertexColor.BLUE);
        //if I'm not red tell my parent that I'm not
        if (memory.allMyChildreAreRedStack.isEmpty()) return false;
        memory.allMyChildreAreRedStack.peek()[0] = false;
        return false;
    }

    static class DFSRedConfiguration<V, A> extends DFTConfigurationSetDeque<V, A> {
        Map<V, WeightedColor> colorMap;
        public DFSRedConfiguration(
                IDepthFirstTraversalParameters<V, A> model,
                Set<Object> known,
                Map<V, WeightedColor> colorMap) {
            super(model, known, new ArrayDeque<>());
            this.colorMap = colorMap;
        }

        @Override
        public boolean knows(V vertex) {
            var weighedColor = colorMap.get(vertex);
            if (weighedColor == null) return true;
            return !weighedColor.color.equals(VertexColor.BLUE);
        }

        @Override
        public void add(V vertex) {
            super.add(vertex);
            //set the color to red
            var weightedColor = colorMap.get(vertex);
            if (weightedColor != null) {
                weightedColor.color = VertexColor.RED;
                return;
            }
            colorMap.put(vertex, new WeightedColor(VertexColor.RED, 0));
        }
    }
    void dfsRed(V vertex, IDepthFirstTraversalConfiguration<V, A> configuration) {
        var rerooted = new ReRootedGraph<>(graph, graph.neighbours(vertex));
        var redModel = new DepthFirstTraversalParameters<>(
                rerooted, depthBound, reducer, FunctionalDFTCallbacksModel.onKnown(this::onKnownRed)
        );
        var redConfig = new DFSRedConfiguration<>(
                redModel,
                configuration.getKnown(),
                memory.colorMap
        );

        var dfsRed = new DepthFirstTraversal<>(traversalAlgorithm, redConfig);
        var prefixStatus = new EmptinessCheckerStatus(status);
        dfsRed.run(c -> EmptinessCheckerStatus.statusCallback(new EmptinessCheckerStatus(0, status.worklistSize), status, c, hasToTerminatePredicate));
        status.reset(prefixStatus);
    }

    boolean onKnownRed(V source, V target, IDepthFirstTraversalConfiguration<V, A> configuration) {
        if (getColor(target).equals(VertexColor.CYAN)) {
            result.holds = false;
            result.witness = new Step<>(source, Optional.empty(), target);
            result.addToTrace(target, configuration.getStack());
            return true;
        }
        return false;
    }

    private VertexColor getColor(V vertex) {
        var weightedColor = memory.colorMap.get(vertex);
        if (weightedColor == null) {
            return VertexColor.WHITE;
        }
        return weightedColor.color;
    }

    public void changeVertexColor(V vertex, VertexColor newColor) {
        var weightedColor = memory.colorMap.get(vertex);
        if (weightedColor != null) {
            weightedColor.color = newColor;
            return;
        }
        memory.colorMap.put(vertex, new WeightedColor(newColor, memory.weight[0]));
    }

    private int getWeight(V vertex) {
        var weightedColor = memory.colorMap.get(vertex);
        if (weightedColor == null) {
            return memory.weight[0];
        }
        return weightedColor.weight;
    }

    private final EmptinessCheckerStatus status = new EmptinessCheckerStatus();

    @Override
    public EmptinessCheckerAnswer<V> run(Predicate<EmptinessCheckerStatus> hasToTerminatePredicate) {
        this.hasToTerminatePredicate = hasToTerminatePredicate;
        traversal.run(c -> EmptinessCheckerStatus.statusCallback(status, c, hasToTerminatePredicate));
        result.trace = result.trace.reversed();
        return result;
    }
}
