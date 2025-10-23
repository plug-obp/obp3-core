package obp3;

import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface IExecutable<R> {
    R run(BooleanSupplier hasToTerminateSupplier);
    default R runAlone() {
        return run(()->false);
    }
}
