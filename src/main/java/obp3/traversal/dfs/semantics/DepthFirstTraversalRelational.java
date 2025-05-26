package obp3.traversal.dfs.semantics;

import obp3.IExecutable;
import obp3.Sequencer;
import obp3.sli.core.DeterministicSemanticRelation;
import obp3.sli.core.operators.ToDetermistic;
import obp3.sli.core.operators.product.Product;
import obp3.sli.core.operators.product.StepSynchronousProductSemantics;
import obp3.sli.core.operators.product.model.StepProductParameters;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.semantics.relational.DepthFirstTraversalRelation;
import obp3.traversal.dfs.semantics.relational.callbacks.DepthFirstTraversalCallbackSemantics;

import java.util.function.BooleanSupplier;

public class DepthFirstTraversalRelational<V, A> implements IExecutable<IDepthFirstTraversalConfiguration<V, A>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalRelational(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    public IDepthFirstTraversalConfiguration<V, A> run(BooleanSupplier hasToTerminateSupplier) {
        if (!configuration.getModel().hasCallbacks()) {
            var relation = new DepthFirstTraversalRelation<>(configuration);
            var sequencer = new Sequencer<>(relation);
            var result = sequencer.run(hasToTerminateSupplier);
            return result.orElse(null);
        }
        var lhs = new DepthFirstTraversalRelation<>(configuration).toSemanticRelation();
        var rhs = new DepthFirstTraversalCallbackSemantics<>(configuration.getModel().callbacks()).toISemanticRelation();
        var synch = new StepSynchronousProductSemantics<>(new StepProductParameters<>(lhs, rhs));
        var det = ToDetermistic.anyPolicy(synch);
        var sequencer = new Sequencer<>(det);
        var result = sequencer.run(hasToTerminateSupplier);
        return result.map(Product::l).orElse(null);
    }
}
