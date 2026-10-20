package obp3.hashcons;

import obp3.utils.Hashable;

import java.util.function.Supplier;

public interface HashConsed<T> {
    T node();
    /** Tag, unique only within {@link #arena()}; meaningful only if {@link #isHashConsed()}. */
    int tag();
    /** The arena that issued the tag, or null if not interned. */
    Arena arena();
    int hashKey();
    default boolean isHashConsed() {
        return true;
    }

    record FunctionalHashConsed<T>(
            Supplier<T> nodeSupplier,
            Supplier<Arena> arenaSupplier,
            Supplier<Integer> tagSupplier,
            Supplier<Integer> hashKeySupplier) implements HashConsed<T> {

        @Override
        public T node() {
            return nodeSupplier.get();
        }

        @Override
        public Arena arena() {
            return arenaSupplier.get();
        }

        @Override
        public int tag() {
            return tagSupplier.get();
        }

        @Override
        public int hashKey() {
            return hashKeySupplier.get();
        }
    }

    /** True iff both values are interned in the same (non-null) arena. */
    static boolean sameArena(HashConsed<?> a, HashConsed<?> b) {
        return a.isHashConsed() && b.isHashConsed() && a.arena() != null && a.arena() == b.arena();
    }

    /** Tag equality within one arena, equality of the underlying nodes otherwise. */
    static boolean equalityFunction (Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a instanceof HashConsed<?> ahc && b instanceof HashConsed<?> bhc) {
            return sameArena(ahc, bhc) ? ahc.tag() == bhc.tag() : ahc.node().equals(bhc.node());
        }
        return a.equals(b);
    }
    static <T> int hashCodeFunction(T o) {
        return o instanceof HashConsed<?> hc ? hc.hashKey() : o.hashCode();
    }

    static <T> Hashable<T> hashable() {
        return Hashable.from(HashConsed::equalityFunction, HashConsed::hashCodeFunction);
    }
}

