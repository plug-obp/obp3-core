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
 * Sentinel singleton representing an empty slot in hash table implementations.
 * <p>
 * This sentinel is used to mark slots in the internal array that have never been used.
 * It allows distinguishing between:
 * </p>
 * <ul>
 * <li>Empty slots (EMPTY sentinel)</li>
 * <li>Deleted slots (DELETED sentinel)</li>
 * <li>Slots containing actual values (including null)</li>
 * </ul>
 * <p>
 * Linear probing behavior: When searching for a key, the search <b>stops</b> at an EMPTY slot,
 * as it indicates the key cannot exist further in the probe sequence.
 * </p>
 * <p>
 * This is a singleton class - only one instance should be used throughout the application.
 * </p>
 */
public final class EmptySentinel {
    /**
     * The singleton instance of the empty sentinel.
     */
    public static final EmptySentinel INSTANCE = new EmptySentinel();
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private EmptySentinel() {
        // Private constructor - only one instance needed
    }
    
    @Override
    public String toString() {
        return "<empty>";
    }
}
