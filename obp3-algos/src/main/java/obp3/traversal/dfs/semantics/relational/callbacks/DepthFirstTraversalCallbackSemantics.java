package obp3.traversal.dfs.semantics.relational.callbacks;

import obp3.sli.core.IDeterministicSematicRelation;
import obp3.sli.core.operators.product.Step;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalCallbacksModel;
import obp3.traversal.dfs.semantics.relational.actions.BacktrackAction;
import obp3.traversal.dfs.semantics.relational.actions.DepthFirstTraversalAction;
import obp3.traversal.dfs.semantics.relational.actions.KnownConfigurationAction;
import obp3.traversal.dfs.semantics.relational.actions.UnknownConfigurationAction;

import java.util.Optional;
import java.util.function.Supplier;

public class DepthFirstTraversalCallbackSemantics<V, A>
        implements IDeterministicSematicRelation<
                    Step<DepthFirstTraversalAction<V, A>, IDepthFirstTraversalConfiguration<V, A>>,
                Supplier<Boolean>, Boolean> {
    IDepthFirstTraversalCallbacksModel<V, A> callbacksModel;
    public DepthFirstTraversalCallbackSemantics(IDepthFirstTraversalCallbacksModel<V, A> callbacksModel) {
        this.callbacksModel = callbacksModel;
    }
    @Override
    public Optional<Boolean> initial() {
        return Optional.of(true);
    }

    @Override
    public Optional<Supplier<Boolean>> actions(
                    Step<DepthFirstTraversalAction<V, A>,
                    IDepthFirstTraversalConfiguration<V, A>> input,
                    Boolean configuration) {
        if (!configuration) return Optional.empty();
        if (input.action().isEmpty()) { return Optional.empty(); }
        return switch (input.action().get()) {
            case UnknownConfigurationAction(V s, V v, var conf) -> Optional.of(() -> callbacksModel.onEntry(s, v, conf));
            case KnownConfigurationAction(V s, V v, var conf) -> Optional.of(() -> callbacksModel.onKnown(s, v, conf));
            case BacktrackAction(V v, IDepthFirstTraversalConfiguration.StackFrame<V> frame, var conf) ->
                    input.end().peek() != null ?
                        Optional.of(() -> callbacksModel.onExit(v, frame, conf)) : Optional.empty();
        };
    }

    @Override
    public Optional<Boolean> execute(Supplier<Boolean> action, Step<DepthFirstTraversalAction<V, A>, IDepthFirstTraversalConfiguration<V, A>> input, Boolean configuration) {
        return Optional.of(!action.get());
    }
}
