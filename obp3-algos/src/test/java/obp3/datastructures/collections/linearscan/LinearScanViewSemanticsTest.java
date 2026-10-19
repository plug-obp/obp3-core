package obp3.datastructures.collections.linearscan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LinearScanViewSemanticsTest {

    private LinearScanHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new LinearScanHashMap<>(16);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
    }

    @Test
    @DisplayName("keySet() is a live view: removal writes through")
    void keySetRemoveWritesThrough() {
        assertTrue(map.keySet().remove("a"));
        assertFalse(map.containsKey("a"));
        assertEquals(2, map.size());
    }

    @Test
    @DisplayName("keySet() reflects later map changes")
    void keySetIsLive() {
        Set<String> keys = map.keySet();
        map.put("d", 4);
        assertTrue(keys.contains("d"));
        assertEquals(4, keys.size());
        map.remove("a");
        assertFalse(keys.contains("a"));
    }

    @Test
    @DisplayName("keySet().clear() clears the map")
    void keySetClear() {
        map.keySet().clear();
        assertTrue(map.isEmpty());
    }

    @Test
    @DisplayName("keySet() does not support add")
    void keySetAddUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().add("z"));
    }

    @Test
    @DisplayName("values() is a live view: removal and iterator.remove write through")
    void valuesWriteThrough() {
        Collection<Integer> values = map.values();
        assertTrue(values.remove(2));
        assertFalse(map.containsKey("b"));

        Iterator<Integer> it = values.iterator();
        while (it.hasNext()) {
            if (it.next() == 1) {
                it.remove();
            }
        }
        assertFalse(map.containsKey("a"));
        assertEquals(1, map.size());
        map.put("z", 26);
        assertTrue(values.contains(26));
    }

    @Test
    @DisplayName("entrySet() entries write setValue through to the map")
    void entrySetSetValueWritesThrough() {
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            e.setValue(e.getValue() * 10);
        }
        assertEquals(10, map.get("a"));
        assertEquals(20, map.get("b"));
        assertEquals(30, map.get("c"));
    }

    @Test
    @DisplayName("entrySet() iterator.remove and remove(Object) write through")
    void entrySetRemoveWritesThrough() {
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getKey().equals("a")) {
                it.remove();
            }
        }
        assertFalse(map.containsKey("a"));
        assertTrue(map.entrySet().remove(new AbstractMap.SimpleEntry<>("b", 2)));
        assertFalse(map.containsKey("b"));
        assertFalse(map.entrySet().remove(new AbstractMap.SimpleEntry<>("c", 99)));
        assertTrue(map.containsKey("c"));
    }

    @Test
    @DisplayName("entrySet().contains matches on key and value")
    void entrySetContains() {
        assertTrue(map.entrySet().contains(new AbstractMap.SimpleEntry<>("a", 1)));
        assertFalse(map.entrySet().contains(new AbstractMap.SimpleEntry<>("a", 2)));
    }

    @Test
    @DisplayName("removeIf on views mutates the map")
    void removeIfOnViews() {
        map.values().removeIf(v -> v >= 2);
        assertEquals(Map.of("a", 1), map);
        map.put("x", 5);
        map.keySet().removeIf("x"::equals);
        assertEquals(Map.of("a", 1), map);
    }

    @Test
    @DisplayName("iterators support null keys/values and tombstones")
    void viewsWithNullsAndTombstones() {
        map.put(null, null);
        map.remove("b");
        assertTrue(map.keySet().contains(null));
        assertTrue(map.values().contains(null));
        assertEquals(3, map.entrySet().size());
        map.keySet().remove(null);
        assertFalse(map.containsKey(null));
    }

    @Test
    @DisplayName("equals/hashCode follow the Map contract")
    void mapEqualsHashCode() {
        Map<String, Integer> hash = new HashMap<>(Map.of("a", 1, "b", 2, "c", 3));
        assertEquals(hash, map);
        assertEquals(map, hash);
        assertEquals(hash.hashCode(), map.hashCode());

        LinearScanHashMap<String, Integer> other = new LinearScanHashMap<>(4);
        other.putAll(hash);
        assertEquals(map, other);
        assertEquals(map.hashCode(), other.hashCode());

        other.put("c", 4);
        assertNotEquals(map, other);
        other.remove("c");
        assertNotEquals(map, other);
    }

    @Test
    @DisplayName("equals/hashCode handle null keys and values")
    void mapEqualsNulls() {
        map.put(null, null);
        Map<String, Integer> hash = new HashMap<>(Map.of("a", 1, "b", 2, "c", 3));
        hash.put(null, null);
        assertEquals(hash, map);
        assertEquals(hash.hashCode(), map.hashCode());
    }

    @Test
    @DisplayName("Set equals/hashCode follow the Set contract")
    void setEqualsHashCode() {
        LinearScanHashSet<String> set = new LinearScanHashSet<>(8);
        set.addAll(List.of("a", "b", "c"));
        Set<String> hash = new HashSet<>(List.of("a", "b", "c"));
        assertEquals(hash, set);
        assertEquals(set, hash);
        assertEquals(hash.hashCode(), set.hashCode());

        LinearScanHashSet<String> other = new LinearScanHashSet<>(2);
        other.addAll(hash);
        assertEquals(set, other);
        other.remove("c");
        assertNotEquals(set, other);
    }

    @Test
    @DisplayName("Set iterator supports remove")
    void setIteratorRemove() {
        LinearScanHashSet<Integer> set = new LinearScanHashSet<>(8);
        for (int i = 0; i < 6; i++) {
            set.add(i);
        }
        Iterator<Integer> it = set.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) {
                it.remove();
            }
        }
        assertEquals(Set.of(1, 3, 5), set);
        assertEquals(3, set.size());
    }

    @Test
    @DisplayName("Set.iterator().remove() without next() throws")
    void setIteratorRemoveIllegal() {
        LinearScanHashSet<Integer> set = new LinearScanHashSet<>(8);
        set.add(1);
        assertThrows(IllegalStateException.class, () -> set.iterator().remove());
    }
}
