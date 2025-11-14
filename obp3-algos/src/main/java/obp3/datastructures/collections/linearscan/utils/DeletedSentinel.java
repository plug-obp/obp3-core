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

package obp3.datastructures.collections.linearscan.utils;

/**
 * Sentinel singleton representing a deleted slot (tombstone) in hash table implementations.
 * <p>
 * This sentinel is used to mark slots in the internal array where an element was previously
 * stored but has since been removed. Tombstones serve several important purposes:
 * </p>
 * <ul>
 * <li><b>Preserve probing chains:</b> Linear probing continues through DELETED slots during lookups</li>
 * <li><b>Enable slot reuse:</b> DELETED slots can be reused during insertion operations</li>
 * <li><b>Maintain correctness:</b> Prevents breaking probe sequences when elements are removed</li>
 * </ul>
 * <p>
 * Linear probing behavior: When searching for a key, the search <b>continues through</b> DELETED slots,
 * only stopping at an EMPTY slot. When inserting, DELETED slots can be reused to minimize wasted space.
 * </p>
 * <p>
 * Tombstones are eliminated during rehashing/growth operations, which helps maintain performance
 * by preventing excessive accumulation of deleted markers.
 * </p>
 * <p>
 * This is a singleton class - only one instance should be used throughout the application.
 * </p>
 */
public final class DeletedSentinel {
    /**
     * The singleton instance of the deleted sentinel.
     */
    public static final DeletedSentinel INSTANCE = new DeletedSentinel();
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private DeletedSentinel() {
        // Private constructor - only one instance needed
    }
    
    @Override
    public String toString() {
        return "<deleted>";
    }
}
