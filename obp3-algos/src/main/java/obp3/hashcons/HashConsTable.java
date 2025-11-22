package obp3.hashcons;

import obp3.datastructures.collections.linearscan.LinearScanHashMap;
import obp3.utils.Hashable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HashConsTable<T> {
    private final AtomicInteger tagger = new AtomicInteger(1);
    private final Hashable<T> hashable;
    private final HashConsMaker<T> maker;
    private final Map<T, HashConsed<T>> table;

    public HashConsTable(Hashable<T> hashable, HashConsMaker<T> maker) {
        this.hashable = hashable;
        this.maker = maker;
        this.table = new LinearScanHashMap<>(10, hashable);
    }

    public HashConsTable(Hashable<T> hashable, HashConsMaker<T> maker, Map<T, HashConsed<T>> table) {
        this.hashable = hashable;
        this.maker = maker;
        //reuse / extend the input table with new entries if the right table type
        if (table instanceof LinearScanHashMap<T, HashConsed<T>>) {
            this.table = table;
            return;
        }
        // create a new table and import the entries
        this.table = new LinearScanHashMap<>(10, hashable);
        if (table == null) return;
        this.table.putAll(table);
    }

    public Map<T, HashConsed<T>> map() {
        return table;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HashConsed<T> hashCons(T value) {
        if (value instanceof HashConsed t && t.isHashConsed()) return (HashConsed<T>) t;
        return table.compute(
                value, (k, existing) -> {
                    // if we already have an element, return it.
                    if (existing != null && hashable.equal(existing.node(), value)) {
                        return existing;
                    }
                    var hashKey = hashable.hash(value);
                    return maker.create(value, tagger.getAndIncrement(), hashKey);
                }
        );
    }
}
