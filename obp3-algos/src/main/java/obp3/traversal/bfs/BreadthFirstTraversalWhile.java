package obp3.traversal.bfs;

import obp3.utils.PeekableIterator;
import obp3.runtime.IExecutable;
import obp3.runtime.sli.IRootedGraph;

import java.util.*;
import java.util.function.Predicate;

public class BreadthFirstTraversalWhile<V> implements IExecutable<BreadthFirstTraversalConfiguration<V>, Set<V>> {
    IRootedGraph<V> graph;

    public BreadthFirstTraversalWhile(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(Predicate<BreadthFirstTraversalConfiguration<V>> hasToTerminatePredicate) {
        var c = BreadthFirstTraversalConfiguration.initial(graph.roots());

        while (!c.frontier.isEmpty() || c.neighbours.hasNext()) {
            for (Iterator<V> it = c.neighbours; it.hasNext(); ) {

                //check if we have a termination request
                if (hasToTerminatePredicate.test(c)) { return c.known; }

                V v = it.next();
                if (!c.known.contains(v)) {
                    c.known.add(v);
                    c.frontier.addLast(v);
                }
            }
            if (!c.frontier.isEmpty()) {
                c.neighbours = new PeekableIterator<>(this.graph.neighbours(c.frontier.removeFirst()));
            }
        }
        return c.known;
    }
}
