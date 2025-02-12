package fr.ensta.traversal.bfs;

import fr.ensta.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BreadthFirstSearch<V> {
    RootedGraph<V> graph;

    public BreadthFirstSearch(RootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run() {
        var known = new HashSet<V>();
        var frontier = new ArrayDeque<V>();
        Iterator<V> neighbours = null;
        while (!frontier.isEmpty() || neighbours == null) {
            if (neighbours == null) {
                neighbours = this.graph.roots();
            } else {
                neighbours = this.graph.neighbours(frontier.removeFirst());
            }
            for (Iterator<V> it = neighbours; it.hasNext(); ) {
                V v = it.next();
                if (!known.contains(v)) {
                    known.add(v);
                    frontier.addLast(v);
                }
            }
        }
        return known;
    }
}
