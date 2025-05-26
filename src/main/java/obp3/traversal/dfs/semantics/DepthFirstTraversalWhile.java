package obp3.traversal.dfs.semantics;

import obp3.IExecutable;
import obp3.traversal.dfs.defaults.domain.DFTConfigurationSetDeque;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.function.BooleanSupplier;

public class DepthFirstTraversalWhile<V, A> implements IExecutable<IDepthFirstTraversalConfiguration<V, A>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalWhile(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> run(BooleanSupplier hasToTerminateSupplier) {
        configuration.initial();
        DFTConfigurationSetDeque.StackFrame<V> stackFrame;
        while (     //did we finish ?
                    (stackFrame = configuration.peek()) != null
                    //do we have a termination request ?
                && !hasToTerminateSupplier.getAsBoolean()) {
            var neighboursIterator = stackFrame.neighbours();

            if (neighboursIterator.hasNext()) {
                var neighbour = neighboursIterator.next();
                var reduced_neighbour = configuration.getModel().canonize(neighbour);
                if (!configuration.knows(neighbour, reduced_neighbour)) {
                    configuration.discover(neighbour, reduced_neighbour);
                    //apply onEntry callback
                    var terminate = configuration.getModel().callbacks().onEntry(stackFrame.vertex(), neighbour, reduced_neighbour);
                    if (terminate) { return configuration; }
                    continue;
                }
                //on known - is called on sharing-links and back-loops
                var terminate = configuration.getModel().callbacks().onKnown(stackFrame.vertex(), neighbour, reduced_neighbour);
                if (terminate) { return configuration; }
                continue;
            }
            configuration.pop();
            if (stackFrame.vertex() == null) continue;
            var terminate = configuration.getModel().callbacks().onExit(stackFrame.vertex(), stackFrame);
            if (terminate) return configuration;
        }
        return configuration;
    }
}
