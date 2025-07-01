package obp3.modelchecking;

import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        return "EmptinessCheckerAnswer{\n\t" +
                "holds=" + holds +
                ",\n\twitness=" + witness +
                ",\n\ttrace=\n\t\t" + trace.stream().map(Object::toString).collect(Collectors.joining(";\n\t\t")) +
                "\n}";
    }
}
