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

    @Override
    @SuppressWarnings("unchecked")
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (int i = 0; i < capacity; i++) {
            if (isActiveSlot(i)) {
                MapEntry<K, V> entry = (MapEntry<K, V>) items[i];
                keys.add(entry.key);
            }
        }
        return keys;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<V> values() {
        List<V> vals = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            if (isActiveSlot(i)) {
                MapEntry<K, V> entry = (MapEntry<K, V>) items[i];
                vals.add(entry.value);
            }
        }
        return vals;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entries = new HashSet<>();
        for (int i = 0; i < capacity; i++) {
            if (isActiveSlot(i)) {
                MapEntry<K, V> entry = (MapEntry<K, V>) items[i];
                entries.add(new AbstractMap.SimpleEntry<>(entry.key, entry.value));
            }
        }
        return entries;
    }

    /**
     * Internal class to store key-value pairs.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    private static class MapEntry<K, V> {
        K key;
        V value;

        MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "{key: " + key + ", value: " + value + "}";
        }
    }
}
