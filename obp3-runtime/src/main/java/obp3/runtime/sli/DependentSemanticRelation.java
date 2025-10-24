package obp3.runtime.sli;

import java.util.List;

public interface DependentSemanticRelation<I, A, C> {
    List<C> initial();
    List<A> actions(I input, C configuration);
    List<C> execute(A action, I input, C configuration);
}
