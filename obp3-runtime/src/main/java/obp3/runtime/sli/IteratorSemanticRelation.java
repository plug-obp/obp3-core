package obp3.runtime.sli;

import java.util.Iterator;

public interface IteratorSemanticRelation<A, C> {
    Iterator<C> initial();
    Iterator<A> actions(C configuration);
    Iterator<C> execute(A action, C configuration);
}
