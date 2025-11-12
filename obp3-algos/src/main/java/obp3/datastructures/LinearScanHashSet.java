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

import java.util.Arrays;

/**
 * A hash set implementation using linear probing for collision resolution.
 * <p>
 * This implementation supports null elements by using a special sentinel object
 * to mark empty slots in the internal array. This allows distinguishing between
 * a stored null value and an empty array position.
 * </p>
 * <p>
 * Invariants:
 * ∀ x ∈ α, known = ∅ →             add(known, x) = true ∧ x ∈ known'
 * ∀ x ∈ α, known ≠ ∅ → x ∉ known → add(known, x) = true ∧ x ∈ known'
 * ∀ x ∈ α, known ≠ ∅ → x ∈ known → add(known, x) = false
 * </p>
 *
 * @param <K> the type of elements maintained by this set
 */
public class LinearScanHashSet<K> {
    /**
     * Sentinel singleton representing an empty slot in the internal array.
     * This allows storing null values while distinguishing them from empty positions.
     * Uses a dedicated class with a custom toString() for clearer debugging output.
     */
    private static final Object EMPTY = new EmptySentinel();
    
    /**
     * Internal singleton class used as a sentinel to mark empty slots.
     * Provides a readable toString() representation for debugging.
     */
    private static final class EmptySentinel {
        private EmptySentinel() {
            // Private constructor - only one instance needed
        }
        
        @Override
        public String toString() {
            return "<empty>";
        }
    }
    
    private int capacity;
    private final Hashable<K> hashable;
    private final double maxLoadFactor;
    private final int growthFactor;
    private Object[] items;
    private int size;

    /**
     * Constructs a new LinearScanHashSet with the specified capacity and hashable instance.
     *
     * @param capacity the initial capacity of the hash set
     * @param hashable the hashable instance for computing hash codes and equality
     */
    public LinearScanHashSet(int capacity, Hashable<K> hashable) {
        this.capacity = capacity;
        this.hashable = hashable.withPositiveHash();
        this.maxLoadFactor = 0.667;
        this.growthFactor = 2;
        this.items = new Object[capacity];
        // Initialize all slots as EMPTY
        for (int i = 0; i < capacity; i++) {
            items[i] = EMPTY;
        }
        this.size = 0;
    }

    /**
     * Constructs a new LinearScanHashSet with default hash and equality functions.
     *
     * @param capacity the initial capacity of the hash set
     */
    public LinearScanHashSet(int capacity) {
        this(capacity, Hashable.<K>standard());
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set
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
     * Adds the specified element to this set if it is not already present.
     *
     * @param element the element to be added
     * @return true if the element was added, false if the element was already present
     */
    @SuppressWarnings("unchecked")
    public boolean add(K element) {
        // Grow the table if the load factor was reached
        if (size >= (capacity * maxLoadFactor)) {
            grow();
        }

        int theHash = hashable.hash(element);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == EMPTY) {
            items[theIndex] = element;
            size++;
            return true;
        }

        // Check if the element is already present at index
        if (hashable.equal(element, (K) items[theIndex])) {
            return false;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
        } while (
            items[theIndex] != EMPTY
            && !hashable.equal(element, (K) items[theIndex])
            && theIndex != start
        );

        if (theIndex == start) {
            // Normally we cannot reach this, since we are trying to grow
            throw new IllegalStateException("The hashset is full");
        }

        // Check for empty slot at index
        if (items[theIndex] == EMPTY) {
            items[theIndex] = element;
            size++;
            return true;
        }

        // The element is already present
        return false;
    }

    /**
     * Adds the specified element if it is not already present.
     * Alias for add method.
     *
     * @param element the element to be added
     * @return true if the element was added, false if the element was already present
     */
    public boolean addIfAbsent(K element) {
        return add(element);
    }

    /**
     * Returns true if this set contains the specified element.
     *
     * @param element the element whose presence is to be tested
     * @return true if this set contains the specified element
     */
    @SuppressWarnings("unchecked")
    public boolean contains(K element) {
        int theHash = hashable.hash(element);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == EMPTY) {
            return false;
        }

        // Check if the element is already present at index
        if (hashable.equal(element, (K) items[theIndex])) {
            return true;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
        } while (
            items[theIndex] != EMPTY
            && !hashable.equal(element, (K) items[theIndex])
            && theIndex != start
        );

        // Scanned the table, the element is not in
        return theIndex != start && items[theIndex] != EMPTY;
    }

    /**
     * Returns the element equal to the specified element if present in the set.
     *
     * @param element the element to search for
     * @return the element if found, or null if not found
     */
    @SuppressWarnings("unchecked")
    public K get(K element) {
        int theHash = hashable.hash(element);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == EMPTY) {
            return null;
        }

        // Check if the element is already present at index
        if (hashable.equal(element, (K) items[theIndex])) {
            return (K) items[theIndex];
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
        } while (
            items[theIndex] != EMPTY
            && !hashable.equal(element, (K) items[theIndex])
            && theIndex != start
        );

        // Scanned the table, the element is not in
        if (theIndex == start || items[theIndex] == EMPTY) {
            return null;
        }
        return (K) items[theIndex];
    }

    /**
     * Grows the capacity of the hash set and rehashes all elements.
     */
    @SuppressWarnings("unchecked")
    private void grow() {
        int newCapacity = capacity * growthFactor;
        Object[] newArray = new Object[newCapacity];
        
        // Initialize new array with EMPTY sentinels
        Arrays.fill(newArray, EMPTY);
        
        // Copy and rehash the elements
        for (int i = 0; i < capacity; i++) {
            if (items[i] == EMPTY) continue;
            K element = (K) items[i];
            internalAddRehash(newArray, element, hashable.hash(element));
        }
        
        capacity = newCapacity;
        items = newArray;
    }

    /**
     * Internal method to add an element during rehashing.
     * Does not check for duplicates, does not change the size.
     *
     * @param array   the array to add to
     * @param element the element to add
     * @param hash    the hash code of the element
     */
    private void internalAddRehash(Object[] array, K element, int hash) {
        int theCapacity = array.length;
        int theIndex = hash % theCapacity;
        
        // Check for empty slot at index
        if (array[theIndex] == EMPTY) {
            array[theIndex] = element;
            return;
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % theCapacity;
        } while (
            array[theIndex] != EMPTY
            && theIndex != start
        );

        if (theIndex == start) {
            // Normally we cannot reach this, since we are trying to grow
            throw new IllegalStateException("The hashset is full");
        }

        // Check for empty slot at index
        if (array[theIndex] == EMPTY) {
            array[theIndex] = element;
        }
    }
}
