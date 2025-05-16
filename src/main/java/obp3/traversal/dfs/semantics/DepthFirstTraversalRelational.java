package obp3.traversal.dfs.semantics;

import obp3.IExecutable;
import obp3.Sequencer;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.semantics.relational.DepthFirstTraversalRelation;

import java.util.Collections;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalRelational<V, A> implements IExecutable<Set<V>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalRelational(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var relation = new DepthFirstTraversalRelation<>(configuration);
        var sequencer = new Sequencer<>(relation);
        var configuration = sequencer.run(hasToTerminateSupplier);
        return configuration.map(IDepthFirstTraversalConfiguration::getKnown).orElse(Collections.emptySet());
    }
}
