package obp3.hashcons;

/**
 * Creates the interned representative of {@code node}. Implementations must return a new
 * value (never mutate or reuse {@code node}) that reports the given arena, tag and hash key.
 */
@FunctionalInterface
public interface HashConsMaker<T> {
    HashConsed<T> create(T node, Arena arena, int tag, int hashKey);
}
