package obp3.des;

import java.util.function.Consumer;

public record Event<D> (String name, long time, Consumer<DESConfiguration<D>> action) implements Comparable<Event<D>> {
    @Override
    public int compareTo(Event<D> o) {
        return Long.compare(time, o.time);
    }
}
