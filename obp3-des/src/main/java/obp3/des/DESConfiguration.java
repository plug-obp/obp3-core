package obp3.des;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class DESConfiguration<D extends Clonable<D>> implements Clonable<DESConfiguration<D>> {
    public long currentTime;
    long endTime;
    public D data;
    PriorityQueue<Event<D>> events;

    public DESConfiguration(D data, long endTime, Event<D> startEvent) {
        this(data, 0, endTime, startEvent);
    }

    public DESConfiguration(D data, long currentTime, long endTime, Event<D> startEvent) {
        this(data, currentTime, endTime, new PriorityQueue<>());
        this.events.add(startEvent);
    }

    public DESConfiguration(D data, long currentTime, long endTime, PriorityQueue<Event<D>> events) {
        this.data = data;
        this.currentTime = currentTime;
        this.endTime = endTime;
        this.events = new PriorityQueue<>(events);
    }

    public void schedule(Event<D> event) {
        //don't schedule events in the past
        if (event.time() < currentTime) { return; }
        events.add(event);
    }

    long delta() {
        if (events.isEmpty()) { return endTime - currentTime; }
        Event<D> nextEvent = events.peek();
        return Math.min(nextEvent.time() - currentTime, endTime - currentTime);
    }

    void advanceTime(long delta) {
        currentTime += delta;
    }

    List<Event<D>> getNextEvents(long delta) {
        //no events available
        if (events.isEmpty()) { return List.of(); }
        //only the events before the end time
        if (events.peek().time() > endTime) { return List.of(); }
        List<Event<D>> nextEvents = new ArrayList<>();
        //get all events with the same time
        while (!events.isEmpty() && events.peek().time() == currentTime + delta) {
            nextEvents.add(events.poll());
        }
        return nextEvents;
    }

    void processEvent(Event<D> event) {
        if (event == null) { return; }
        event.action().accept(this);
    }

    public DESConfiguration<D> clone() {
        return new DESConfiguration<>(data.clone(), currentTime, endTime, events);
    }

    @Override
    public String toString() {
        return "DESConfiguration{" +
                "currentTime=" + currentTime +
                ", endTime=" + endTime +
                ", data=" + data +
                ", events=" + events +
                '}';
    }
}
