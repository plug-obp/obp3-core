package obp3.traversal.bfs;

import obp3.things.PeekableIterator;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;

import java.util.*;
import java.util.function.BooleanSupplier;

public class BreadthFirstTraversalDo<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public BreadthFirstTraversalDo(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var c = BreadthFirstTraversalConfiguration.initial(graph.roots());
        do {
            for (Iterator<V> it = c.neighbours; it.hasNext(); ) {
                V v = it.next();
                //check if we have a termination request
                if (hasToTerminateSupplier.getAsBoolean()) { return c.known; }
                if (!c.known.contains(v)) {
                    c.known.add(v);
                    c.frontier.addLast(v);
                }
            }
            if (!c.frontier.isEmpty()) {
                c.neighbours = new PeekableIterator<>(this.graph.neighbours(c.frontier.removeFirst()));
                continue;
            }
            c.neighbours = null;
        } while (c.neighbours != null);
        return c.known;
    }
}
