package fr.ensta.obp3;

import java.util.function.BooleanSupplier;

@FunctionalInterface
public interface IExecutionController<R> {
    R run(BooleanSupplier hasToTerminateSupplier);
    default R runAlone() {
        return run(()->false);
    }
}
