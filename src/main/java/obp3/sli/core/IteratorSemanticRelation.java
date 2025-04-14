package obp3.sli.core;

import java.util.Iterator;

public interface IteratorSemanticRelation<A, C> {
    Iterator<C> initial();
    Iterator<A> actions(C configuration);
    Iterator<C> execute(A action, C configuration);
}
