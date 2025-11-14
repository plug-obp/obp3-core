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

import obp3.hashcons.Hashable;

import java.util.*;

/**
 * A hash set implementation using linear probing for collision resolution.
 * <p>
 * This implementation supports null elements by using special sentinel objects
 * to mark empty slots and deleted slots (tombstones) in the internal array.
 * This allows distinguishing between stored null values, empty positions, and
 * deleted positions.
 * </p>
 * <p>
 * <b>Set Invariants:</b>
 * </p>
 * <p>
 * <b>Size and emptiness:</b><br>
 * • |S| ≥ 0<br>
 * • isEmpty(S) ⟺ |S| = 0<br>
 * • |S| ≤ capacity(S)
 * </p>
 * <p>
 * <b>Add operations:</b><br>
 * • ∀x: S = ∅ → add(S, x) = true ∧ x ∈ S' ∧ |S'| = 1<br>
 * • ∀x: x ∉ S → add(S, x) = true ∧ x ∈ S' ∧ |S'| = |S| + 1<br>
 * • ∀x: x ∈ S → add(S, x) = false ∧ S' = S ∧ |S'| = |S|<br>
 * • ∀x: addIfAbsent(S, x) ⟺ add(S, x)
 * </p>
 * <p>
 * <b>Contains and membership:</b><br>
 * • ∀x: contains(S, x) ⟺ x ∈ S<br>
 * • ∀x: x ∈ S → get(S, x) = x<br>
 * • ∀x: x ∉ S → get(S, x) = null
 * </p>
 * <p>
 * <b>Remove operations:</b><br>
 * • ∀x: x ∈ S → remove(S, x) = true ∧ x ∉ S' ∧ |S'| = |S| - 1<br>
 * • ∀x: x ∉ S → remove(S, x) = false ∧ S' = S ∧ |S'| = |S|<br>
 * • clear(S) → S' = ∅ ∧ |S'| = 0
 * </p>
 * <p>
 * <b>Bulk operations:</b><br>
 * • addAll(S, C) → ∀x ∈ C: x ∈ S'<br>
 * • addAll(S, C) = true ⟺ ∃x ∈ C: x ∉ S<br>
 * • removeAll(S, C) → ∀x ∈ C: x ∉ S'<br>
 * • removeAll(S, C) = true ⟺ ∃x ∈ (S ∩ C)<br>
 * • retainAll(S, C) → S' = S ∩ C<br>
 * • retainAll(S, C) = true ⟺ ∃x ∈ S: x ∉ C<br>
 * • containsAll(S, C) ⟺ C ⊆ S ⟺ ∀x ∈ C: x ∈ S
 * </p>
 * <p>
 * <b>Iterator properties:</b><br>
 * • iterator(S) generates each element in S exactly once<br>
 * • ∀x yielded by iterator: x ∈ S<br>
 * • |{x | x yielded by iterator}| = |S|<br>
 * • iterator.hasNext() = false ⟺ all elements have been yielded
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
 * • null is a valid element<br>
 * • ∀operations: null is treated as any other element value
 * </p>
 *
 * @param <K> the type of elements maintained by this set
 */
public class LinearScanHashSet<K> extends AbstractLinearProbingTable<K> implements Set<K> {

    /**
     * Constructs a new LinearScanHashSet with the specified capacity and hashable instance.
     *
     * @param capacity the initial capacity of the hash set
     * @param hashable the hashable instance for computing hash codes and equality
     */
    public LinearScanHashSet(int capacity, Hashable<K> hashable) {
        super(capacity, hashable, 0.667, 2);
    }

    /**
     * Constructs a new LinearScanHashSet with default hash and equality functions.
     *
     * @param capacity the initial capacity of the hash set
     */
    public LinearScanHashSet(int capacity) {
        this(capacity, Hashable.<K>standard());
    }

    @Override
    protected K extractKey(Object item) {
        // For a Set, the item itself is the key
        @SuppressWarnings("unchecked")
        K key = (K) item;
        return key;
    }

    /**
     * Adds the specified element to this set if it is not already present.
     *
     * @param element the element to be added
     * @return true if the element was added, false if the element was already present
     */
    @Override
    public boolean add(K element) {
        checkAndGrow();
        
        InsertionSlot slot = findInsertionSlot(element);
        
        if (slot.exists) {
            // Element already present
            return false;
        }
        
        // Insert new element
        items[slot.index] = element;
        size++;
        return true;
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
     * @param o the element whose presence is to be tested
     * @return true if this set contains the specified element
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        K element = (K) o;
        SearchResult result = findSlot(element);
        return result.found;
    }

    /**
     * Returns the element equal to the specified element if present in the set.
     *
     * @param element the element to search for
     * @return the element if found, or null if not found
     */
    @SuppressWarnings("unchecked")
    public K get(K element) {
        SearchResult result = findSlot(element);
        if (result.found) {
            return (K) items[result.index];
        }
        return null;
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o the element to be removed
     * @return true if the element was removed, false if the element was not present
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        K element = (K) o;
        SearchResult result = findSlot(element);
        
        if (result.found) {
            removeAtIndex(result.index);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<K> iterator() {
        return new Iterator<K>() {
            private int currentIndex = 0;
            private int itemsReturned = 0;

            @Override
            public boolean hasNext() {
                return itemsReturned < size;
            }

            @Override
            @SuppressWarnings("unchecked")
            public K next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                
                // Find next non-empty, non-deleted item
                while (currentIndex < capacity && !isActiveSlot(currentIndex)) {
                    currentIndex++;
                }
                
                if (currentIndex >= capacity) {
                    throw new NoSuchElementException();
                }
                
                K item = (K) items[currentIndex];
                currentIndex++;
                itemsReturned++;
                return item;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int index = 0;
        for (int i = 0; i < capacity && index < size; i++) {
            if (isActiveSlot(i)) {
                result[index++] = items[i];
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
        }
        System.arraycopy(toArray(), 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends K> c) {
        boolean modified = false;
        for (K e : c) {
            if (add(e)) {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        for (int i = 0; i < capacity; i++) {
            if (isActiveSlot(i)) {
                if (!c.contains(items[i])) {
                    removeAtIndex(i);
                    modified = true;
                }
            }
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (remove(e)) {
                modified = true;
            }
        }
        return modified;
    }
}
