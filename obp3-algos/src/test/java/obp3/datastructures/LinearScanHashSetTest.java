package obp3.datastructures;

import obp3.hashcons.Hashable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LinearScanHashSet Tests")
class LinearScanHashSetTest {

    private LinearScanHashSet<String> set;

    @BeforeEach
    void setUp() {
        set = new LinearScanHashSet<>(16);
    }

    @Test
    @DisplayName("New set should be empty")
    void testNewSetIsEmpty() {
        assertEquals(0, set.size());
        assertEquals(16, set.capacity());
    }

    @Test
    @DisplayName("add() should add new elements and return true")
    void testAddNewElement() {
        assertTrue(set.add("element1"));
        assertEquals(1, set.size());
        assertTrue(set.contains("element1"));
    }

    @Test
    @DisplayName("add() should not add duplicates and return false")
    void testAddDuplicate() {
        assertTrue(set.add("element1"));
        assertFalse(set.add("element1")); // Duplicate
        assertEquals(1, set.size());
    }

    @Test
    @DisplayName("add() should handle null values")
    void testAddNull() {
        assertTrue(set.add(null));
        assertEquals(1, set.size());
        assertTrue(set.contains(null));
        assertFalse(set.add(null)); // Duplicate null
        assertEquals(1, set.size());
    }

    @Test
    @DisplayName("addIfAbsent() should behave like add()")
    void testAddIfAbsent() {
        assertTrue(set.addIfAbsent("element1"));
        assertFalse(set.addIfAbsent("element1"));
        assertEquals(1, set.size());
    }

    @Test
    @DisplayName("contains() should return true for existing elements")
    void testContainsExisting() {
        set.add("element1");
        set.add("element2");
        
        assertTrue(set.contains("element1"));
        assertTrue(set.contains("element2"));
        assertFalse(set.contains("element3"));
    }

    @Test
    @DisplayName("contains() should return false for non-existing elements")
    void testContainsNonExisting() {
        assertFalse(set.contains("nonexistent"));
    }

    @Test
    @DisplayName("contains() should handle null")
    void testContainsNull() {
        assertFalse(set.contains(null));
        set.add(null);
        assertTrue(set.contains(null));
    }

    @Test
    @DisplayName("get() should return existing element")
    void testGetExisting() {
        String element = "test";
        set.add(element);
        
        String retrieved = set.get("test");
        assertNotNull(retrieved);
        assertEquals("test", retrieved);
    }

    @Test
    @DisplayName("get() should return null for non-existing element")
    void testGetNonExisting() {
        assertNull(set.get("nonexistent"));
    }

    @Test
    @DisplayName("get() should return the canonical instance")
    void testGetCanonicalInstance() {
        String s1 = new String("test");
        String s2 = new String("test");
        
        set.add(s1);
        String retrieved = set.get(s2);
        
        assertSame(s1, retrieved); // Should return the first instance
        assertNotSame(s2, retrieved);
    }

    @Test
    @DisplayName("Set should grow when load factor is reached")
    void testGrowth() {
        LinearScanHashSet<Integer> smallSet = new LinearScanHashSet<>(4);
        
        assertEquals(4, smallSet.capacity());
        
        // Add elements: load factor is 0.667, so 0.667 * 4 = 2.668
        // Growth happens when size >= 2.668, so when we try to add the 3rd element
        smallSet.add(1);
        smallSet.add(2);
        assertEquals(4, smallSet.capacity()); // No growth yet
        
        smallSet.add(3); // Size becomes 3, which is >= 2.668, so next add will trigger growth
        
        // Check before growth
        int capacityBeforeGrowth = smallSet.capacity();
        smallSet.add(4); // This should trigger growth
        
        assertTrue(smallSet.capacity() > capacityBeforeGrowth);
        assertEquals(4, smallSet.size());
        assertTrue(smallSet.contains(1));
        assertTrue(smallSet.contains(2));
        assertTrue(smallSet.contains(3));
        assertTrue(smallSet.contains(4));
    }

    @Test
    @DisplayName("Set should handle collisions correctly")
    void testCollisions() {
        // Create hashable with many collisions
        Hashable<Integer> collidingHashable = x -> 1; // All hash to same value!
        LinearScanHashSet<Integer> collidingSet = new LinearScanHashSet<>(16, collidingHashable);
        
        collidingSet.add(1);
        collidingSet.add(2);
        collidingSet.add(3);
        
        assertEquals(3, collidingSet.size());
        assertTrue(collidingSet.contains(1));
        assertTrue(collidingSet.contains(2));
        assertTrue(collidingSet.contains(3));
    }

    @Test
    @DisplayName("Set should work with custom hashable")
    void testCustomHashable() {
        record Point(int x, int y) {}
        
        // Only compare x coordinates
        Hashable<Point> xOnlyHashable = new Hashable<Point>() {
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
        
        LinearScanHashSet<Point> pointSet = new LinearScanHashSet<>(16, xOnlyHashable);
        
        assertTrue(pointSet.add(new Point(1, 1)));
        assertFalse(pointSet.add(new Point(1, 2))); // Same x, considered duplicate
        assertTrue(pointSet.add(new Point(2, 1))); // Different x
        
        assertEquals(2, pointSet.size());
        assertTrue(pointSet.contains(new Point(1, 999))); // Any y with x=1
        assertTrue(pointSet.contains(new Point(2, 999))); // Any y with x=2
        assertFalse(pointSet.contains(new Point(3, 1)));
    }

    @Test
    @DisplayName("Set should work with identity hashable")
    void testIdentityHashable() {
        LinearScanHashSet<String> identitySet = new LinearScanHashSet<>(16, Hashable.identity());
        
        String s1 = new String("test");
        String s2 = new String("test");
        
        identitySet.add(s1);
        identitySet.add(s2); // Different object, should be added
        
        assertEquals(2, identitySet.size());
        assertTrue(identitySet.contains(s1));
        assertTrue(identitySet.contains(s2));
    }

    @Test
    @DisplayName("Set should handle many elements")
    void testManyElements() {
        LinearScanHashSet<Integer> largeSet = new LinearScanHashSet<>(16);
        
        int count = 1000;
        for (int i = 0; i < count; i++) {
            assertTrue(largeSet.add(i));
        }
        
        assertEquals(count, largeSet.size());
        
        for (int i = 0; i < count; i++) {
            assertTrue(largeSet.contains(i));
        }
        
        assertFalse(largeSet.contains(count));
    }

    @Test
    @DisplayName("toString() should include capacity and size")
    void testToString() {
        set.add("element1");
        String str = set.toString();
        
        assertTrue(str.contains("capacity="));
        assertTrue(str.contains("size="));
    }

    @Test
    @DisplayName("Set with small capacity should still work")
    void testSmallCapacity() {
        LinearScanHashSet<Integer> tinySet = new LinearScanHashSet<>(2);
        
        assertEquals(2, tinySet.capacity());
        assertTrue(tinySet.add(1));
        assertTrue(tinySet.add(2));
        // Should grow here
        assertTrue(tinySet.add(3));
        
        assertEquals(3, tinySet.size());
        assertTrue(tinySet.capacity() > 2);
    }

    @Test
    @DisplayName("Set should handle hash code edge cases")
    void testHashCodeEdgeCases() {
        LinearScanHashSet<Integer> intSet = new LinearScanHashSet<>(16);
        
        // Test with various hash values
        intSet.add(Integer.MIN_VALUE);
        intSet.add(Integer.MAX_VALUE);
        intSet.add(0);
        intSet.add(-1);
        intSet.add(1);
        
        assertEquals(5, intSet.size());
        assertTrue(intSet.contains(Integer.MIN_VALUE));
        assertTrue(intSet.contains(Integer.MAX_VALUE));
        assertTrue(intSet.contains(0));
        assertTrue(intSet.contains(-1));
        assertTrue(intSet.contains(1));
    }

    @Test
    @DisplayName("Set should maintain elements after growth")
    void testElementsAfterGrowth() {
        LinearScanHashSet<String> growSet = new LinearScanHashSet<>(4);
        
        growSet.add("a");
        growSet.add("b");
        growSet.add("c");
        growSet.add("d");
        growSet.add("e"); // Should trigger growth
        
        assertTrue(growSet.contains("a"));
        assertTrue(growSet.contains("b"));
        assertTrue(growSet.contains("c"));
        assertTrue(growSet.contains("d"));
        assertTrue(growSet.contains("e"));
        assertEquals(5, growSet.size());
    }

    @Test
    @DisplayName("Set should work with custom objects")
    void testCustomObjects() {
        record Person(String name, int age) {}
        
        LinearScanHashSet<Person> personSet = new LinearScanHashSet<>(16);
        
        Person p1 = new Person("Alice", 30);
        Person p2 = new Person("Bob", 25);
        Person p3 = new Person("Alice", 30); // Equal to p1
        
        assertTrue(personSet.add(p1));
        assertTrue(personSet.add(p2));
        assertFalse(personSet.add(p3)); // Duplicate of p1
        
        assertEquals(2, personSet.size());
    }
}
