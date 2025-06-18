package obp3.buchi.ndfs.gs09;

import obp3.things.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.Map;

public class BuchiGS09RedConfiguration<V, A> extends BuchiGS09BlueConfiguration<V,A> {
    public BuchiGS09RedConfiguration(IDepthFirstTraversalParameters<V, A> model, Map<V, VertexColor> known) {
        super(model, known);
    }

    @Override
    public IDepthFirstTraversalConfiguration<V, A> initial() {
        this.stack.clear();
        this.stack.push(new StackFrame<>(null, new PeekableIterator<>(model.getGraph().roots())));
        return this;
    }

    @Override
    public boolean knows(V vertex) {
        if (getVertexColor(vertex).equals(VertexColor.BLUE)) return false;
        return true;
    }
    @Override
    public void add(V vertex) {
        changeVertexColor(vertex, VertexColor.RED);
    }
}
