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

package obp3.datastructures;

import obp3.hashcons.Hashable;

/**
 * A hash map implementation using linear probing for collision resolution.
 * <p>
 * Map semantics:
 * - put(key, value) returns null if the key is new, or the old value if the key already exists
 * - get(key) returns the value associated with the key, or null if not found
 * - containsKey(key) returns true if the key is present
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class LinearScanHashMap<K, V> {
    private int capacity;
    private final Hashable<K> hashable;
    private final double maxLoadFactor;
    private final int growthFactor;
    private MapEntry<K, V>[] items;
    private int size;

    /**
     * Constructs a new LinearScanHashMap with the specified capacity and hashable instance.
     *
     * @param capacity the initial capacity of the hash map
     * @param hashable the hashable instance for computing hash codes and equality
     */
    @SuppressWarnings("unchecked")
    public LinearScanHashMap(int capacity, Hashable<K> hashable) {
        this.capacity = capacity;
        this.hashable = hashable.withPositiveHash();
        this.maxLoadFactor = 0.667;
        this.growthFactor = 2;
        this.items = (MapEntry<K, V>[]) new MapEntry[capacity];
        this.size = 0;
    }

    /**
     * Constructs a new LinearScanHashMap with default hash and equality functions.
     *
     * @param capacity the initial capacity of the hash map
     */
    public LinearScanHashMap(int capacity) {
        this(capacity, Hashable.<K>standard());
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns the capacity of the underlying array.
     *
     * @return the capacity
     */
    public int capacity() {
        return capacity;
    }

    @Override
    public String toString() {
        StringBuilder repr = new StringBuilder("capacity=" + capacity + ", size=" + size + " items=[");
        for (int i = 0; i < capacity; i++) {
            if (i != 0) {
                repr.append(", ");
            }
            repr.append(items[i]);
        }
        repr.append("]");
        return repr.toString();
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is replaced.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key
     */
    public V put(K key, V value) {
        // Grow the table if the load factor was reached
        if (size >= (capacity * maxLoadFactor)) {
            grow();
        }

        int theHash = hashable.hash(key);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == null) {
            items[theIndex] = new MapEntry<>(key, value);
            size++;
            return null;
        }

        // Check if the key is already present at index
        if (hashable.equal(key, items[theIndex].key)) {
            V oldValue = items[theIndex].value;
            items[theIndex].value = value;
            return oldValue;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
        } while (
            items[theIndex] != null
            && !hashable.equal(key, items[theIndex].key)
            && theIndex != start
        );

        if (theIndex == start) {
            // Normally we cannot reach this, since we are trying to grow
            throw new IllegalStateException("The hashmap is full");
        }

        // Check for empty slot at index
        if (items[theIndex] == null) {
            items[theIndex] = new MapEntry<>(key, value);
            size++;
            return null;
        }

        // The key is already present, update value
        V oldValue = items[theIndex].value;
        items[theIndex].value = value;
        return oldValue;
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key the key whose presence in this map is to be tested
     * @return true if this map contains a mapping for the specified key
     */
    public boolean containsKey(K key) {
        int theHash = hashable.hash(key);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == null) {
            return false;
        }

        // Check if the key is present at index
        if (hashable.equal(key, items[theIndex].key)) {
            return true;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
        } while (
            items[theIndex] != null
            && !hashable.equal(key, items[theIndex].key)
            && theIndex != start
        );

        // Scanned the table, the key is not in
        return theIndex != start && items[theIndex] != null;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or null if this map contains no mapping for the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     */
    public V get(K key) {
        int theHash = hashable.hash(key);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == null) {
            return null;
        }

        // Check if the key is present at index
        if (hashable.equal(key, items[theIndex].key)) {
            return items[theIndex].value;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
        } while (
            items[theIndex] != null
            && !hashable.equal(key, items[theIndex].key)
            && theIndex != start
        );

        // Scanned the table, the key is not in
        if (theIndex == start || items[theIndex] == null) {
            return null;
        }
        return items[theIndex].value;
    }

    /**
     * Grows the capacity of the hash map and rehashes all entries.
     */
    @SuppressWarnings("unchecked")
    private void grow() {
        int newCapacity = capacity * growthFactor;
        MapEntry<K, V>[] newArray = (MapEntry<K, V>[]) new MapEntry[newCapacity];
        
        // Copy and rehash the elements
        for (int i = 0; i < capacity; i++) {
            MapEntry<K, V> entry = items[i];
            if (entry == null) continue;
            internalAddRehash(newArray, entry, hashable.hash(entry.key));
        }
        
        capacity = newCapacity;
        items = newArray;
    }

    /**
     * Internal method to add an entry during rehashing.
     * Does not check for duplicates, does not change the size.
     *
     * @param array the array to add to
     * @param entry the entry to add
     * @param hash  the hash code of the key
     */
    private void internalAddRehash(MapEntry<K, V>[] array, MapEntry<K, V> entry, int hash) {
        int theCapacity = array.length;
        int theIndex = hash % theCapacity;
        
        // Check for empty slot at index
        if (array[theIndex] == null) {
            array[theIndex] = entry;
            return;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % theCapacity;
        } while (
            array[theIndex] != null
            && theIndex != start
        );

        if (theIndex == start) {
            // Normally we cannot reach this, since we are trying to grow
            throw new IllegalStateException("The hashmap is full");
        }

        // Check for empty slot at index
        if (array[theIndex] == null) {
            array[theIndex] = entry;
        }
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
