package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalDo<V, A> implements IExecutable<Set<V>> {
    IDepthFirstTraversalParameters<V, A> model;

    public DepthFirstTraversalDo(IDepthFirstTraversalParameters<V, A> model) {
        this.model = model;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var configuration = DepthFirstTraversalConfiguration.initial(model);
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
                if (!configuration.knows(neighbour)) {
                    configuration.discover(neighbour);
                }
                continue;
            }
            configuration.pop();
        } while (true);
        return configuration.getKnown();
    }
}
