package obp3.traversal.dfs.model;

import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

public interface IDepthFirstTraversalParameters<V, A> {
    IRootedGraph<V> getGraph();

    A canonize(V v);

    boolean onEntry(V source, V vertex, A canonical);
    boolean onKnown(V source, V vertex, A canonical);

    boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame);
}
