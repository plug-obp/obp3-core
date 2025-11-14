package obp3.datastructures.collections.linearscan;

import obp3.hashcons.Hashable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for LinearScanHashMap including Map interface compliance,
 * tombstone handling, and invariant validation.
 */
@DisplayName("LinearScanHashMap Tests")
class LinearScanHashMapTest {

    private LinearScanHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new LinearScanHashMap<>(16);
    }

    @Test
    @DisplayName("New map should be empty")
    void testNewMapIsEmpty() {
        assertEquals(0, map.size());
        assertEquals(16, map.capacity());
    }

    @Test
    @DisplayName("put() should add new key-value pair and return null")
    void testPutNew() {
        assertNull(map.put("key1", 100));
        assertEquals(1, map.size());
        assertEquals(100, map.get("key1"));
    }

    @Test
    @DisplayName("put() should update existing key and return old value")
    void testPutUpdate() {
        map.put("key1", 100);
        Integer oldValue = map.put("key1", 200);
        
        assertEquals(100, oldValue);
        assertEquals(1, map.size());
        assertEquals(200, map.get("key1"));
    }

    @Test
    @DisplayName("put() should handle null keys")
    void testPutNullKey() {
        assertNull(map.put(null, 100));
        assertEquals(1, map.size());
        assertEquals(100, map.get(null));
        
        Integer oldValue = map.put(null, 200);
        assertEquals(100, oldValue);
        assertEquals(200, map.get(null));
    }

    @Test
    @DisplayName("put() should handle null values")
    void testPutNullValue() {
        assertNull(map.put("key1", null));
        assertEquals(1, map.size());
        assertNull(map.get("key1"));
        assertTrue(map.containsKey("key1"));
    }

    @Test
    @DisplayName("get() should return value for existing key")
    void testGetExisting() {
        map.put("key1", 100);
        map.put("key2", 200);
        
        assertEquals(100, map.get("key1"));
        assertEquals(200, map.get("key2"));
    }

    @Test
    @DisplayName("get() should return null for non-existing key")
    void testGetNonExisting() {
        assertNull(map.get("nonexistent"));
    }

    @Test
    @DisplayName("containsKey() should return true for existing keys")
    void testContainsKeyExisting() {
        map.put("key1", 100);
        map.put("key2", 200);
        
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
    }

    @Test
    @DisplayName("containsKey() should return false for non-existing keys")
    void testContainsKeyNonExisting() {
        assertFalse(map.containsKey("nonexistent"));
    }

    @Test
    @DisplayName("containsKey() should work with null key")
    void testContainsKeyNull() {
        assertFalse(map.containsKey(null));
        map.put(null, 100);
        assertTrue(map.containsKey(null));
    }

    @Test
    @DisplayName("Map should grow when load factor is reached")
    void testGrowth() {
        LinearScanHashMap<Integer, String> smallMap = new LinearScanHashMap<>(4);
        
        assertEquals(4, smallMap.capacity());
        
        // Load factor is 0.667, so 0.667 * 4 = 2.668
        // Growth happens when size >= 2.668
        smallMap.put(1, "one");
        smallMap.put(2, "two");
        assertEquals(4, smallMap.capacity()); // No growth yet
        
        smallMap.put(3, "three");
        
        // Check before growth
        int capacityBeforeGrowth = smallMap.capacity();
        smallMap.put(4, "four"); // This should trigger growth
        
        assertTrue(smallMap.capacity() > capacityBeforeGrowth);
        assertEquals(4, smallMap.size());
        assertEquals("one", smallMap.get(1));
        assertEquals("two", smallMap.get(2));
        assertEquals("three", smallMap.get(3));
        assertEquals("four", smallMap.get(4));
    }

    @Test
    @DisplayName("Map should handle collisions correctly")
    void testCollisions() {
        // Create hashable with many collisions
        Hashable<Integer> collidingHashable = x -> 1; // All hash to same value!
        LinearScanHashMap<Integer, String> collidingMap = new LinearScanHashMap<>(16, collidingHashable);
        
        collidingMap.put(1, "one");
        collidingMap.put(2, "two");
        collidingMap.put(3, "three");
        
        assertEquals(3, collidingMap.size());
        assertEquals("one", collidingMap.get(1));
        assertEquals("two", collidingMap.get(2));
        assertEquals("three", collidingMap.get(3));
    }

    @Test
    @DisplayName("Map should work with custom hashable")
    void testCustomHashable() {
        record CaseInsensitiveKey(String value) {}
        
        Hashable<CaseInsensitiveKey> caseInsensitive = new Hashable<CaseInsensitiveKey>() {
            @Override
            public int hash(CaseInsensitiveKey k) {
                return k == null ? 0 : k.value().toLowerCase().hashCode();
            }
            
            @Override
            public boolean equal(CaseInsensitiveKey k1, CaseInsensitiveKey k2) {
                if (k1 == null && k2 == null) return true;
                if (k1 == null || k2 == null) return false;
                return k1.value().equalsIgnoreCase(k2.value());
            }
        };
        
        LinearScanHashMap<CaseInsensitiveKey, Integer> ciMap = new LinearScanHashMap<>(16, caseInsensitive);
        
        ciMap.put(new CaseInsensitiveKey("Hello"), 1);
        Integer oldValue = ciMap.put(new CaseInsensitiveKey("HELLO"), 2); // Should update
        
        assertEquals(1, oldValue);
        assertEquals(1, ciMap.size());
        assertEquals(2, ciMap.get(new CaseInsensitiveKey("hello")));
    }

    @Test
    @DisplayName("Map should work with identity hashable")
    void testIdentityHashable() {
        LinearScanHashMap<String, Integer> identityMap = new LinearScanHashMap<>(16, Hashable.identity());
        
        String s1 = new String("test");
        String s2 = new String("test");
        
        identityMap.put(s1, 100);
        identityMap.put(s2, 200); // Different object, different key
        
        assertEquals(2, identityMap.size());
        assertEquals(100, identityMap.get(s1));
        assertEquals(200, identityMap.get(s2));
    }

    @Test
    @DisplayName("Map should handle many entries")
    void testManyEntries() {
        LinearScanHashMap<Integer, String> largeMap = new LinearScanHashMap<>(16);
        
        int count = 1000;
        for (int i = 0; i < count; i++) {
            assertNull(largeMap.put(i, "value" + i));
        }
        
        assertEquals(count, largeMap.size());
        
        for (int i = 0; i < count; i++) {
            assertEquals("value" + i, largeMap.get(i));
            assertTrue(largeMap.containsKey(i));
        }
        
        assertFalse(largeMap.containsKey(count));
    }

    @Test
    @DisplayName("toString() should include capacity and size")
    void testToString() {
        map.put("key1", 100);
        String str = map.toString();
        
        assertTrue(str.contains("capacity="));
        assertTrue(str.contains("size="));
    }

    @Test
    @DisplayName("Map with small capacity should still work")
    void testSmallCapacity() {
        LinearScanHashMap<Integer, String> tinyMap = new LinearScanHashMap<>(2);
        
        assertEquals(2, tinyMap.capacity());
        tinyMap.put(1, "one");
        tinyMap.put(2, "two");
        // Should grow here
        tinyMap.put(3, "three");
        
        assertEquals(3, tinyMap.size());
        assertTrue(tinyMap.capacity() > 2);
    }

    @Test
    @DisplayName("Map should handle hash code edge cases")
    void testHashCodeEdgeCases() {
        LinearScanHashMap<Integer, String> intMap = new LinearScanHashMap<>(16);
        
        intMap.put(Integer.MIN_VALUE, "min");
        intMap.put(Integer.MAX_VALUE, "max");
        intMap.put(0, "zero");
        intMap.put(-1, "minus one");
        intMap.put(1, "one");
        
        assertEquals(5, intMap.size());
        assertEquals("min", intMap.get(Integer.MIN_VALUE));
        assertEquals("max", intMap.get(Integer.MAX_VALUE));
        assertEquals("zero", intMap.get(0));
        assertEquals("minus one", intMap.get(-1));
        assertEquals("one", intMap.get(1));
    }

    @Test
    @DisplayName("Map should maintain entries after growth")
    void testEntriesAfterGrowth() {
        LinearScanHashMap<String, Integer> growMap = new LinearScanHashMap<>(4);
        
        growMap.put("a", 1);
        growMap.put("b", 2);
        growMap.put("c", 3);
        growMap.put("d", 4);
        growMap.put("e", 5); // Should trigger growth
        
        assertEquals(1, growMap.get("a"));
        assertEquals(2, growMap.get("b"));
        assertEquals(3, growMap.get("c"));
        assertEquals(4, growMap.get("d"));
        assertEquals(5, growMap.get("e"));
        assertEquals(5, growMap.size());
    }

    @Test
    @DisplayName("Map should work with custom key types")
    void testCustomKeyTypes() {
        record Point(int x, int y) {}
        
        LinearScanHashMap<Point, String> pointMap = new LinearScanHashMap<>(16);
        
        Point p1 = new Point(1, 2);
        Point p2 = new Point(3, 4);
        Point p3 = new Point(1, 2); // Equal to p1
        
        pointMap.put(p1, "first");
        pointMap.put(p2, "second");
        String oldValue = pointMap.put(p3, "third"); // Should update p1's value
        
        assertEquals("first", oldValue);
        assertEquals(2, pointMap.size());
        assertEquals("third", pointMap.get(p1));
        assertEquals("third", pointMap.get(p3));
        assertEquals("second", pointMap.get(p2));
    }

    @Test
    @DisplayName("Map should handle sequential updates")
    void testSequentialUpdates() {
        map.put("key", 1);
        assertEquals(1, map.get("key"));
        
        map.put("key", 2);
        assertEquals(2, map.get("key"));
        
        map.put("key", 3);
        assertEquals(3, map.get("key"));
        
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("Map should distinguish between null value and missing key")
    void testNullValueVsMissingKey() {
        map.put("key1", null);
        
        assertTrue(map.containsKey("key1"));
        assertNull(map.get("key1"));
        
        assertFalse(map.containsKey("key2"));
        assertNull(map.get("key2"));
    }

    @Test
    @DisplayName("Map should handle mixed null and non-null keys")
    void testMixedNullKeys() {
        map.put(null, 1);
        map.put("key1", 2);
        map.put("key2", 3);
        
        assertEquals(3, map.size());
        assertEquals(1, map.get(null));
        assertEquals(2, map.get("key1"));
        assertEquals(3, map.get("key2"));
        
        assertTrue(map.containsKey(null));
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertFalse(map.containsKey("key3"));
    }

    @Test
    @DisplayName("Map operations should maintain correct size")
    void testSizeConsistency() {
        assertEquals(0, map.size());
        
        map.put("k1", 1);
        assertEquals(1, map.size());
        
        map.put("k2", 2);
        assertEquals(2, map.size());
        
        map.put("k1", 10); // Update, size shouldn't change
        assertEquals(2, map.size());
        
        map.put("k3", 3);
        assertEquals(3, map.size());
    }

    // ==================== MAP INTERFACE TESTS ====================

    @Test
    @DisplayName("remove() should remove existing key and return value")
    void testRemoveExisting() {
        map.put("key1", 100);
        map.put("key2", 200);
        
        Integer removed = map.remove("key1");
        
        assertEquals(100, removed);
        assertEquals(1, map.size());
        assertFalse(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
    }

    @Test
    @DisplayName("remove() should return null for non-existing key")
    void testRemoveNonExisting() {
        Integer removed = map.remove("nonexistent");
        
        assertNull(removed);
        assertEquals(0, map.size());
    }

    @Test
    @DisplayName("remove() should handle null key")
    void testRemoveNullKey() {
        map.put(null, 100);
        map.put("key1", 200);
        
        Integer removed = map.remove(null);
        
        assertEquals(100, removed);
        assertEquals(1, map.size());
        assertFalse(map.containsKey(null));
    }

    @Test
    @DisplayName("clear() should remove all entries")
    void testClear() {
        map.put("key1", 100);
        map.put("key2", 200);
        map.put("key3", 300);
        
        map.clear();
        
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
        assertFalse(map.containsKey("key3"));
    }

    @Test
    @DisplayName("isEmpty() should return true for empty map")
    void testIsEmpty() {
        assertTrue(map.isEmpty());
        
        map.put("key1", 100);
        assertFalse(map.isEmpty());
        
        map.remove("key1");
        assertTrue(map.isEmpty());
    }

    @Test
    @DisplayName("containsValue() should return true for existing value")
    void testContainsValue() {
        map.put("key1", 100);
        map.put("key2", 200);
        
        assertTrue(map.containsValue(100));
        assertTrue(map.containsValue(200));
        assertFalse(map.containsValue(300));
    }

    @Test
    @DisplayName("containsValue() should handle null value")
    void testContainsValueNull() {
        map.put("key1", null);
        assertTrue(map.containsValue(null));
    }

    @Test
    @DisplayName("keySet() should return all keys")
    void testKeySet() {
        map.put("key1", 100);
        map.put("key2", 200);
        map.put("key3", 300);
        
        Set<String> keys = map.keySet();
        
        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));
    }

    @Test
    @DisplayName("values() should return all values")
    void testValues() {
        map.put("key1", 100);
        map.put("key2", 200);
        map.put("key3", 300);
        
        Collection<Integer> values = map.values();
        
        assertEquals(3, values.size());
        assertTrue(values.contains(100));
        assertTrue(values.contains(200));
        assertTrue(values.contains(300));
    }

    @Test
    @DisplayName("entrySet() should return all entries")
    void testEntrySet() {
        map.put("key1", 100);
        map.put("key2", 200);
        
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        
        assertEquals(2, entries.size());
        
        Map<String, Integer> reconstructed = new HashMap<>();
        for (Map.Entry<String, Integer> entry : entries) {
            reconstructed.put(entry.getKey(), entry.getValue());
        }
        
        assertEquals(100, reconstructed.get("key1"));
        assertEquals(200, reconstructed.get("key2"));
    }

    @Test
    @DisplayName("putAll() should add all entries from another map")
    void testPutAll() {
        Map<String, Integer> other = new HashMap<>();
        other.put("key1", 100);
        other.put("key2", 200);
        other.put("key3", 300);
        
        map.putAll(other);
        
        assertEquals(3, map.size());
        assertEquals(100, map.get("key1"));
        assertEquals(200, map.get("key2"));
        assertEquals(300, map.get("key3"));
    }

    // ==================== TOMBSTONE TESTS ====================

    @Test
    @DisplayName("Tombstones should be reused on put")
    void testTombstoneReuse() {
        map.put("key1", 100);
        map.remove("key1");
        
        map.put("key2", 200);
        
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key2"));
        assertFalse(map.containsKey("key1"));
    }

    @Test
    @DisplayName("Linear probing should work through tombstones")
    void testLinearProbingWithTombstones() {
        LinearScanHashMap<Integer, String> intMap = new LinearScanHashMap<>(4);
        
        intMap.put(0, "zero");
        intMap.put(4, "four");   // Collides with 0
        intMap.put(8, "eight");  // Collides with 0 and 4
        
        // Remove middle element
        intMap.remove(4);
        
        // Should still find the last element through the tombstone
        assertTrue(intMap.containsKey(8));
        assertEquals("eight", intMap.get(8));
    }

    @Test
    @DisplayName("Growth should clear tombstones")
    void testGrowthClearsTombstones() {
        LinearScanHashMap<Integer, String> intMap = new LinearScanHashMap<>(4);
        
        intMap.put(1, "one");
        intMap.put(2, "two");
        intMap.put(3, "three");
        intMap.remove(2);  // Create tombstone
        
        // Force growth
        intMap.put(4, "four");
        intMap.put(5, "five");
        
        // After growth, all remaining elements should be accessible
        assertTrue(intMap.containsKey(1));
        assertFalse(intMap.containsKey(2));  // Was removed
        assertTrue(intMap.containsKey(3));
        assertTrue(intMap.containsKey(4));
        assertTrue(intMap.containsKey(5));
        assertEquals(4, intMap.size());
    }

    @Test
    @DisplayName("Remove and re-put should work correctly")
    void testRemoveAndRePut() {
        map.put("key1", 100);
        map.remove("key1");
        map.put("key1", 200);
        
        assertEquals(1, map.size());
        assertEquals(200, map.get("key1"));
    }

    // ==================== INVARIANT TESTS ====================
    // Tests specifically designed to validate the formal invariants
    // documented in the class JavaDoc

    @Test
    @DisplayName("Invariant: |M| ≥ 0 always holds")
    void testInvariantSizeNonNegative() {
        assertTrue(map.size() >= 0);
        map.put("a", 1);
        assertTrue(map.size() >= 0);
        map.remove("a");
        assertTrue(map.size() >= 0);
        map.clear();
        assertTrue(map.size() >= 0);
    }

    @Test
    @DisplayName("Invariant: isEmpty(M) ⟺ |M| = 0")
    void testInvariantIsEmptyEquivalence() {
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        
        map.put("a", 1);
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        
        map.remove("a");
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    @DisplayName("Invariant: k ∉ M → put(M, k, v) = null ∧ (k → v) ∈ M' ∧ |M'| = |M| + 1")
    void testInvariantPutNewKey() {
        assertFalse(map.containsKey("x"));
        int sizeBefore = map.size();
        
        Integer result = map.put("x", 100);
        
        assertNull(result);
        assertTrue(map.containsKey("x"));
        assertEquals(100, map.get("x"));
        assertEquals(sizeBefore + 1, map.size());
    }

    @Test
    @DisplayName("Invariant: k → v' ∈ M → put(M, k, v) = v' ∧ (k → v) ∈ M' ∧ |M'| = |M|")
    void testInvariantPutExistingKey() {
        map.put("x", 100);
        assertTrue(map.containsKey("x"));
        int sizeBefore = map.size();
        
        Integer result = map.put("x", 200);
        
        assertEquals(100, result);
        assertEquals(200, map.get("x"));
        assertEquals(sizeBefore, map.size());
    }

    @Test
    @DisplayName("Invariant: k → v ∈ M → get(M, k) = v")
    void testInvariantGetExistingKey() {
        map.put("x", 100);
        
        assertEquals(100, map.get("x"));
    }

    @Test
    @DisplayName("Invariant: k ∉ M → get(M, k) = null")
    void testInvariantGetNonExistingKey() {
        assertNull(map.get("nonexistent"));
    }

    @Test
    @DisplayName("Invariant: containsKey(M, k) ⟺ ∃v: k → v ∈ M")
    void testInvariantContainsKey() {
        // k not in M
        assertFalse(map.containsKey("x"));
        
        // Add k
        map.put("x", 100);
        assertTrue(map.containsKey("x"));
        
        // Remove k
        map.remove("x");
        assertFalse(map.containsKey("x"));
    }

    @Test
    @DisplayName("Invariant: containsValue(M, v) ⟺ ∃k: k → v ∈ M")
    void testInvariantContainsValue() {
        assertFalse(map.containsValue(100));
        
        map.put("x", 100);
        assertTrue(map.containsValue(100));
        
        map.remove("x");
        assertFalse(map.containsValue(100));
    }

    @Test
    @DisplayName("Invariant: k → v ∈ M → remove(M, k) = v ∧ k ∉ M' ∧ |M'| = |M| - 1")
    void testInvariantRemoveExistingKey() {
        map.put("x", 100);
        assertTrue(map.containsKey("x"));
        int sizeBefore = map.size();
        
        Integer result = map.remove("x");
        
        assertEquals(100, result);
        assertFalse(map.containsKey("x"));
        assertEquals(sizeBefore - 1, map.size());
    }

    @Test
    @DisplayName("Invariant: k ∉ M → remove(M, k) = null ∧ M' = M ∧ |M'| = |M|")
    void testInvariantRemoveNonExistingKey() {
        assertFalse(map.containsKey("x"));
        int sizeBefore = map.size();
        
        Integer result = map.remove("x");
        
        assertNull(result);
        assertEquals(sizeBefore, map.size());
    }

    @Test
    @DisplayName("Invariant: clear(M) → M' = ∅ ∧ |M'| = 0")
    void testInvariantClear() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        
        map.clear();
        
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsKey("b"));
        assertFalse(map.containsKey("c"));
    }

    @Test
    @DisplayName("Invariant: putAll(M, M2) → ∀(k → v) ∈ M2: get(M', k) = v")
    void testInvariantPutAll() {
        Map<String, Integer> other = new HashMap<>();
        other.put("a", 1);
        other.put("b", 2);
        other.put("c", 3);
        
        map.putAll(other);
        
        for (Map.Entry<String, Integer> entry : other.entrySet()) {
            assertEquals(entry.getValue(), map.get(entry.getKey()));
        }
    }

    @Test
    @DisplayName("Invariant: keySet(M) = {k | ∃v: k → v ∈ M}")
    void testInvariantKeySet() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        
        Set<String> keys = map.keySet();
        
        assertEquals(map.size(), keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
        assertTrue(keys.contains("c"));
        assertFalse(keys.contains("d"));
    }

    @Test
    @DisplayName("Invariant: |M| ≤ capacity(M)")
    void testInvariantSizeWithinCapacity() {
        LinearScanHashMap<Integer, String> intMap = new LinearScanHashMap<>(4);
        
        for (int i = 0; i < 20; i++) {
            intMap.put(i, "value" + i);
            assertTrue(intMap.size() <= intMap.capacity());
        }
    }

    @Test
    @DisplayName("Invariant: Null keys and values are valid")
    void testInvariantNullHandling() {
        // Null key
        map.put(null, 100);
        assertTrue(map.containsKey(null));
        assertEquals(100, map.get(null));
        
        // Null value
        map.put("key1", null);
        assertTrue(map.containsKey("key1"));
        assertNull(map.get("key1"));
        assertTrue(map.containsValue(null));
        
        // Both null
        map.put(null, null);
        assertTrue(map.containsKey(null));
        assertNull(map.get(null));
    }

    @Test
    @DisplayName("Invariant: Tombstones preserve linear probing chains")
    void testInvariantTombstonesPreserveChains() {
        LinearScanHashMap<Integer, String> intMap = new LinearScanHashMap<>(4);
        
        intMap.put(0, "zero");
        intMap.put(4, "four");   // Collides
        intMap.put(8, "eight");  // Collides
        
        intMap.remove(4);  // Create tombstone
        
        // Should still find last element through tombstone
        assertTrue(intMap.containsKey(8));
        assertEquals("eight", intMap.get(8));
    }

    @Test
    @DisplayName("Invariant: Tombstones are eliminated during growth")
    void testInvariantTombstonesEliminatedOnGrowth() {
        LinearScanHashMap<Integer, String> intMap = new LinearScanHashMap<>(4);
        
        intMap.put(1, "one");
        intMap.put(2, "two");
        intMap.put(3, "three");
        
        // Create tombstone
        intMap.remove(2);
        assertEquals(2, intMap.size());
        
        // Force growth
        intMap.put(4, "four");
        intMap.put(5, "five");
        
        // After growth, tombstones should be gone, only active entries remain
        assertEquals(4, intMap.size());
        assertTrue(intMap.containsKey(1));
        assertFalse(intMap.containsKey(2));
        assertTrue(intMap.containsKey(3));
        assertTrue(intMap.containsKey(4));
        assertTrue(intMap.containsKey(5));
    }

    @Test
    @DisplayName("Complex invariant test: Multiple operations maintain all invariants")
    void testInvariantComplexScenario() {
        // Start with empty map
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        
        // Add entries
        map.put("a", 1);
        assertEquals(1, map.size());
        map.put("b", 2);
        assertEquals(2, map.size());
        map.put("c", 3);
        assertEquals(3, map.size());
        
        // Update existing
        Integer old = map.put("a", 10);
        assertEquals(1, old);
        assertEquals(3, map.size());
        assertEquals(10, map.get("a"));
        
        // All keys are present
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
        
        // Remove entry
        Integer removed = map.remove("b");
        assertEquals(2, removed);
        assertEquals(2, map.size());
        assertFalse(map.containsKey("b"));
        
        // Can add after remove
        map.put("d", 4);
        assertEquals(3, map.size());
        
        // putAll
        Map<String, Integer> other = new HashMap<>();
        other.put("e", 5);
        other.put("f", 6);
        map.putAll(other);
        assertEquals(5, map.size());
        
        // keySet has correct size
        assertEquals(5, map.keySet().size());
        
        // Clear empties map
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }
}
