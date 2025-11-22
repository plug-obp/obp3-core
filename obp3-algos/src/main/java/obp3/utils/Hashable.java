package obp3.utils;

import java.util.Objects;
import java.util.Comparator;

/**
 * A functional interface that combines hash code computation and equality testing
 * for a given type. This abstraction is useful when you need custom hashing and
 * equality semantics that differ from the standard {@link Object#hashCode()} and
 * {@link Object#equals(Object)} methods.
 * 
 * <p><b>Contract:</b> Implementations must satisfy the following invariants:
 * <ul>
 *   <li>If {@code equal(x, y)} returns {@code true}, then {@code hash(x) == hash(y)}</li>
 *   <li>{@code equal(x, y)} must be symmetric: {@code equal(x, y) == equal(y, x)}</li>
 *   <li>{@code equal(x, x)} must return {@code true} (reflexive)</li>
 *   <li>If {@code equal(x, y)} and {@code equal(y, z)}, then {@code equal(x, z)} (transitive)</li>
 *   <li>{@code hash(x)} must be consistent: multiple calls should return the same value</li>
 * </ul>
 * 
 * <p><b>Null handling:</b> The default implementation handles nulls using {@link Objects#equals}
 * and {@link Objects#hashCode}. Custom implementations should document their null behavior.
 *
 * @param <T> the type of objects that may be hashed and compared
 */
@FunctionalInterface
public interface Hashable<T> {
    
    /**
     * Computes the hash code for the given object.
     * 
     * @param x the object to hash (may be null depending on implementation)
     * @return the hash code
     */
    int hash(T x);
    
    /**
     * Tests whether two objects are equal according to this hashable's semantics.
     * 
     * @param x the first object (may be null depending on implementation)
     * @param y the second object (may be null depending on implementation)
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    default boolean equal(T x, T y) {
        return Objects.equals(x, y);
    }
    
    /**
     * Returns a hashable that uses the standard {@link Object#hashCode()} and
     * {@link Object#equals(Object)} methods, with proper null handling.
     * 
     * @param <T> the type of objects
     * @return a hashable using standard Java equality and hashing
     */
    static <T> Hashable<T> standard() {
        return new Hashable<T>() {
            @Override
            public int hash(T x) {
                return Objects.hashCode(x);
            }
            
            @Override
            public boolean equal(T x, T y) {
                return Objects.equals(x, y);
            }
            
            @Override
            public String toString() {
                return "Hashable.standard()";
            }
        };
    }
    
    /**
     * Returns a hashable that uses identity comparison ({@code ==}) for equality
     * and {@link System#identityHashCode(Object)} for hashing.
     * 
     * @param <T> the type of objects
     * @return a hashable using identity comparison
     */
    static <T> Hashable<T> identity() {
        return new Hashable<T>() {
            @Override
            public int hash(T x) {
                return System.identityHashCode(x);
            }
            
            @Override
            public boolean equal(T x, T y) {
                return x == y;
            }
            
            @Override
            public String toString() {
                return "Hashable.identity()";
            }
        };
    }
    
    /**
     * Returns a hashable based on a comparator. Two objects are considered equal
     * if the comparator returns 0 when comparing them.
     * 
     * @param <T> the type of objects
     * @param comparator the comparator to use for equality testing
     * @return a hashable based on the comparator
     */
    static <T> Hashable<T> fromComparator(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator must not be null");
        return new Hashable<T>() {
            @Override
            public int hash(T x) {
                return Objects.hashCode(x);
            }
            
            @Override
            public boolean equal(T x, T y) {
                if (x == null && y == null) return true;
                if (x == null || y == null) return false;
                return comparator.compare(x, y) == 0;
            }
            
            @Override
            public String toString() {
                return "Hashable.fromComparator(" + comparator + ")";
            }
        };
    }
    
    /**
     * Validates that this hashable satisfies the basic contract for the given object.
     * Useful for testing custom implementations.
     * 
     * @param obj the object to test
     * @throws IllegalStateException if the contract is violated
     */
    default void validateContract(T obj) {
        if (obj == null) return;
        
        // Reflexive: equal(x, x) must be true
        if (!equal(obj, obj)) {
            throw new IllegalStateException("Hashable violates reflexivity: equal(x, x) must be true");
        }
        
        // Hash consistency
        int hash1 = hash(obj);
        int hash2 = hash(obj);
        if (hash1 != hash2) {
            throw new IllegalStateException("Hashable violates hash consistency: hash(x) returned different values");
        }
    }
    
    /**
     * Returns a hashable that always returns positive hash codes by taking the
     * absolute value of the underlying hash.
     * 
     * @return a hashable that produces non-negative hash codes
     */
    default Hashable<T> withPositiveHash() {
        Hashable<T> parent = this;
        return new Hashable<T>() {
            @Override
            public int hash(T x) {
                return Math.abs(parent.hash(x));
            }
            
            @Override
            public boolean equal(T x, T y) {
                return parent.equal(x, y);
            }
            
            @Override
            public String toString() {
                return parent + ".withPositiveHash()";
            }
        };
    }
}
