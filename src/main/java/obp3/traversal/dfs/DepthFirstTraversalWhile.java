package obp3.traversal.dfs;

import obp3.things.PeekableIterator;
import obp3.IExecutable;
import obp3.IRootedGraph;

import java.util.*;
import java.util.function.BooleanSupplier;

public class DepthFirstTraversalWhile<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public DepthFirstTraversalWhile(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var c = DepthFirstTraversalConfiguration.initial(graph.roots());
        while (!c.stack.isEmpty()) {
            var stackFrame = c.stack.peek();
            var neighboursIterator = stackFrame.neighbours();

            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return c.known; }

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
        }
        return c.known;
    }
}
