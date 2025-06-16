package obp3.traversal.dfs.model;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.Objects;
import java.util.function.Function;

public class FunctionalDFTCallbacksModel<V, A> implements IDepthFirstTraversalCallbacksModel<V, A> {
    final TriFunction<V, V, IDepthFirstTraversalConfiguration<V, A>, Boolean> onEntry;
    final TriFunction<V, V, IDepthFirstTraversalConfiguration<V, A>, Boolean> onKnown;
    final TriFunction<V, IDepthFirstTraversalConfiguration.StackFrame<V>, IDepthFirstTraversalConfiguration<V, A>, Boolean> onExit;

    public FunctionalDFTCallbacksModel(
            TriFunction<V, V, IDepthFirstTraversalConfiguration<V, A>, Boolean> onEntry,
            TriFunction<V, V, IDepthFirstTraversalConfiguration<V, A>, Boolean> onKnown,
            TriFunction<V, IDepthFirstTraversalConfiguration.StackFrame<V>, IDepthFirstTraversalConfiguration<V, A>, Boolean> onExit
    ) {
        this.onEntry = onEntry;
        this.onKnown = onKnown;
        this.onExit = onExit;
    }

    public FunctionalDFTCallbacksModel() {
        onEntry = null;
        onKnown = null;
        onExit = null;
    }

    static IDepthFirstTraversalCallbacksModel NONE;
    public static <V, A> IDepthFirstTraversalCallbacksModel<V, A> none() {
        if (NONE == null) {
            NONE = new FunctionalDFTCallbacksModel<>();
        }
        return (IDepthFirstTraversalCallbacksModel<V, A>) NONE;
    }

    public static <V, A> IDepthFirstTraversalCallbacksModel<V, A> onEntry(TriFunction<V, V, IDepthFirstTraversalConfiguration<V, A>, Boolean> onEntry) {
        return new FunctionalDFTCallbacksModel<>(onEntry, null, null);
    }

    public static <V, A> IDepthFirstTraversalCallbacksModel<V, A> onKnown(TriFunction<V, V, IDepthFirstTraversalConfiguration<V, A>, Boolean> onKnown) {
        return new FunctionalDFTCallbacksModel<>(null, onKnown, null);
    }

    public static <V, A> IDepthFirstTraversalCallbacksModel<V, A> onExit(TriFunction<V, IDepthFirstTraversalConfiguration.StackFrame<V>, IDepthFirstTraversalConfiguration<V, A>, Boolean> onExit) {
        return new FunctionalDFTCallbacksModel<>(null, null, onExit);
    }

    @Override
    public boolean onEntry(V source, V vertex, IDepthFirstTraversalConfiguration<V, A> configuration) {
        if (onEntry == null) {
            return false;
        }
        return onEntry.apply(source, vertex, configuration);
    }

    @Override
    public boolean onKnown(V source, V vertex, IDepthFirstTraversalConfiguration<V, A> configuration) {
        if (onKnown == null) {
            return false;
        }
        return onKnown.apply(source, vertex, configuration);
    }

    @Override
    public boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame, IDepthFirstTraversalConfiguration<V, A> configuration) {
        if (onExit == null) {
            return false;
        }
        return onExit.apply(vertex, frame, configuration);
    }

    public interface TriFunction<I1, I2, I3, O> {
        O apply(I1 i1, I2 i2, I3 i3);

        /**
         * Returns a composed function that first applies this function to
         * its input, and then applies the {@code after} function to the result.
         * If evaluation of either function throws an exception, it is relayed to
         * the caller of the composed function.
         *
         * @param <X> the type of output of the {@code after} function, and of the
         *           composed function
         * @param after the function to apply after this function is applied
         * @return a composed function that first applies this function and then
         * applies the {@code after} function
         * @throws NullPointerException if after is null
         */
        default <X> TriFunction<I1, I2, I3, X> andThen(Function<? super O, ? extends X> after) {
            Objects.requireNonNull(after);
            return (x, y, z) -> after.apply(apply(x, y, z));
        }
    }
}
