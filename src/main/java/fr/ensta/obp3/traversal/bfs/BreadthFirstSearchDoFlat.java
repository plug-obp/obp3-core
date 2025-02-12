package fr.ensta.obp3.traversal.bfs;

import fr.ensta.obp3.IExecutable;
import fr.ensta.obp3.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class BreadthFirstSearchDoFlat<V> implements IExecutable<Set<V>> {
    RootedGraph<V> graph;

    public BreadthFirstSearchDoFlat(RootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var known = new HashSet<V>();
        var frontier = new ArrayDeque<V>();
        Iterator<V> neighbours = this.graph.roots();
        do {
            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return known; }
            if (neighbours.hasNext()) {
                V v = neighbours.next();
                if (!known.contains(v)) {
                    known.add(v);
                    frontier.addLast(v);
                }
                continue;
            } else if (!frontier.isEmpty()) {
                neighbours = this.graph.neighbours(frontier.removeFirst());
                continue;
            }
            return known;
        } while (true);
    }
}
