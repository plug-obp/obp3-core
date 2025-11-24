package obp3.modelchecking;

import obp3.sli.core.operators.product.Product;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;
import obp3.utils.Either;

import java.util.Objects;
import java.util.function.Predicate;

public class EmptinessCheckerStatus {
    public long knownSize = 0;
    public long worklistSize = 0;

    public static EmptinessCheckerStatus ZERO = new EmptinessCheckerStatus(0, 0);

    public EmptinessCheckerStatus() {}

    public EmptinessCheckerStatus(long knownSize, long worklistSize) {
        this.knownSize = knownSize;
        this.worklistSize = worklistSize;
    }

    public EmptinessCheckerStatus(EmptinessCheckerStatus status) {
        this.knownSize = status.knownSize;
        this.worklistSize = status.worklistSize;
    }

    public void reset(EmptinessCheckerStatus status) {
        this.knownSize = status.knownSize;
        this.worklistSize = status.worklistSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmptinessCheckerStatus that)) return false;
        return knownSize == that.knownSize && worklistSize == that.worklistSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(knownSize, worklistSize);
    }

    @Override
    public String toString() {
        return "EmptinessCheckerStatus{" +
                "knownSize=" + knownSize +
                ", worklistSize=" + worklistSize +
                '}';
    }

    public static<V, A> boolean statusCallback(EmptinessCheckerStatus status, Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>> c, Predicate<EmptinessCheckerStatus> hasToTerminatePredicate) {
        return statusCallback(ZERO, status, c, hasToTerminatePredicate);
    }

    public static<V, A> boolean statusCallback(EmptinessCheckerStatus base, EmptinessCheckerStatus status, Either<IDepthFirstTraversalConfiguration<V, A>, Product<IDepthFirstTraversalConfiguration<V, A>, Boolean>> c, Predicate<EmptinessCheckerStatus> hasToTerminatePredicate) {
        switch (c) {
            case Either.Left(IDepthFirstTraversalConfiguration<V, A> s) -> {
                status.knownSize = base.knownSize + s.getKnown().size();
                status.worklistSize = base.worklistSize + s.stackSize();
            }
            case Either.Right(Product<IDepthFirstTraversalConfiguration<V, A>, Boolean> p) -> {
                status.knownSize = base.knownSize + p.l().getKnown().size();
                status.worklistSize = base.worklistSize + p.l().stackSize();
            }
        }
        return hasToTerminatePredicate.test(status);
    }
}
