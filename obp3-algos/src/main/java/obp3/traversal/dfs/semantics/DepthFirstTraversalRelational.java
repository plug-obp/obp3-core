package obp3.traversal.dfs.semantics;

import obp3.Either;
import obp3.runtime.IExecutable;
import obp3.Sequencer;
import obp3.sli.core.operators.ToDetermistic;
import obp3.sli.core.operators.product.Product;
import obp3.sli.core.operators.product.StepSynchronousProductSemantics;
import obp3.sli.core.operators.product.deterministic.DeterministicStepSynchronousProductSemantics;
import obp3.sli.core.operators.product.deterministic.model.DeterministicStepProductParameters;
import obp3.sli.core.operators.product.model.StepProductParameters;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.semantics.relational.DepthFirstTraversalRelation;
import obp3.traversal.dfs.semantics.relational.callbacks.DepthFirstTraversalCallbackSemantics;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class DepthFirstTraversalRelational<V, A> implements IExecutable<Either<IDepthFirstTraversalConfiguration<V, A>,  Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>, IDepthFirstTraversalConfiguration<V, A>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalRelational(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    public IDepthFirstTraversalConfiguration<V, A> run(
            Predicate<Either<IDepthFirstTraversalConfiguration<V, A>,  Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>>> hasToTerminatePredicate) {
        if (!configuration.getModel().hasCallbacks()) {
            var relation = new DepthFirstTraversalRelation<>(configuration);
            var sequencer = new Sequencer<>(relation);
            var result = sequencer.run(c-> hasToTerminatePredicate.test(Either.left(c)));
            return result.orElse(null);
        }
        if (configuration.getModel().deterministicProduct()) {
            var lhs = new DepthFirstTraversalRelation<>(configuration);
            var rhs = new DepthFirstTraversalCallbackSemantics<>(configuration.getModel().callbacks());
            var synch = new DeterministicStepSynchronousProductSemantics<>(new DeterministicStepProductParameters<>(lhs, rhs));
            var sequencer = new Sequencer<>(synch);
            var result = sequencer.run(c -> hasToTerminatePredicate.test(Either.right(c)));
            return result.map(Product::l).orElse(null);
        }
        var lhs = new DepthFirstTraversalRelation<>(configuration).toSemanticRelation();
        var rhs = new DepthFirstTraversalCallbackSemantics<>(configuration.getModel().callbacks()).toISemanticRelation();
        var synch = new StepSynchronousProductSemantics<>(new StepProductParameters<>(lhs, rhs));
        var det = ToDetermistic.anyPolicy(synch);
        var sequencer = new Sequencer<>(det);
        var result = sequencer.run(c-> hasToTerminatePredicate.test(Either.right(c)));
        return result.map(Product::l).orElse(null);
    }


}
