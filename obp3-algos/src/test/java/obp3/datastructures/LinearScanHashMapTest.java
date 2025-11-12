package obp3.datastructures;

import obp3.hashcons.Hashable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

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
}
