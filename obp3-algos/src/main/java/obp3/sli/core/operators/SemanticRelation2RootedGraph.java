package obp3.sli.core.operators;

import obp3.sli.core.IRootedGraph;
import obp3.sli.core.SemanticRelation;

import java.util.Collections;
import java.util.Iterator;

public class SemanticRelation2RootedGraph<A, C> implements IRootedGraph<C> {
    private final SemanticRelation<A, C> operand;

    public SemanticRelation2RootedGraph(SemanticRelation<A, C> operand) {
        this.operand = operand;
    }

    @Override
    public Iterator<C> roots() {
        return operand.initial().iterator();
    }

    @Override
    public Iterator<C> neighbours(C c) {
        var actions = operand.actions(c);
        if (actions.isEmpty())
            return Collections.emptyIterator();

        return actions.stream()
                .flatMap(a -> operand.execute(a, c).stream())
                .iterator();
    }
}
