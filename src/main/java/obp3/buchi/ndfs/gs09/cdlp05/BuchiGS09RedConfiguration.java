package obp3.buchi.ndfs.gs09.cdlp05;

import obp3.buchi.ndfs.gs09.VertexColor;
import obp3.things.PeekableIterator;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.traversal.dfs.model.IDepthFirstTraversalParameters;

import java.util.Map;

public class BuchiGS09RedConfiguration<V, A> extends BuchiGS09BlueConfiguration<V,A> {
    public BuchiGS09RedConfiguration(IDepthFirstTraversalParameters<V, A> model, Map<V, WeightedColor> known) {
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
        return !getVertexColor(vertex).equals(VertexColor.BLUE);
    }
    @Override
    public void add(V vertex) {
        changeVertexColor(vertex, VertexColor.RED);
    }
}
