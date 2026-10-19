// MIT License

// Copyright (c) 2022 Ciprian Teodorov

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package obp3.datastructures.collections.linearscan;

import obp3.utils.Hashable;

import java.util.*;

/**
 * A hash map implementation using linear probing for collision resolution.
 * <p>
 * This implementation supports null keys and null values by using special sentinel objects
 * to mark empty slots and deleted slots (tombstones) in the internal array.
 * </p>
 * <p>
 * <b>Map Invariants:</b>
 * </p>
 * <p>
 * <b>Size and emptiness:</b><br>
 * • |M| ≥ 0<br>
 * • isEmpty(M) ⟺ |M| = 0<br>
 * • |M| ≤ capacity(M)
 * </p>
 * <p>
 * <b>Put operations:</b><br>
 * • ∀k,v: k ∉ M → put(M, k, v) = null ∧ (k → v) ∈ M' ∧ |M'| = |M| + 1<br>
 * • ∀k,v,v': k → v' ∈ M → put(M, k, v) = v' ∧ (k → v) ∈ M' ∧ |M'| = |M|<br>
 * • ∀k,v: put(M, k, v) → get(M', k) = v
 * </p>
 * <p>
 * <b>Get and containsKey:</b><br>
 * • ∀k: k → v ∈ M → get(M, k) = v<br>
 * • ∀k: k ∉ M → get(M, k) = null<br>
 * • ∀k: containsKey(M, k) ⟺ ∃v: k → v ∈ M<br>
 * • ∀v: containsValue(M, v) ⟺ ∃k: k → v ∈ M
 * </p>
 * <p>
 * <b>Remove operations:</b><br>
 * • ∀k: k → v ∈ M → remove(M, k) = v ∧ k ∉ M' ∧ |M'| = |M| - 1<br>
 * • ∀k: k ∉ M → remove(M, k) = null ∧ M' = M ∧ |M'| = |M|<br>
 * • clear(M) → M' = ∅ ∧ |M'| = 0
 * </p>
 * <p>
 * <b>Bulk operations:</b><br>
 * • putAll(M, M2) → ∀(k → v) ∈ M2: get(M', k) = v<br>
 * • keySet(M) = {k | ∃v: k → v ∈ M}<br>
 * • values(M) = [v | ∃k: k → v ∈ M] (multiset)<br>
 * • entrySet(M) = {(k, v) | k → v ∈ M}
 * </p>
 * <p>
 * <b>Tombstone invariants:</b><br>
 * • Deleted slots (tombstones) preserve linear probing chains<br>
 * • Tombstones can be reused during insertion<br>
 * • Tombstones are eliminated during growth/rehashing<br>
 * • Linear probing continues through DELETED, stops at EMPTY
 * </p>
 * <p>
 * <b>Null handling:</b><br>
 * • null is a valid key<br>
 * • null is a valid value<br>
 * • ∀operations: null keys and values are treated as any other key/value
 * </p>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class LinearScanHashMap<K, V> extends AbstractLinearProbingTable<K> implements Map<K, V> {

    /**
     * Constructs a new LinearScanHashMap with the specified capacity and hashable instance.
     *
     * @param capacity the initial capacity of the hash map
     * @param hashable the hashable instance for computing hash codes and equality
     */
    public LinearScanHashMap(int capacity, Hashable<K> hashable) {
        super(capacity, hashable, 0.667, 2);
    }

    /**
     * Constructs a new LinearScanHashMap with default hash and equality functions.
     *
     * @param capacity the initial capacity of the hash map
     */
    public LinearScanHashMap(int capacity) {
        this(capacity, Hashable.<K>standard());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected K extractKey(Object item) {
        // For a Map, extract the key from the MapEntry
        MapEntry<K, V> entry = (MapEntry<K, V>) item;
        return entry.key;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    @SuppressWarnings("unchecked")
    @Override
    public V put(K key, V value) {
        checkAndGrow();
        
        InsertionSlot slot = findInsertionSlot(key);
        
        if (slot.exists) {
            // Key already exists - update value
            MapEntry<K, V> entry = (MapEntry<K, V>) items[slot.index];
            V oldValue = entry.value;
            entry.value = value;
            return oldValue;
        }
        
        // Insert new entry
        items[slot.index] = new MapEntry<>(key, value);
        size++;
        return null;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        K k = (K) key;
        SearchResult result = findSlot(k);
        return result.found;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or null if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        K k = (K) key;
        SearchResult result = findSlot(k);
        
        if (result.found) {
            MapEntry<K, V> entry = (MapEntry<K, V>) items[result.index];
            return entry.value;
        }
        return null;
    }

    /**
     * Removes the mapping for a key from this map if it is present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
        K k = (K) key;
        SearchResult result = findSlot(k);
        
        if (result.found) {
            MapEntry<K, V> entry = (MapEntry<K, V>) items[result.index];
            V oldValue = entry.value;
            removeAtIndex(result.index);
            return oldValue;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(items, EMPTY);
        size = 0;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsValue(Object value) {
        for (int i = 0; i < capacity; i++) {
            if (isActiveSlot(i)) {
                MapEntry<K, V> entry = (MapEntry<K, V>) items[i];
                if (Objects.equals(value, entry.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a live view of the keys. Removals through the view (including its iterator)
     * mutate this map; additions are unsupported.
     */
    @Override
    public Set<K> keySet() {
        return new KeyView();
    }

    /**
     * Returns a live view of the values. Removals through the view (including its iterator)
     * mutate this map; additions are unsupported.
     */
    @Override
    public Collection<V> values() {
        return new ValuesView();
    }

    /**
     * Returns a live view of the entries. Entries write {@code setValue} through to this map;
     * removals through the view mutate this map; additions are unsupported.
     */
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new EntryView();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map<?, ?> other) || other.size() != size) {
            return false;
        }
        try {
            for (int i = 0; i < capacity; i++) {
                if (isActiveSlot(i)) {
                    @SuppressWarnings("unchecked")
                    MapEntry<K, V> e = (MapEntry<K, V>) items[i];
                    if (e.value == null ? !(other.get(e.key) == null && other.containsKey(e.key))
                                        : !e.value.equals(other.get(e.key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException | NullPointerException ex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = 0; i < capacity; i++) {
            if (isActiveSlot(i)) {
                h += items[i].hashCode();
            }
        }
        return h;
    }

    /** Iterates over active slots and supports {@link Iterator#remove()}. */
    private abstract class SlotIterator<T> implements Iterator<T> {
        private int next = advance(0);
        private int last = -1;

        private int advance(int from) {
            while (from < capacity && !isActiveSlot(from)) {
                from++;
            }
            return from;
        }

        abstract T project(MapEntry<K, V> entry);

        @Override
        public boolean hasNext() {
            return next < capacity;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            last = next;
            next = advance(next + 1);
            return project((MapEntry<K, V>) items[last]);
        }

        @Override
        public void remove() {
            if (last < 0) {
                throw new IllegalStateException();
            }
            removeAtIndex(last);
            last = -1;
        }
    }

    private final class KeyView extends AbstractSet<K> {
        @Override
        public Iterator<K> iterator() {
            return new SlotIterator<K>() {
                @Override
                K project(MapEntry<K, V> entry) {
                    return entry.key;
                }
            };
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            if (!containsKey(o)) {
                return false;
            }
            LinearScanHashMap.this.remove(o);
            return true;
        }

        @Override
        public void clear() {
            LinearScanHashMap.this.clear();
        }
    }

    private final class ValuesView extends AbstractCollection<V> {
        @Override
        public Iterator<V> iterator() {
            return new SlotIterator<V>() {
                @Override
                V project(MapEntry<K, V> entry) {
                    return entry.value;
                }
            };
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return containsValue(o);
        }

        @Override
        public void clear() {
            LinearScanHashMap.this.clear();
        }
    }

    private final class EntryView extends AbstractSet<Map.Entry<K, V>> {
        @Override
        public Iterator<Map.Entry<K, V>> iterator() {
            return new SlotIterator<Map.Entry<K, V>>() {
                @Override
                Map.Entry<K, V> project(MapEntry<K, V> entry) {
                    return entry;
                }
            };
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) {
                return false;
            }
            SearchResult r = findSlot((K) e.getKey());
            return r.found && Objects.equals(((MapEntry<K, V>) items[r.index]).value, e.getValue());
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry<?, ?> e)) {
                return false;
            }
            SearchResult r = findSlot((K) e.getKey());
            if (r.found && Objects.equals(((MapEntry<K, V>) items[r.index]).value, e.getValue())) {
                removeAtIndex(r.index);
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            LinearScanHashMap.this.clear();
        }
    }

    /**
     * Internal class to store key-value pairs.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    private static class MapEntry<K, V> implements Map.Entry<K, V> {
        K key;
        V value;

        MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V old = value;
            value = newValue;
            return old;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Map.Entry<?, ?> e
                    && Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "{key: " + key + ", value: " + value + "}";
        }
    }
}
