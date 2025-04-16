package obp3.des;

import java.util.function.Function;

public record Event<D> (String name, long time, Function<D, D> action) implements Comparable<Event<D>> {
    @Override
    public int compareTo(Event<D> o) {
        return Long.compare(time, o.time);
    }
}
