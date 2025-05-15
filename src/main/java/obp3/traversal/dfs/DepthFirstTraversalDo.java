package obp3.traversal.dfs;

import obp3.IExecutable;
import obp3.sli.core.IRootedGraph;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalDo<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public DepthFirstTraversalDo(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var configuration = DepthFirstTraversalConfiguration.initial(graph);
        do {
            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return configuration.getKnown(); }

            var stackFrame = configuration.peek();
            if (stackFrame == null) {
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
