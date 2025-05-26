package obp3.sli.core;

import java.util.List;

public interface ISemanticRelation<I, A, C> {
    List<C> initial();
    List<A> actions(I input, C configuration);
    List<C> execute(A action, I input, C configuration);
}
