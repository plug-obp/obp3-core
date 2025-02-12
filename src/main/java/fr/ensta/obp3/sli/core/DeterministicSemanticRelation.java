package fr.ensta.obp3.sli.core;

import java.util.Optional;

public interface DeterministicSemanticRelation<A, C> {
    Optional<C> initial();

    Optional<A> actions(C configuration);

    Optional<C> execute(A action, C configuration);
}
