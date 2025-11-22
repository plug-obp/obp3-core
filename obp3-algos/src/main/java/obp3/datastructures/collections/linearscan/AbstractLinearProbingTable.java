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

import obp3.datastructures.collections.linearscan.utils.DeletedSentinel;
import obp3.datastructures.collections.linearscan.utils.EmptySentinel;
import obp3.utils.Hashable;

import java.util.Arrays;

/**
 * Abstract base class for hash table implementations using linear probing collision resolution.
 * <p>
 * This class encapsulates the core linear probing algorithm with tombstone-based deletion,
 * allowing subclasses to implement different collection types (Set, Map, etc.) while sharing
 * the fundamental probing logic.
 * </p>
 * <p>
 * <b>Key features:</b>
 * </p>
 * <ul>
 * <li>Linear probing for collision resolution</li>
 * <li>Tombstone-based deletion (DELETED sentinel marks removed slots)</li>
 * <li>Automatic growth when load factor is exceeded (default 0.667)</li>
 * <li>Tombstone reuse during insertion to minimize wasted space</li>
 * <li>Support for null keys via sentinel objects</li>
 * </ul>
 * <p>
 * <b>Linear probing invariants:</b>
 * </p>
 * <ul>
 * <li>Search continues through DELETED slots, stops at EMPTY slots</li>
 * <li>Insertion prefers first DELETED slot encountered (tombstone reuse)</li>
 * <li>Growth/rehashing eliminates all tombstones</li>
 * <li>Load factor = size / capacity ≤ maxLoadFactor</li>
 * </ul>
 * <p>
 * <b>Subclass responsibilities:</b>
 * </p>
 * <ul>
 * <li>Implement {@link #extractKey(Object)} to get the key from stored items</li>
 * <li>Implement {@link #keysEqual(Object, Object)} for key equality testing</li>
 * <li>Provide constructors that initialize the base class fields</li>
 * <li>Implement collection-specific operations (add, get, remove, etc.)</li>
 * </ul>
 *
 * @param <K> the type of keys used for hashing and lookup
 */
public abstract class AbstractLinearProbingTable<K> {
    /**
     * Sentinel singleton representing an empty slot in the internal array.
     * Empty slots indicate positions that have never been used.
     */
    protected static final Object EMPTY = EmptySentinel.INSTANCE;
    
    /**
     * Sentinel singleton representing a deleted slot (tombstone) in the internal array.
     * Deleted slots preserve probing chains and can be reused during insertion.
     */
    protected static final Object DELETED = DeletedSentinel.INSTANCE;
    
    /**
     * Current capacity of the hash table.
     */
    protected int capacity;
    
    /**
     * Hashable instance for computing hash codes and testing key equality.
     */
    protected final Hashable<K> hashable;
    
    /**
     * Maximum load factor before triggering growth.
     * When size / capacity exceeds this threshold, the table grows.
     */
    protected final double maxLoadFactor;
    
    /**
     * Growth factor for capacity expansion.
     * New capacity = old capacity * growthFactor.
     */
    protected final int growthFactor;
    
    /**
     * Internal array storing items (elements or entries) and sentinels.
     */
    protected Object[] items;
    
    /**
     * Number of active items in the table (excludes EMPTY and DELETED).
     */
    protected int size;

    /**
     * Constructs a new AbstractLinearProbingTable with the specified parameters.
     *
     * @param capacity      the initial capacity of the hash table
     * @param hashable      the hashable instance for computing hash codes and equality
     * @param maxLoadFactor the maximum load factor before growth (typically 0.5-0.75)
     * @param growthFactor  the factor by which capacity increases during growth (typically 2)
     */
    protected AbstractLinearProbingTable(int capacity, Hashable<K> hashable, double maxLoadFactor, int growthFactor) {
        this.capacity = capacity;
        this.hashable = hashable.withPositiveHash();
        this.maxLoadFactor = maxLoadFactor;
        this.growthFactor = growthFactor;
        this.items = new Object[capacity];
        Arrays.fill(items, EMPTY);
        this.size = 0;
    }

    /**
     * Extracts the key from a stored item.
     * <p>
     * For a Set, this returns the element itself.
     * For a Map, this extracts the key from a key-value entry.
     * </p>
     *
     * @param item the stored item (not EMPTY or DELETED)
     * @return the key associated with this item
     */
    protected abstract K extractKey(Object item);

    /**
     * Tests whether two keys are equal using the appropriate equality function.
     * <p>
     * This delegates to the hashable's equality function, which may be custom
     * or the standard Object.equals() method.
     * </p>
     *
     * @param key1 the first key
     * @param key2 the second key
     * @return true if the keys are equal, false otherwise
     */
    protected boolean keysEqual(K key1, K key2) {
        return hashable.equal(key1, key2);
    }

    /**
     * Returns the number of active items in this table.
     *
     * @return the number of items (excludes EMPTY and DELETED slots)
     */
    public int size() {
        return size;
    }

    /**
     * Returns the current capacity of the underlying array.
     *
     * @return the capacity
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Returns true if this table contains no items.
     *
     * @return true if size is 0, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all items from this table.
     * Resets all slots to EMPTY and sets size to 0.
     */
    public void clear() {
        Arrays.fill(items, EMPTY);
        size = 0;
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
     * Result of a slot search operation.
     * Encapsulates both the index and whether the key was found.
     */
    protected static class SearchResult {
        /**
         * The index where the key was found, or -1 if not found.
         */
        public final int index;
        
        /**
         * True if the key was found, false otherwise.
         */
        public final boolean found;

        public SearchResult(int index, boolean found) {
            this.index = index;
            this.found = found;
        }

        /**
         * Creates a SearchResult indicating the key was not found.
         */
        public static SearchResult notFound() {
            return new SearchResult(-1, false);
        }

        /**
         * Creates a SearchResult indicating the key was found at the given index.
         */
        public static SearchResult found(int index) {
            return new SearchResult(index, true);
        }
    }

    /**
     * Result of an insertion slot search operation.
     * Includes information about the slot to use and whether a tombstone was found.
     */
    protected static class InsertionSlot {
        /**
         * The index where insertion should occur.
         */
        public final int index;
        
        /**
         * True if the key already exists at this index, false if this is a new insertion.
         */
        public final boolean exists;
        
        /**
         * True if a tombstone (DELETED) was found and is being reused.
         */
        public final boolean reusedTombstone;

        public InsertionSlot(int index, boolean exists, boolean reusedTombstone) {
            this.index = index;
            this.exists = exists;
            this.reusedTombstone = reusedTombstone;
        }
    }

    /**
     * Finds the slot containing the specified key using linear probing.
     * <p>
     * This method searches for an existing key in the table, skipping over
     * DELETED slots and stopping at EMPTY slots (which indicate the key
     * cannot exist further in the probe sequence).
     * </p>
     *
     * @param key the key to search for
     * @return a SearchResult containing the index if found, or notFound() if not present
     */
    protected SearchResult findSlot(K key) {
        int theHash = hashable.hash(key);
        int theIndex = theHash % capacity;

        // Check for empty slot at index
        if (items[theIndex] == EMPTY) {
            return SearchResult.notFound();
        }

        // Check for deleted slot - skip it
        if (items[theIndex] != DELETED) {
            // Check if the key is present at index
            K itemKey = extractKey(items[theIndex]);
            if (keysEqual(key, itemKey)) {
                return SearchResult.found(theIndex);
            }
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
            
            if (items[theIndex] == EMPTY) {
                return SearchResult.notFound();
            }
            
            if (items[theIndex] != DELETED) {
                K itemKey = extractKey(items[theIndex]);
                if (keysEqual(key, itemKey)) {
                    return SearchResult.found(theIndex);
                }
            }
        } while (theIndex != start);

        // Scanned the entire table, key not found
        return SearchResult.notFound();
    }

    /**
     * Finds the best slot for inserting a key, considering tombstone reuse.
     * <p>
     * This method implements the insertion strategy:
     * </p>
     * <ul>
     * <li>If key already exists, returns that slot (for update)</li>
     * <li>If EMPTY slot found, returns it (or first DELETED if found earlier)</li>
     * <li>Tracks first DELETED slot encountered for tombstone reuse</li>
     * <li>Prefers reusing tombstones to minimize wasted space</li>
     * </ul>
     *
     * @param key the key to insert
     * @return an InsertionSlot describing where and how to insert
     */
    protected InsertionSlot findInsertionSlot(K key) {
        int theHash = hashable.hash(key);
        int theIndex = theHash % capacity;
        int firstDeletedIndex = -1;

        // Check for empty slot at index
        if (items[theIndex] == EMPTY) {
            return new InsertionSlot(theIndex, false, false);
        }

        // Check for deleted slot - remember it for potential insertion
        if (items[theIndex] == DELETED) {
            firstDeletedIndex = theIndex;
        } else {
            K itemKey = extractKey(items[theIndex]);
            // Check if the key already exists at index
            if (keysEqual(key, itemKey)) {
                return new InsertionSlot(theIndex, true, false);
            }
        }

        // Not found, start linear probing
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % capacity;
            
            if (items[theIndex] == EMPTY) {
                // Found empty slot - use tombstone if we found one earlier
                if (firstDeletedIndex != -1) {
                    return new InsertionSlot(firstDeletedIndex, false, true);
                } else {
                    return new InsertionSlot(theIndex, false, false);
                }
            }
            
            if (items[theIndex] == DELETED) {
                // Remember first deleted slot for reuse
                if (firstDeletedIndex == -1) {
                    firstDeletedIndex = theIndex;
                }
            } else {
                K itemKey = extractKey(items[theIndex]);
                // Check if the key already exists
                if (keysEqual(key, itemKey)) {
                    return new InsertionSlot(theIndex, true, false);
                }
            }
        } while (theIndex != start);

        // If we've scanned the entire table and found a deleted slot, use it
        if (firstDeletedIndex != -1) {
            return new InsertionSlot(firstDeletedIndex, false, true);
        }

        // Table is full (no EMPTY or DELETED slots found)
        throw new IllegalStateException("The hash table is full");
    }

    /**
     * Checks if the table needs to grow and triggers growth if necessary.
     * Growth occurs when size exceeds capacity * maxLoadFactor.
     */
    protected void checkAndGrow() {
        if (size >= (capacity * maxLoadFactor)) {
            grow();
        }
    }

    /**
     * Grows the capacity of the hash table and rehashes all active items.
     * <p>
     * This method:
     * </p>
     * <ul>
     * <li>Creates a new array with capacity * growthFactor</li>
     * <li>Rehashes all active items (skips EMPTY and DELETED)</li>
     * <li>Eliminates all tombstones in the process</li>
     * <li>Updates the capacity and items reference</li>
     * </ul>
     */
    protected void grow() {
        int newCapacity = capacity * growthFactor;
        Object[] newArray = new Object[newCapacity];
        
        // Initialize new array with EMPTY sentinels
        Arrays.fill(newArray, EMPTY);
        
        // Copy and rehash only active items (skip EMPTY and DELETED)
        for (int i = 0; i < capacity; i++) {
            if (items[i] != EMPTY && items[i] != DELETED) {
                Object item = items[i];
                K key = extractKey(item);
                internalRehashInsert(newArray, newCapacity, item, hashable.hash(key));
            }
        }
        
        capacity = newCapacity;
        items = newArray;
    }

    /**
     * Internal method to insert an item during rehashing.
     * <p>
     * This is a simplified insertion that:
     * </p>
     * <ul>
     * <li>Does not check for duplicates (keys are already unique)</li>
     * <li>Does not update size (size remains unchanged during rehash)</li>
     * <li>Only probes for EMPTY slots (no DELETED in new array)</li>
     * </ul>
     *
     * @param array       the new array to insert into
     * @param arrayLength the length of the new array
     * @param item        the item to insert
     * @param hash        the hash code of the item's key
     */
    protected void internalRehashInsert(Object[] array, int arrayLength, Object item, int hash) {
        int theIndex = hash % arrayLength;
        
        // Check for empty slot at index
        if (array[theIndex] == EMPTY) {
            array[theIndex] = item;
            return;
        }

        // Linear probe for next empty slot
        int start = theIndex;
        do {
            theIndex = (theIndex + 1) % arrayLength;
        } while (array[theIndex] != EMPTY && theIndex != start);

        if (theIndex == start) {
            // Should never happen during growth since we size appropriately
            throw new IllegalStateException("The hash table is full during rehashing");
        }

        // Insert at empty slot
        array[theIndex] = item;
    }

    /**
     * Removes the item at the specified index by marking it as DELETED.
     * <p>
     * This method implements tombstone-based deletion:
     * </p>
     * <ul>
     * <li>Replaces the item with DELETED sentinel</li>
     * <li>Decrements size</li>
     * <li>Preserves probing chains for other items</li>
     * </ul>
     *
     * @param index the index of the item to remove
     */
    protected void removeAtIndex(int index) {
        items[index] = DELETED;
        size--;
    }

    /**
     * Checks if the specified index contains an active item (not EMPTY or DELETED).
     *
     * @param index the index to check
     * @return true if the slot contains an active item, false otherwise
     */
    protected boolean isActiveSlot(int index) {
        return items[index] != EMPTY && items[index] != DELETED;
    }
}
