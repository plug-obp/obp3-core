package obp3.sli.core.operators;

import obp3.sli.core.IRootedGraph;

import java.util.Iterator;

public class ReRootedGraph<V> implements IRootedGraph<V> {
    IRootedGraph<V> operand;
    Iterator<V> newRoots;
    public ReRootedGraph(IRootedGraph<V> operand, Iterator<V> newRoots) {
        this.operand = operand;
        this.newRoots = newRoots;
    }
    @Override
    public Iterator<V> roots() {
        return newRoots;
    }

    @Override
    public Iterator<V> neighbours(V v) {
        return operand.neighbours(v);
    }
}
