package obp3.fixer;

import java.util.function.BiPredicate;

public record Lattice<T>(T bottom, T top, BiPredicate<T, T> equality) {
    public Lattice(T bottom, BiPredicate<T, T> equality) {
        this(bottom, null, equality);
    }

    public static final Lattice<Boolean> BooleanLattice =
            new Lattice<>(false, true, (l, r) -> l == r);

    public boolean isMaximal(T value) {
        return top != null && value == top;
    }
}
