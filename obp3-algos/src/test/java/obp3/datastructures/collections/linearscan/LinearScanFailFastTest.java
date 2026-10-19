package obp3.datastructures.collections.linearscan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LinearScanFailFastTest {

    private static LinearScanHashMap<Integer, Integer> map(int n) {
        LinearScanHashMap<Integer, Integer> m = new LinearScanHashMap<>(4);
        for (int i = 0; i < n; i++) {
            m.put(i, i);
        }
        return m;
    }

    private static LinearScanHashSet<Integer> set(int n) {
        LinearScanHashSet<Integer> s = new LinearScanHashSet<>(4);
        for (int i = 0; i < n; i++) {
            s.add(i);
        }
        return s;
    }

    @Test
    @DisplayName("map views: put of a new key (rehash) during iteration throws CME")
    void mapPutNewKeyWithRehash() {
        LinearScanHashMap<Integer, Integer> m = map(2);
        int cap = m.capacity();
        Iterator<Integer> keys = m.keySet().iterator();
        Iterator<Integer> vals = m.values().iterator();
        Iterator<Map.Entry<Integer, Integer>> ents = m.entrySet().iterator();
        keys.next();
        vals.next();
        ents.next();
        for (int i = 100; m.capacity() == cap; i++) {
            m.put(i, i);
        }
        assertThrows(ConcurrentModificationException.class, keys::next);
        assertThrows(ConcurrentModificationException.class, vals::next);
        assertThrows(ConcurrentModificationException.class, ents::next);
    }

    @Test
    @DisplayName("map: put of a new key without rehash throws CME")
    void mapPutNewKeyNoRehash() {
        LinearScanHashMap<Integer, Integer> m = new LinearScanHashMap<>(64);
        m.put(1, 1);
        m.put(2, 2);
        Iterator<Integer> it = m.keySet().iterator();
        it.next();
        m.put(3, 3);
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    @DisplayName("map: updating an existing key is not structural and never rehashes")
    void mapUpdateExistingKeyAtThreshold() {
        LinearScanHashMap<Integer, Integer> m = map(2);
        while (m.size() < m.capacity() * 0.667) {
            m.put(m.size() + 1000, 0);
        }
        int cap = m.capacity();
        Iterator<Integer> it = m.keySet().iterator();
        it.next();
        m.put(0, 42);
        assertEquals(cap, m.capacity());
        assertEquals(42, m.get(0));
        assertDoesNotThrow(() -> { while (it.hasNext()) it.next(); });
    }

    @Test
    @DisplayName("map: remove / clear during iteration throw CME")
    void mapRemoveAndClear() {
        LinearScanHashMap<Integer, Integer> m = map(5);
        Iterator<Integer> it = m.keySet().iterator();
        it.next();
        m.remove(3);
        assertThrows(ConcurrentModificationException.class, it::next);

        Iterator<Integer> it2 = m.keySet().iterator();
        it2.next();
        m.clear();
        assertThrows(ConcurrentModificationException.class, it2::next);
    }

    @Test
    @DisplayName("map: iterator.remove stays valid, other iterators are invalidated")
    void mapIteratorRemove() {
        LinearScanHashMap<Integer, Integer> m = map(6);
        Iterator<Integer> a = m.keySet().iterator();
        Iterator<Integer> b = m.keySet().iterator();
        a.next();
        a.remove();
        assertDoesNotThrow(() -> { while (a.hasNext()) a.next(); });
        assertThrows(ConcurrentModificationException.class, b::next);
        assertEquals(5, m.size());
    }

    @Test
    @DisplayName("map: iterator.remove after external modification throws CME")
    void mapIteratorRemoveAfterExternalChange() {
        LinearScanHashMap<Integer, Integer> m = map(3);
        Iterator<Integer> it = m.keySet().iterator();
        it.next();
        m.put(99, 99);
        assertThrows(ConcurrentModificationException.class, it::remove);
    }

    @Test
    @DisplayName("map: removeIf via views and full iteration with remove work")
    void mapRemoveIfStillWorks() {
        LinearScanHashMap<Integer, Integer> m = map(20);
        m.values().removeIf(v -> v % 2 == 0);
        assertEquals(10, m.size());
        m.entrySet().removeIf(e -> e.getKey() > 10);
        assertEquals(5, m.size());
    }

    @Test
    @DisplayName("set: add of a new element (rehash) during iteration throws CME")
    void setAddNewWithRehash() {
        LinearScanHashSet<Integer> s = set(2);
        int cap = s.capacity();
        Iterator<Integer> it = s.iterator();
        it.next();
        for (int i = 100; s.capacity() == cap; i++) {
            s.add(i);
        }
        assertThrows(ConcurrentModificationException.class, it::next);
    }

    @Test
    @DisplayName("set: add of an existing element is a no-op even at the growth threshold")
    void setAddExistingAtThreshold() {
        LinearScanHashSet<Integer> s = set(2);
        while (s.size() < s.capacity() * 0.667) {
            s.add(s.size() + 1000);
        }
        int cap = s.capacity();
        Iterator<Integer> it = s.iterator();
        it.next();
        assertFalse(s.add(0));
        assertEquals(cap, s.capacity());
        assertDoesNotThrow(() -> { while (it.hasNext()) it.next(); });
    }

    @Test
    @DisplayName("set: remove / clear / retainAll during iteration throw CME")
    void setRemoveClear() {
        LinearScanHashSet<Integer> s = set(5);
        Iterator<Integer> it = s.iterator();
        it.next();
        s.remove(3);
        assertThrows(ConcurrentModificationException.class, it::next);

        Iterator<Integer> it2 = s.iterator();
        it2.next();
        s.clear();
        assertThrows(ConcurrentModificationException.class, it2::next);
    }

    @Test
    @DisplayName("set: iterator.remove stays valid")
    void setIteratorRemoveStillValid() {
        LinearScanHashSet<Integer> s = set(8);
        Iterator<Integer> it = s.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) {
                it.remove();
            }
        }
        assertEquals(Set.of(1, 3, 5, 7), s);
    }

    @Test
    @DisplayName("set: retainAll remains correct")
    void setRetainAll() {
        LinearScanHashSet<Integer> s = set(8);
        assertTrue(s.retainAll(List.of(1, 2, 3)));
        assertEquals(Set.of(1, 2, 3), s);
    }
}
