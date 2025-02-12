package fr.ensta;

import java.util.Optional;

public class Sequencer<A, C> {
    DeterministicSemanticRelation<A, C> operand;

    public Sequencer(DeterministicSemanticRelation<A, C> operand) {
        this.operand = operand;
    }

    public Optional<C> run() {
        C previous = null;
        var current = this.operand.initial();
        while (current.isPresent()) {
            previous = current.get();
            var actionO = this.operand.actions(previous);
            if (actionO.isEmpty()) break;
            current = this.operand.execute(actionO.get(), previous);
        }
        return previous == null ? Optional.empty() : Optional.of(previous);
    }
}
