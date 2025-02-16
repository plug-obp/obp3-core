package obp3;

import obp3.sli.core.DeterministicSemanticRelation;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class Sequencer<A, C> implements IExecutable<Optional<C>> {
    DeterministicSemanticRelation<A, C> operand;

    public Sequencer(DeterministicSemanticRelation<A, C> operand) {
        this.operand = operand;
    }

    public Optional<C> run(BooleanSupplier hasToTerminateSupplier) {
        C previous = null;
        var current = this.operand.initial();
        while (current.isPresent()) {
            previous = current.get();

            //check if we have a termination request
            if (hasToTerminateSupplier.getAsBoolean()) { return Optional.of(previous); }

            var actionO = this.operand.actions(previous);
            if (actionO.isEmpty()) break;
            current = this.operand.execute(actionO.get(), previous);
        }
        return previous == null ? Optional.empty() : Optional.of(previous);
    }
}
