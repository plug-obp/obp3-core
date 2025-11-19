package obp3.runtime;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@FunctionalInterface
public interface IExecutable<ExeState, R> {
    default R run(BooleanSupplier hasToTerminateSupplier) {
        return run((e) -> hasToTerminateSupplier.getAsBoolean());
    }
    default R runAlone() {
        return run(()->false);
    }
    R run(Predicate<ExeState> hasToTerminatePredicate);
}
