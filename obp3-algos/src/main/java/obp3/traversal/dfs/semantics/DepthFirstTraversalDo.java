package obp3.traversal.dfs.semantics;

import obp3.runtime.IExecutable;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.function.BooleanSupplier;

public class DepthFirstTraversalDo<V, A> implements IExecutable<IDepthFirstTraversalConfiguration<V, A>> {
    IDepthFirstTraversalConfiguration<V, A> configuration;

    public DepthFirstTraversalDo(IDepthFirstTraversalConfiguration<V, A> configuration) {
        this.configuration = configuration;
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> run(BooleanSupplier hasToTerminateSupplier) {
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

            var depthBound = configuration.getModel().getDepthBound();
            if (   (depthBound < 0 || depthBound >= configuration.stackSize())
                    && neighboursIterator.hasNext())
            {
                var neighbour = neighboursIterator.next();
                if (!configuration.knows(neighbour)) {
                    configuration.discover(neighbour);
                    //apply onEntry callback
                    var terminate = configuration.getModel().callbacks().onEntry(stackFrame.vertex(), neighbour, configuration);
                    if (terminate) { return configuration; }
                    continue;
                }
                //on known - is called on sharing-links and back-loops
                var terminate = configuration.getModel().callbacks().onKnown(stackFrame.vertex(), neighbour, configuration);
                if (terminate) { return configuration; }
                continue;
            }
            configuration.pop();
            if (stackFrame.vertex() == null) continue;
            //onExit is called when all children of a vertex are done
            var terminate = configuration.getModel().callbacks().onExit(stackFrame.vertex(), stackFrame, configuration);
            if (terminate) return configuration;
        } while (true);
        return configuration;
    }
}
