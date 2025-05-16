package obp3.traversal.dfs.semantics;

import obp3.IExecutable;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalDo<V, A> implements IExecutable<Set<V>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalDo(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    @Override
    public Set run(BooleanSupplier hasToTerminateSupplier) {
        configuration.initial();
        do {
            var stackFrame = configuration.peek();
            if (   //did we finish ?
                   stackFrame == null
                   //do we have a termination request ?
                || hasToTerminateSupplier.getAsBoolean()) {
                //at the end no stack frame is left
                break;
            }
            var neighboursIterator = stackFrame.neighbours();

            if (neighboursIterator.hasNext()) {
                var neighbour = neighboursIterator.next();
                var reduced_neighbour = configuration.getModel().canonize(neighbour);
                if (!configuration.knows(neighbour, reduced_neighbour)) {
                    configuration.discover(neighbour, reduced_neighbour);
                    //apply onEntry callback
                    var terminate = configuration.getModel().onEntry(stackFrame.vertex(), neighbour, reduced_neighbour);
                    if (terminate) { return configuration.getKnown(); }
                    continue;
                }
                //on known - is called on sharing-links and back-loops
                var terminate = configuration.getModel().onKnown(stackFrame.vertex(), neighbour, reduced_neighbour);
                if (terminate) { return configuration.getKnown(); }
                continue;
            }
            configuration.pop();
            if (stackFrame.vertex() == null) continue;
            //onExit is called when all children of a vertex are done
            var terminate = configuration.getModel().onExit(stackFrame.vertex(), stackFrame);
            if (terminate) return configuration.getKnown();
        } while (true);
        return configuration.getKnown();
    }
}
