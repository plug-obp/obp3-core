package obp3.traversal.bfs;

import obp3.things.PeekableIterator;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;

import java.util.Set;
import java.util.function.BooleanSupplier;

public class BreadthFirstTraversalDoFlat<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public BreadthFirstTraversalDoFlat(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var c = BreadthFirstTraversalConfiguration.initial(graph.roots());
        do {
            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return c.known; }

            if (c.neighbours.hasNext()) {
                V v = c.neighbours.next();
                if (!c.known.contains(v)) {
                    c.known.add(v);
                    c.frontier.addLast(v);
                }
                continue;
            } else if (!c.frontier.isEmpty()) {
                c.neighbours = new PeekableIterator<>(this.graph.neighbours(c.frontier.removeFirst()));
                continue;
            }
            return c.known;
        } while (true);
    }
}
