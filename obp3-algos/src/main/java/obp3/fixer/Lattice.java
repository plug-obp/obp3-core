package obp3.fixer;

import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Minimal property-domain operations for {@link Fixer}. Bottom must be least;
 * optional top must be greatest. Equality must be a stable equivalence matching
 * the domain's order and equations. All properties are non-null and immutable
 * (or treated as immutable). A null top means no top optimization is supplied.
 * This descriptor supplies neither an order test, a join nor widening.
 */
public record Lattice<T>(T bottom, T top, BiPredicate<T, T> equality) {
    public Lattice {
        Objects.requireNonNull(bottom, "bottom");
        Objects.requireNonNull(equality, "equality");
    }

    public Lattice(T bottom, BiPredicate<T, T> equality) {
        this(bottom, null, equality);
    }

    public static final Lattice<Boolean> BooleanLattice =
            new Lattice<>(false, true, Objects::equals);

    public boolean isMaximal(T value) {
        Objects.requireNonNull(value, "value");
        return top != null && equality.test(value, top);
    }
}
