package obp3.datastructures.collections.linearscan;

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

    // ==================== INVARIANT TESTS ====================
    // Tests specifically designed to validate the formal invariants
    // documented in the class JavaDoc

    @Test
    @DisplayName("Invariant: |S| ≥ 0 always holds")
    void testInvariantSizeNonNegative() {
        assertTrue(set.size() >= 0);
        set.add("a");
        assertTrue(set.size() >= 0);
        set.remove("a");
        assertTrue(set.size() >= 0);
        set.clear();
        assertTrue(set.size() >= 0);
    }

    @Test
    @DisplayName("Invariant: isEmpty(S) ⟺ |S| = 0")
    void testInvariantIsEmptyEquivalence() {
        // Empty set
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
        
        // Non-empty set
        set.add("a");
        assertEquals(1, set.size());
        assertFalse(set.isEmpty());
        
        // After removal back to empty
        set.remove("a");
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }

    @Test
    @DisplayName("Invariant: x ∉ S → add(S, x) = true ∧ x ∈ S' ∧ |S'| = |S| + 1")
    void testInvariantAddNewElement() {
        assertFalse(set.contains("x"));
        int sizeBefore = set.size();
        
        boolean result = set.add("x");
        
        assertTrue(result);
        assertTrue(set.contains("x"));
        assertEquals(sizeBefore + 1, set.size());
    }

    @Test
    @DisplayName("Invariant: x ∈ S → add(S, x) = false ∧ S' = S ∧ |S'| = |S|")
    void testInvariantAddExistingElement() {
        set.add("x");
        assertTrue(set.contains("x"));
        int sizeBefore = set.size();
        
        boolean result = set.add("x");
        
        assertFalse(result);
        assertTrue(set.contains("x"));
        assertEquals(sizeBefore, set.size());
    }

    @Test
    @DisplayName("Invariant: x ∈ S → remove(S, x) = true ∧ x ∉ S' ∧ |S'| = |S| - 1")
    void testInvariantRemoveExistingElement() {
        set.add("x");
        assertTrue(set.contains("x"));
        int sizeBefore = set.size();
        
        boolean result = set.remove("x");
        
        assertTrue(result);
        assertFalse(set.contains("x"));
        assertEquals(sizeBefore - 1, set.size());
    }

    @Test
    @DisplayName("Invariant: x ∉ S → remove(S, x) = false ∧ S' = S ∧ |S'| = |S|")
    void testInvariantRemoveNonExistingElement() {
        assertFalse(set.contains("x"));
        int sizeBefore = set.size();
        
        boolean result = set.remove("x");
        
        assertFalse(result);
        assertFalse(set.contains("x"));
        assertEquals(sizeBefore, set.size());
    }

    @Test
    @DisplayName("Invariant: clear(S) → S' = ∅ ∧ |S'| = 0")
    void testInvariantClear() {
        set.add("a");
        set.add("b");
        set.add("c");
        
        set.clear();
        
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
        assertFalse(set.contains("a"));
        assertFalse(set.contains("b"));
        assertFalse(set.contains("c"));
    }

    @Test
    @DisplayName("Invariant: contains(S, x) ⟺ x ∈ S")
    void testInvariantContainsMembership() {
        // x not in S
        assertFalse(set.contains("x"));
        
        // Add x
        set.add("x");
        assertTrue(set.contains("x"));
        
        // Remove x
        set.remove("x");
        assertFalse(set.contains("x"));
    }

    @Test
    @DisplayName("Invariant: x ∈ S → get(S, x) = x")
    void testInvariantGetReturnsElement() {
        String element = new String("test");
        set.add(element);
        
        String retrieved = set.get("test");
        
        assertNotNull(retrieved);
        assertSame(element, retrieved);
    }

    @Test
    @DisplayName("Invariant: x ∉ S → get(S, x) = null")
    void testInvariantGetReturnsNullForNonExisting() {
        assertNull(set.get("nonexistent"));
    }

    @Test
    @DisplayName("Invariant: addAll(S, C) → ∀x ∈ C: x ∈ S'")
    void testInvariantAddAllMembership() {
        java.util.List<String> collection = java.util.Arrays.asList("a", "b", "c");
        
        set.addAll(collection);
        
        for (String x : collection) {
            assertTrue(set.contains(x), "Element " + x + " should be in set");
        }
    }

    @Test
    @DisplayName("Invariant: addAll(S, C) = true ⟺ ∃x ∈ C: x ∉ S")
    void testInvariantAddAllReturnValue() {
        set.add("a");
        
        // Case 1: Some elements not in S
        boolean result1 = set.addAll(java.util.Arrays.asList("a", "b", "c"));
        assertTrue(result1, "addAll should return true when adding new elements");
        
        // Case 2: All elements already in S
        boolean result2 = set.addAll(java.util.Arrays.asList("a", "b", "c"));
        assertFalse(result2, "addAll should return false when all elements exist");
    }

    @Test
    @DisplayName("Invariant: removeAll(S, C) → ∀x ∈ C: x ∉ S'")
    void testInvariantRemoveAllMembership() {
        set.add("a");
        set.add("b");
        set.add("c");
        set.add("d");
        
        java.util.List<String> toRemove = java.util.Arrays.asList("a", "c");
        set.removeAll(toRemove);
        
        for (String x : toRemove) {
            assertFalse(set.contains(x), "Element " + x + " should not be in set");
        }
        assertTrue(set.contains("b"));
        assertTrue(set.contains("d"));
    }

    @Test
    @DisplayName("Invariant: removeAll(S, C) = true ⟺ ∃x ∈ (S ∩ C)")
    void testInvariantRemoveAllReturnValue() {
        set.add("a");
        set.add("b");
        
        // Case 1: Some elements in intersection
        boolean result1 = set.removeAll(java.util.Arrays.asList("a", "c"));
        assertTrue(result1, "removeAll should return true when removing existing elements");
        
        // Case 2: No elements in intersection
        boolean result2 = set.removeAll(java.util.Arrays.asList("x", "y"));
        assertFalse(result2, "removeAll should return false when no elements to remove");
    }

    @Test
    @DisplayName("Invariant: retainAll(S, C) → S' = S ∩ C")
    void testInvariantRetainAllIntersection() {
        set.add("a");
        set.add("b");
        set.add("c");
        set.add("d");
        
        java.util.Set<String> toRetain = java.util.Set.of("a", "c", "e");
        set.retainAll(toRetain);
        
        // Should only contain elements in intersection
        assertTrue(set.contains("a"));
        assertFalse(set.contains("b"));
        assertTrue(set.contains("c"));
        assertFalse(set.contains("d"));
        assertFalse(set.contains("e"));
        assertEquals(2, set.size());
    }

    @Test
    @DisplayName("Invariant: retainAll(S, C) = true ⟺ ∃x ∈ S: x ∉ C")
    void testInvariantRetainAllReturnValue() {
        set.add("a");
        set.add("b");
        set.add("c");
        
        // Case 1: Some elements not in C (will be removed)
        boolean result1 = set.retainAll(java.util.Set.of("a", "b"));
        assertTrue(result1, "retainAll should return true when removing elements");
        
        // Case 2: All elements in C (no removals)
        boolean result2 = set.retainAll(java.util.Set.of("a", "b", "d"));
        assertFalse(result2, "retainAll should return false when no elements removed");
    }

    @Test
    @DisplayName("Invariant: containsAll(S, C) ⟺ C ⊆ S")
    void testInvariantContainsAllSubset() {
        set.add("a");
        set.add("b");
        set.add("c");
        
        // C ⊆ S
        assertTrue(set.containsAll(java.util.Arrays.asList("a", "b")));
        assertTrue(set.containsAll(java.util.Arrays.asList("a", "b", "c")));
        
        // C ⊄ S
        assertFalse(set.containsAll(java.util.Arrays.asList("a", "b", "d")));
        assertFalse(set.containsAll(java.util.Arrays.asList("x", "y")));
    }

    @Test
    @DisplayName("Invariant: iterator generates each element exactly once")
    void testInvariantIteratorUniqueElements() {
        set.add("a");
        set.add("b");
        set.add("c");
        
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String element : set) {
            assertFalse(seen.contains(element), "Iterator should not yield " + element + " twice");
            seen.add(element);
        }
        
        assertEquals(set.size(), seen.size());
    }

    @Test
    @DisplayName("Invariant: |{x | x yielded by iterator}| = |S|")
    void testInvariantIteratorCount() {
        set.add("a");
        set.add("b");
        set.add("c");
        set.remove("b");  // Create tombstone
        set.add("d");
        
        int count = 0;
        for (String element : set) {
            count++;
        }
        
        assertEquals(set.size(), count, "Iterator should yield exactly |S| elements");
    }

    @Test
    @DisplayName("Invariant: iterator.hasNext() = false ⟺ all elements yielded")
    void testInvariantIteratorExhaustion() {
        set.add("a");
        set.add("b");
        
        java.util.Iterator<String> iter = set.iterator();
        
        int expectedCount = set.size();
        int actualCount = 0;
        
        while (iter.hasNext()) {
            iter.next();
            actualCount++;
        }
        
        assertEquals(expectedCount, actualCount);
        assertFalse(iter.hasNext(), "hasNext() should be false after all elements yielded");
    }

    @Test
    @DisplayName("Invariant: addIfAbsent(S, x) ⟺ add(S, x)")
    void testInvariantAddIfAbsentEquivalence() {
        LinearScanHashSet<String> set1 = new LinearScanHashSet<>(16);
        LinearScanHashSet<String> set2 = new LinearScanHashSet<>(16);
        
        // Test equivalence for new element
        assertEquals(set1.add("x"), set2.addIfAbsent("x"));
        assertEquals(set1.size(), set2.size());
        
        // Test equivalence for existing element
        assertEquals(set1.add("x"), set2.addIfAbsent("x"));
        assertEquals(set1.size(), set2.size());
    }

    @Test
    @DisplayName("Invariant: Null is treated as any other element value")
    void testInvariantNullHandling() {
        // Add null
        assertTrue(set.add(null));
        assertTrue(set.contains(null));
        assertEquals(1, set.size());
        
        // Add null again (duplicate)
        assertFalse(set.add(null));
        assertEquals(1, set.size());
        
        // Remove null
        assertTrue(set.remove(null));
        assertFalse(set.contains(null));
        assertEquals(0, set.size());
        
        // Remove null again (not present)
        assertFalse(set.remove(null));
    }

    @Test
    @DisplayName("Invariant: Tombstones preserve linear probing chains")
    void testInvariantTombstonesPreserveChains() {
        // Force collisions
        LinearScanHashSet<Integer> intSet = new LinearScanHashSet<>(4);
        intSet.add(0);  // hash % 4 = 0
        intSet.add(4);  // hash % 4 = 0, goes to slot 1
        intSet.add(8);  // hash % 4 = 0, goes to slot 2
        
        // Remove middle element
        assertTrue(intSet.remove(4));
        
        // Should still find last element through the tombstone
        assertTrue(intSet.contains(8), "Linear probing through tombstone should find element");
        assertEquals(Integer.valueOf(8), intSet.get(8));
    }

    @Test
    @DisplayName("Invariant: Tombstones are eliminated during growth")
    void testInvariantTombstonesEliminatedOnGrowth() {
        LinearScanHashSet<Integer> intSet = new LinearScanHashSet<>(4);
        
        // Add and remove to create tombstones
        intSet.add(1);
        intSet.add(2);
        intSet.add(3);
        intSet.remove(2);  // Creates tombstone
        
        int capacityBefore = intSet.capacity();
        
        // Force growth
        intSet.add(4);
        intSet.add(5);
        
        // After growth, all elements should still be accessible
        assertTrue(intSet.contains(1));
        assertFalse(intSet.contains(2));  // Was removed
        assertTrue(intSet.contains(3));
        assertTrue(intSet.contains(4));
        assertTrue(intSet.contains(5));
        assertEquals(4, intSet.size());
        assertTrue(intSet.capacity() > capacityBefore);
    }

    @Test
    @DisplayName("Invariant: |S| ≤ capacity(S)")
    void testInvariantSizeWithinCapacity() {
        LinearScanHashSet<Integer> intSet = new LinearScanHashSet<>(4);
        
        for (int i = 0; i < 20; i++) {
            intSet.add(i);
            assertTrue(intSet.size() <= intSet.capacity(), 
                "Size should never exceed capacity");
        }
    }

    @Test
    @DisplayName("Complex invariant test: Multiple operations maintain all invariants")
    void testInvariantComplexScenario() {
        // Start with empty set: |S| = 0, isEmpty = true
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
        
        // Add elements: each add increases size by 1
        set.add("a");
        assertEquals(1, set.size());
        set.add("b");
        assertEquals(2, set.size());
        set.add("c");
        assertEquals(3, set.size());
        
        // Duplicate add doesn't change size
        assertFalse(set.add("a"));
        assertEquals(3, set.size());
        
        // All added elements are contained
        assertTrue(set.contains("a"));
        assertTrue(set.contains("b"));
        assertTrue(set.contains("c"));
        
        // Remove decreases size
        assertTrue(set.remove("b"));
        assertEquals(2, set.size());
        assertFalse(set.contains("b"));
        
        // Can add after remove
        assertTrue(set.add("d"));
        assertEquals(3, set.size());
        
        // Bulk operations
        set.addAll(java.util.Arrays.asList("e", "f"));
        assertEquals(5, set.size());
        
        set.removeAll(java.util.Arrays.asList("a", "c"));
        assertEquals(3, set.size());
        assertFalse(set.contains("a"));
        assertFalse(set.contains("c"));
        assertTrue(set.contains("d"));
        assertTrue(set.contains("e"));
        assertTrue(set.contains("f"));
        
        // Iterator yields correct count
        int count = 0;
        for (String s : set) {
            count++;
        }
        assertEquals(set.size(), count);
        
        // Clear empties set
        set.clear();
        assertEquals(0, set.size());
        assertTrue(set.isEmpty());
    }
}
