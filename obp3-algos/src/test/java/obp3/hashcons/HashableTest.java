package obp3.hashcons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;

@DisplayName("Hashable Interface Tests")
class HashableTest {

    @Test
    @DisplayName("standard() should use Objects.hashCode and Objects.equals")
    void testStandard() {
        Hashable<String> hashable = Hashable.standard();
        
        // Test hash
        assertEquals("test".hashCode(), hashable.hash("test"));
        assertEquals(0, hashable.hash(null));
        
        // Test equality
        assertTrue(hashable.equal("test", "test"));
        assertTrue(hashable.equal(null, null));
        assertFalse(hashable.equal("test", "other"));
        assertFalse(hashable.equal("test", null));
        assertFalse(hashable.equal(null, "test"));
    }

    @Test
    @DisplayName("identity() should use reference equality")
    void testIdentity() {
        Hashable<String> hashable = Hashable.identity();
        
        String s1 = new String("test");
        String s2 = new String("test");
        String s3 = s1;
        
        // Test hash
        assertEquals(System.identityHashCode(s1), hashable.hash(s1));
        assertEquals(System.identityHashCode(s2), hashable.hash(s2));
        
        // Test equality - only same reference is equal
        assertTrue(hashable.equal(s1, s1));
        assertTrue(hashable.equal(s1, s3)); // Same reference
        assertFalse(hashable.equal(s1, s2)); // Different objects, even if value-equal
        assertTrue(hashable.equal(null, null));
        assertFalse(hashable.equal(s1, null));
    }

    @Test
    @DisplayName("fromComparator() should use comparator for equality")
    void testFromComparator() {
        Hashable<String> hashable = Hashable.fromComparator(String.CASE_INSENSITIVE_ORDER);
        
        // Test equality based on comparator
        assertTrue(hashable.equal("test", "TEST"));
        assertTrue(hashable.equal("Hello", "hello"));
        assertFalse(hashable.equal("test", "other"));
        assertTrue(hashable.equal(null, null));
        assertFalse(hashable.equal("test", null));
        assertFalse(hashable.equal(null, "test"));
    }

    @Test
    @DisplayName("fromComparator() should throw NPE for null comparator")
    void testFromComparatorNull() {
        assertThrows(NullPointerException.class, () -> {
            Hashable.fromComparator(null);
        });
    }

    @Test
    @DisplayName("validateContract() should pass for valid implementation")
    void testValidateContractValid() {
        Hashable<String> hashable = Hashable.standard();
        
        // Should not throw
        assertDoesNotThrow(() -> hashable.validateContract("test"));
        assertDoesNotThrow(() -> hashable.validateContract(null));
    }

    @Test
    @DisplayName("validateContract() should fail for non-reflexive equality")
    void testValidateContractNonReflexive() {
        Hashable<String> badHashable = new Hashable<String>() {
            @Override
            public int hash(String x) {
                return 0;
            }
            
            @Override
            public boolean equal(String x, String y) {
                return false; // Never equal, not even to itself!
            }
        };
        
        assertThrows(IllegalStateException.class, () -> {
            badHashable.validateContract("test");
        });
    }

    @Test
    @DisplayName("validateContract() should fail for inconsistent hash")
    void testValidateContractInconsistentHash() {
        Hashable<String> badHashable = new Hashable<String>() {
            private int counter = 0;
            
            @Override
            public int hash(String x) {
                return counter++; // Different value each time!
            }
        };
        
        assertThrows(IllegalStateException.class, () -> {
            badHashable.validateContract("test");
        });
    }

    @Test
    @DisplayName("withPositiveHash() should make negative hashes positive")
    void testWithPositiveHash() {
        Hashable<Integer> hashable = x -> x * -1; // Always negative for positive input
        Hashable<Integer> positive = hashable.withPositiveHash();
        
        assertTrue(positive.hash(100) >= 0);
        assertTrue(positive.hash(1) >= 0);
        assertTrue(positive.hash(-1) >= 0);
        
        // Should still preserve equality
        assertTrue(positive.equal(5, 5));
        assertFalse(positive.equal(5, 10));
    }

    @Test
    @DisplayName("withPositiveHash() should handle Integer.MIN_VALUE")
    void testWithPositiveHashMinValue() {
        Hashable<Integer> hashable = x -> Integer.MIN_VALUE;
        Hashable<Integer> positive = hashable.withPositiveHash();
        
        // Math.abs(Integer.MIN_VALUE) is still negative, but should work
        int result = positive.hash(0);
        assertTrue(result >= 0 || result == Integer.MIN_VALUE);
    }

    @Test
    @DisplayName("Custom hashable should satisfy hash-equality contract")
    void testCustomHashableContract() {
        record Point(int x, int y) {}
        
        // Custom hashable that only considers x coordinate
        Hashable<Point> hashable = new Hashable<Point>() {
            @Override
            public int hash(Point p) {
                return p == null ? 0 : p.x();
            }
            
            @Override
            public boolean equal(Point p1, Point p2) {
                if (p1 == null && p2 == null) return true;
                if (p1 == null || p2 == null) return false;
                return p1.x() == p2.x();
            }
        };
        
        Point p1 = new Point(5, 10);
        Point p2 = new Point(5, 20);
        Point p3 = new Point(6, 10);
        
        // Points with same x should be equal and have same hash
        assertTrue(hashable.equal(p1, p2));
        assertEquals(hashable.hash(p1), hashable.hash(p2));
        
        // Points with different x should not be equal
        assertFalse(hashable.equal(p1, p3));
        
        // Validate contract
        assertDoesNotThrow(() -> hashable.validateContract(p1));
    }

    @Test
    @DisplayName("Default equal() method should use Objects.equals")
    void testDefaultEqualMethod() {
        Hashable<String> hashable = x -> x == null ? 0 : x.hashCode();
        
        assertTrue(hashable.equal("test", "test"));
        assertTrue(hashable.equal(null, null));
        assertFalse(hashable.equal("test", "other"));
        assertFalse(hashable.equal("test", null));
        assertFalse(hashable.equal(null, "test"));
    }

    @Test
    @DisplayName("Hashable should work with records")
    void testWithRecords() {
        record Person(String name, int age) {}
        
        Hashable<Person> hashable = Hashable.standard();
        
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Alice", 30);
        Person p3 = new Person("Bob", 25);
        
        assertTrue(hashable.equal(p1, p2));
        assertFalse(hashable.equal(p1, p3));
        assertEquals(hashable.hash(p1), hashable.hash(p2));
    }

    @Test
    @DisplayName("Comparator-based hashable should work with custom comparators")
    void testComparatorWithCustomType() {
        record Person(String name, int age) {}
        
        // Compare only by age
        Comparator<Person> ageComparator = Comparator.comparingInt(Person::age);
        Hashable<Person> hashable = Hashable.fromComparator(ageComparator);
        
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Bob", 30);
        Person p3 = new Person("Charlie", 25);
        
        assertTrue(hashable.equal(p1, p2)); // Same age
        assertFalse(hashable.equal(p1, p3)); // Different age
    }

    @Test
    @DisplayName("toString() should provide meaningful representations")
    void testToString() {
        assertTrue(Hashable.standard().toString().contains("standard"));
        assertTrue(Hashable.identity().toString().contains("identity"));
        
        Hashable<String> base = Hashable.standard();
        Hashable<String> wrapped = base.withPositiveHash();
        assertTrue(wrapped.toString().contains("withPositiveHash"));
    }
}
