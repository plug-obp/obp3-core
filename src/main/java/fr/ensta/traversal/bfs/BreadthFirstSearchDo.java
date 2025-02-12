package fr.ensta.traversal.bfs;

import fr.ensta.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BreadthFirstSearchDo<V> {
    RootedGraph<V> graph;

    public BreadthFirstSearchDo(RootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run() {
        var known = new HashSet<V>();
        var frontier = new ArrayDeque<V>();
        Iterator<V> neighbours = this.graph.roots();
        do {
            for (Iterator<V> it = neighbours; it.hasNext(); ) {
                V v = it.next();
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
