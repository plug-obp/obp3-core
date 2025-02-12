package fr.ensta.obp3.traversal.bfs;

import fr.ensta.obp3.RootedGraph;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BreadthFirstSearchDoFlat<V> {
    RootedGraph<V> graph;

    public BreadthFirstSearchDoFlat(RootedGraph<V> graph) {
        this.graph = graph;
    }

    public Set<V> run() {
        var known = new HashSet<V>();
        var frontier = new ArrayDeque<V>();
        Iterator<V> neighbours = this.graph.roots();
        do {
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
