package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.Sequencer;
import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.relational.DepthFirstTraversalRelation;

import java.util.Collections;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalRelational<V, A> implements IExecutable<Set<V>> {
    IDepthFirstTraversalParameters<V, A> model;

    public DepthFirstTraversalRelational(IDepthFirstTraversalParameters<V, A> model) {
        this.model = model;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var relation = new DepthFirstTraversalRelation<>(model);
        var sequencer = new Sequencer<>(relation);
        var configuration = sequencer.run(hasToTerminateSupplier);
        return configuration.map(DepthFirstTraversalConfiguration::getKnown).orElse(Collections.emptySet());
    }
}
