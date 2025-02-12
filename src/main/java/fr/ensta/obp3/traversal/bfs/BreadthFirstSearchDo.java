package fr.ensta.obp3.traversal.bfs;

import fr.ensta.obp3.IExecutionController;
import fr.ensta.obp3.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class BreadthFirstSearchDo<V> implements IExecutionController<Set<V>> {
    RootedGraph<V> graph;

    public BreadthFirstSearchDo(RootedGraph<V> graph) {
        this.graph = graph;
    }

    @Override
    public Set<V> run(BooleanSupplier hasToTerminateSupplier) {
        var known = new HashSet<V>();
        var frontier = new ArrayDeque<V>();
        Iterator<V> neighbours = this.graph.roots();
        do {
            for (Iterator<V> it = neighbours; it.hasNext(); ) {
                V v = it.next();
                //check if we have a termination request
                if (hasToTerminateSupplier.getAsBoolean()) { return known; }
                if (!known.contains(v)) {
                    known.add(v);
                    frontier.addLast(v);
                }
            }
            if (!frontier.isEmpty()) {
                neighbours = this.graph.neighbours(frontier.removeFirst());
                continue;
            }
            neighbours = null;
        } while (neighbours != null);
        return known;
    }
}
