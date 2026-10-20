package obp3.hashcons;

import obp3.datastructures.collections.linearscan.LinearScanHashMap;
import obp3.utils.Hashable;

import java.util.Collections;
import java.util.Map;

public class HashConsTable<T> {
    private final Arena arena = new Arena();
    private final Hashable<T> hashable;
    private final HashConsMaker<T> maker;
    private final Map<T, HashConsed<T>> table;

    public HashConsTable(Hashable<T> hashable, HashConsMaker<T> maker) {
        this.hashable = hashable;
        this.maker = maker;
        this.table = new LinearScanHashMap<>(10, hashable);
    }

    /**
     * Creates a table that owns fresh storage and a fresh arena, then re-interns the nodes of
     * {@code seed} into it. The seed map is never shared, extended or mutated, and its tags
     * (possibly issued by another arena) are discarded.
     */
    public HashConsTable(Hashable<T> hashable, HashConsMaker<T> maker, Map<T, HashConsed<T>> seed) {
        this(hashable, maker);
        if (seed == null) return;
        for (var entry : seed.values()) {
            if (entry != null) hashCons(entry.node());
        }
    }

    public Arena arena() {
        return arena;
    }

    /** Read-only view of the interned entries. */
    public Map<T, HashConsed<T>> map() {
        return Collections.unmodifiableMap(table);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HashConsed<T> hashCons(T input) {
        T value = input;
        if (value instanceof HashConsed t && t.isHashConsed()) {
            if (t.arena() == arena) return (HashConsed<T>) t;
            // interned by another arena: re-intern the underlying node here, never adopt the foreign tag
            value = (T) t.node();
        }
        final T node = value;
        return table.compute(
                node, (k, existing) -> {
                    // if we already have an element, return it.
                    if (existing != null && hashable.equal(existing.node(), node)) {
                        return existing;
                    }
                    var hashKey = hashable.hash(node);
                    return maker.create(node, arena, arena.nextTag(), hashKey);
                }
        );
    }
}
