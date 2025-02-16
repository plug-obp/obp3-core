package obp3.traversal.dfs;

import obp3.things.PeekableIterator;
import obp3.IExecutable;
import obp3.IRootedGraph;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalDo<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public DepthFirstTraversalDo(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var c = DepthFirstTraversalConfiguration.initial(graph.roots());
        do {
            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return c.known; }

            var stackFrame = c.stack.peek();
            var neighboursIterator = stackFrame.neighbours();

            if (neighboursIterator.hasNext()) {
                var neighbour = neighboursIterator.next();
                if (!c.known.contains(neighbour)) {
                    c.known.add(neighbour);
                    c.stack.push(
                            new DepthFirstTraversalConfiguration.StackFrame<>(
                                    neighbour,
                                    new PeekableIterator<>(graph.neighbours(neighbour))));
                }
                continue;
            }
            c.stack.pop();
        } while (!c.stack.isEmpty());
        return c.known;
    }
}
