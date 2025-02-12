package fr.ensta.obp3.traversal.bfs;

import fr.ensta.obp3.IExecutable;
import fr.ensta.obp3.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class BreadthFirstSearchWhile<V> implements IExecutable<Set<V>> {
    RootedGraph<V> graph;

    public BreadthFirstSearchWhile(RootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var known = new HashSet<V>();
        var frontier = new ArrayDeque<V>();
        Iterator<V> neighbours = this.graph.roots();
        while (!frontier.isEmpty() || neighbours.hasNext()) {
            for (Iterator<V> it = neighbours; it.hasNext(); ) {

                //check if we have a termination request
                if (hasToTerminateSupplier.getAsBoolean()) { return known; }

                V v = it.next();
                if (!known.contains(v)) {
                    known.add(v);
                    frontier.addLast(v);
                }
            }
            if (!frontier.isEmpty()) {
                neighbours = this.graph.neighbours(frontier.removeFirst());
            }
        }
        return known;
    }
}
