package obp3.traversal.dfs.defaults.domain;

import obp3.sli.core.IRootedGraph;
import obp3.things.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.*;

public class DFTConfigurationSetDeque<V, A> extends DFTConfiguration4TreeDeque<V, A> {
    private final Set<Object> known;

    public DFTConfigurationSetDeque(
            IDepthFirstTraversalParameters<V, A> model,
            Set<Object> known,
            Deque<StackFrame<V>> stack) {
        super(model, stack);
        this.known = known;
    }

    public DFTConfigurationSetDeque(IDepthFirstTraversalParameters<V, A> model) {
        this(model, new HashSet<>(), new ArrayDeque<>());
    }

    public static <X, Y> DFTConfigurationSetDeque<X, Y> initial(IDepthFirstTraversalParameters<X, Y> model) {
             return new DFTConfigurationSetDeque<>(model);
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> initial() {
        this.known.clear();
        return super.initial();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DFTConfigurationSetDeque<?, ?> other)) return false;
        return     super.equals(other)
                && Objects.equals(known, other.known);
    }

    @Override
    public int hashCode() {
        return Objects.hash(known, super.hashCode());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" +
                super.toString() + ", known=" + known + ")";
    }

    /// The reductedVertex is a local-to-transition variable,
    /// that means that is computed when selecting the transition and using when executing the transition.
    /// it only exists as a local cache so that the canonize function is not called twice
    public A reducedVertex;
    @Override
    public boolean knows(V vertex) {
        if (getModel().hasReduction()) {
            reducedVertex = getModel().reduce(vertex);
            return known.contains(reducedVertex);
        }
        return known.contains(vertex);
    }
    @Override
    public void add(V vertex) {
        if (getModel().hasReduction()) {
            known.add(reducedVertex);
            return;
        }
        known.add(vertex);
    }

    @Override
    public Set<Object> getKnown() { return known; }
}
