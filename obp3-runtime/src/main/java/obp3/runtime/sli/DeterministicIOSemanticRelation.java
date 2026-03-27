package obp3.runtime.sli;

import java.util.List;
import java.util.Optional;

public interface DeterministicIOSemanticRelation<I, O, A, C> {
    Optional<C> initial();

    Optional<A> actions(I input, C configuration);


    Optional<IOSematicRelation.Pair<O, C>> execute(A action, I input, C configuration);

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

    default IOSematicRelation<I, O, A, C> toIOSemanticRelation() {
        return new IOSematicRelation<I, O, A, C>() {
            @Override
            public List<C> initial() {
                return DeterministicIOSemanticRelation.this.initial().stream().toList();
            }

            @Override
            public List<A> actions(I input, C configuration) {
                return DeterministicIOSemanticRelation.this.actions(input, configuration).stream().toList();
            }

            @Override
            public Fanout<O, C> execute(A action, I input, C configuration) {
                var pairO = DeterministicIOSemanticRelation.this.execute(action, input, configuration);
                return pairO.map(ocPair -> new Fanout<>(null, List.of(ocPair))).orElseGet(() -> new Fanout<>(null, List.of()));
            }
        };
    }
}
