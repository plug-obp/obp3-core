package fr.ensta.obp3.traversal.bfs;

import fr.ensta.PeekableIterator;
import fr.ensta.obp3.IExecutable;
import fr.ensta.obp3.IRootedGraph;

import java.util.*;
import java.util.function.BooleanSupplier;

public class BreadthFirstTraversalWhile<V> implements IExecutable<Set<V>> {
    IRootedGraph<V> graph;

    public BreadthFirstTraversalWhile(IRootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var c = BreadthFirstTraversalConfiguration.initial(graph.roots());

        while (!c.frontier.isEmpty() || c.neighbours.hasNext()) {
            for (Iterator<V> it = c.neighbours; it.hasNext(); ) {

                //check if we have a termination request
                if (hasToTerminateSupplier.getAsBoolean()) { return c.known; }

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
