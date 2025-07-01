package obp3.sli.core;

import java.util.List;
import java.util.Optional;

public interface DeterministicSemanticRelation<A, C> {
    Optional<C> initial();

    Optional<A> actions(C configuration);


    Optional<C> execute(A action, C configuration);

    /**
     * By default, we suppose that a language runtime has side effects.
     * That is the action function modifies the input configuration.
     *
     * If a language semantics does not have 'action' side effects it should override this function for less overhead.
     * @return true, if the actions function is pure
     * */
    default boolean actionsIsPure() { return false; }

    /**
     * By default, we suppose that a language runtime has side effects.
     * That is the execute function modifies the input configuration.
     *
     * If a language semantics does not have 'execute' side effects it should override this function for less overhead.
     * @return true, if the execute function is pure
     * */
    default boolean executeIsPure() { return false; }

    default SemanticRelation<A, C> toSemanticRelation() {
        return new SemanticRelation<A, C>() {
            @Override
            public List<C> initial() {
                return DeterministicSemanticRelation.this.initial().stream().toList();
            }

            @Override
            public List<A> actions(C configuration) {
                return DeterministicSemanticRelation.this.actions(configuration).stream().toList();
            }

            @Override
            public List<C> execute(A action, C configuration) {
                return DeterministicSemanticRelation.this.execute(action, configuration).stream().toList();
            }
        };
    }
}
