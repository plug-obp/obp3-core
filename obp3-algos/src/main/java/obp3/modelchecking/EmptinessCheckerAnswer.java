package obp3.modelchecking;

import obp3.runtime.sli.IOSematicRelation;
import obp3.runtime.sli.Step;
import obp3.sli.core.operators.product.Product;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EmptinessCheckerAnswer<V> {
    public boolean holds = true;
    public Step<?, V> witness;
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

    public <U> EmptinessCheckerAnswer<U> map(Function<V, U> mapper) {
        EmptinessCheckerAnswer<U> result = new EmptinessCheckerAnswer<>();
        result.holds = this.holds;
        result.witness = this.witness != null ? new Step<>(mapper.apply(witness.start()), Optional.empty(), mapper.apply(witness.end())) : null;
        result.trace = this.trace.stream().map(mapper).collect(Collectors.toList());
        return result;
    }
}
