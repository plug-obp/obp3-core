package obp3.runtime.sli;

import java.util.List;
import java.util.Optional;

public interface IDeterministicSematicRelation<I, A, C> {
    Optional<C> initial();
    Optional<A> actions(I input, C configuration);
    Optional<C> execute(A action, I input, C configuration);

    default DependentSemanticRelation<I, A, C> toISemanticRelation() {
        return new DependentSemanticRelation<>() {

            @Override
            public List<C> initial() {
                return IDeterministicSematicRelation.this.initial().stream().toList();
            }

            @Override
            public List<A> actions(I input, C configuration) {
                return IDeterministicSematicRelation.this.actions(input, configuration).stream().toList();
            }

            @Override
            public List<C> execute(A action, I input, C configuration) {
                return IDeterministicSematicRelation.this.execute(action, input, configuration).stream().toList();
            }
        };
    }
}
