package obp3.hashcons;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Identity of an interning domain. Tags are only unique within one arena, so every
 * {@link HashConsed} value carries the arena that issued its tag. Each {@link HashConsTable}
 * owns exactly one arena, and the arena owns the tag allocator.
 */
public final class Arena {
    private static final AtomicInteger IDS = new AtomicInteger();
    private final int id = IDS.incrementAndGet();
    private final AtomicInteger tagger = new AtomicInteger(1);

    int nextTag() {
        return tagger.getAndIncrement();
    }

    @Override
    public String toString() {
        return "Arena#" + id;
    }
}
