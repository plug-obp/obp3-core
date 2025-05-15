package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalWhile<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public DepthFirstTraversalWhile(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var configuration = DepthFirstTraversalConfiguration.initial(graph);
        DepthFirstTraversalConfiguration.StackFrame<V> stackFrame;
        while ((stackFrame = configuration.peek()) != null) {
            var neighboursIterator = stackFrame.neighbours();

            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return configuration.getKnown(); }

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
