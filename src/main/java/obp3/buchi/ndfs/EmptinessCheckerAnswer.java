package obp3.buchi.ndfs;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EmptinessCheckerAnswer<V> {
    public boolean holds = true;
    public V witness;
    public List<V> trace = new ArrayList<>();

    public EmptinessCheckerAnswer() {}

    public void addToTrace(Iterator<IDepthFirstTraversalConfiguration.StackFrame<V>> stackI) {
        while (stackI.hasNext()) {
            var x = stackI.next().vertex();
            if (x == null) break;
            trace.add(x);
        }
    }

    public void addToTrace(V vertex, Iterator<IDepthFirstTraversalConfiguration.StackFrame<V>> stackI) {
        trace.add(vertex);
        addToTrace(stackI);
    }
}
