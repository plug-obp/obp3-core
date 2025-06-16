package obp3.scc.tarjan2;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

/// Implementation of the Algorithm T from
/// [Robert E. Tarjan, Uri Zwick, *Finding Strong Components Using Depth-First Search*](https://arxiv.org/pdf/2201.07197)
/// This version isolates the state-components needed by the Tarjan T algorithm from the underlying DFT-specific state.
/// The TarjanMemory captures the T-algorithm specific state, which is then used by the callbacks
/// While this implementation is good, it closes the traversal (it does not re-exposes the callbacks).
/// -- a better (conceptually at least) version would use a callbacks semantics that captures the Tarjan state-components
/// along with the callback semantics configuration, so that when composing again we can observe this part of the state also.
/// It would be interesting to see if through another synchronous composition we can obtain an efficient
///  buchi acceptance cycle detection, without touching the underlying Tarjan.
/// It seems to me that the acceptance cycle detection part could act as a pruning monitor over Tarjan.
public class TarjanStronglyConnectedComponentsAlgoT<V, A> implements IExecutable<TarjanMemory<V>> {

    final TarjanCallbacks<V, A> tarjanCallbacks = new TarjanCallbacks<>();
    final IExecutable<IDepthFirstTraversalConfiguration<V, A>> algorithm;

    public TarjanStronglyConnectedComponentsAlgoT(IRootedGraph<V> graph) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, null);
    }

    public TarjanStronglyConnectedComponentsAlgoT(IRootedGraph<V> graph, Function<V, A> reducer) {
        this(DepthFirstTraversal.Algorithm.WHILE, graph, reducer);
    }

    public TarjanStronglyConnectedComponentsAlgoT(DepthFirstTraversal.Algorithm traversalAlgorithm, IRootedGraph<V> graph) {
        this(traversalAlgorithm, graph, null);
    }

    public TarjanStronglyConnectedComponentsAlgoT(DepthFirstTraversal.Algorithm traversalAlgorithm, IRootedGraph<V> graph, Function<V, A> reducer) {
        algorithm = new DepthFirstTraversal<>(traversalAlgorithm, graph, reducer, tarjanCallbacks);
    }

    @Override
    public TarjanMemory<V> run(BooleanSupplier hasToTerminateSupplier) {
        algorithm.run(hasToTerminateSupplier);
        return tarjanCallbacks.memory;
    }
}
