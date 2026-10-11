package obp3.fixer;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LatticeTest {
    private record Property(int value, String description) {}

    @Test void maximalityUsesTheDeclaredEqualityRatherThanIdentityOrObjectEquals() {
        var top = new Property(3, "canonical top");
        var lattice = new Lattice<>(new Property(0, "bottom"), top,
                (left, right) -> left.value() == right.value());
        var equivalentTop = new Property(3, "another representation");

        assertNotSame(top, equivalentTop);
        assertNotEquals(top, equivalentTop);
        assertTrue(lattice.isMaximal(equivalentTop));
        assertFalse(lattice.isMaximal(new Property(2, "below top")));
    }

    @Test void absentTopNeverCallsEqualityToRecognizeMaximality() {
        var comparisons = new AtomicInteger();
        var lattice = new Lattice<>(0, (left, right) -> {
            comparisons.incrementAndGet();
            return left.equals(right);
        });

        assertNull(lattice.top());
        assertFalse(lattice.isMaximal(0));
        assertFalse(lattice.isMaximal(Integer.MAX_VALUE));
        assertEquals(0, comparisons.get());
    }

    @Test void bottomAndEqualityAreRequiredButTopIsOptional() {
        assertThrows(NullPointerException.class, () -> new Lattice<Integer>(null, Integer::equals));
        assertThrows(NullPointerException.class, () -> new Lattice<Integer>(0, null));
        assertThrows(NullPointerException.class, () -> new Lattice<Integer>(0, 1, null));
        assertDoesNotThrow(() -> new Lattice<>(0, null, Integer::equals));
    }

    @Test void booleanLatticeRetainsItsBottomAndTopContract() {
        assertEquals(false, Lattice.BooleanLattice.bottom());
        assertEquals(true, Lattice.BooleanLattice.top());
        assertFalse(Lattice.BooleanLattice.isMaximal(false));
        assertTrue(Lattice.BooleanLattice.isMaximal(true));
    }
}
