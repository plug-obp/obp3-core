package obp3.des;

import obp3.sli.core.support.Clonable;

import java.util.function.Consumer;

public record Event<D extends Clonable<D>> (String name, long time, Consumer<DESConfiguration<D>> action) implements Comparable<Event<D>> {
    @Override
    public int compareTo(Event<D> o) {
        return Long.compare(time, o.time);
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", time=" + time +
                '}';
    }
}
