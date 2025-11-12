package obp3.hashcons;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HashConsTable<T> {
    private final AtomicInteger tagger = new AtomicInteger(1);
    private final Hashable<T> hashable;
    private final HashConsMaker<T> maker;
    private final Map<Key<T>, HashConsed<T>> table;

    //This is needed to allow for custom Hashable instances, instead of equals && hashCode
    private record Key<T>(int hashKey, T node, Hashable<T> hashable) {
        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key(int key, Object node1, _))) return false;
            return this.hashKey == key && hashable.equal(this.node, (T)node1);
        }

        @Override
        public int hashCode() {
            return hashKey;
        }
    }

    public HashConsTable(Hashable<T> hashable, HashConsMaker<T> maker) {
        this.hashable = hashable;
        this.maker = maker;
        this.table = new ConcurrentHashMap<>();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HashConsed<T> hashCons(T value) {
        if (value instanceof HashConsed t && t.isHashConsed()) return (HashConsed<T>) t;
        var hashKey = hashable.hash(value);
        var key = new Key<>(hashKey, value, hashable);

        return table.compute(
                key, (k, existing) -> {
                    // if we already have an element, return it.
                    if (existing != null && hashable.equal(existing.node(), value)) {
                        return existing;
                    }
                    return maker.create(value, tagger.getAndIncrement(), hashKey);
                }
        );
    }
}
