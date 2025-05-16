package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalWhile<V, A> implements IExecutable<Set<V>> {
    IDepthFirstTraversalParameters<V, A> model;

    public DepthFirstTraversalWhile(IDepthFirstTraversalParameters<V, A> model) {
        this.model = model;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var configuration = DepthFirstTraversalConfiguration.initial(model);
        DepthFirstTraversalConfiguration.StackFrame<V> stackFrame;
        while (     //did we finish ?
                    (stackFrame = configuration.peek()) != null
                    //do we have a termination request ?
                && !hasToTerminateSupplier.getAsBoolean()) {
            var neighboursIterator = stackFrame.neighbours();

            if (neighboursIterator.hasNext()) {
                var neighbour = neighboursIterator.next();
                if (!configuration.knows(neighbour)) {
                    configuration.discover(neighbour);
                }
                continue;
            }
            configuration.pop();
        }
        return configuration.getKnown();
    }
}
