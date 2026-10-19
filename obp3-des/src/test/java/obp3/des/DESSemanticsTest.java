package obp3.des;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.PriorityQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DESSemanticsTest {
    @Test
    void actionsDoNotDrainTheConfiguration() {
        var configuration = configurationWithSimultaneousEvents();
        var semantics = new DESSemantics<>(configuration);

        var actions = semantics.actions(configuration);

        assertEquals(2, actions.size());
        assertEquals(2, configuration.events.size());
        assertEquals(0, configuration.currentTime);
        assertEquals(0, configuration.data.value);
    }

    @Test
    void executingOneSimultaneousEventPreservesItsPeer() {
        var configuration = configurationWithSimultaneousEvents();
        var semantics = new DESSemantics<>(configuration);
        var actions = semantics.actions(configuration);
        var selectedAction = actions.getFirst();
        var peer = actions.get(1).event();

        var result = semantics.execute(selectedAction, configuration).getFirst();

        assertEquals(1, result.data.value);
        assertEquals(List.of(peer), result.events.stream().toList());
        assertEquals(2, configuration.events.size());
        var nextActions = semantics.actions(result);
        assertEquals(1, nextActions.size());
        assertEquals(0, nextActions.getFirst().delta());
        assertEquals(peer, nextActions.getFirst().event());
    }

    private DESConfiguration<Counter> configurationWithSimultaneousEvents() {
        Event<Counter> first = new Event<>("first", 5, c -> c.data.value++);
        Event<Counter> second = new Event<>("second", 5, c -> c.data.value++);
        var events = new PriorityQueue<Event<Counter>>();
        events.add(first);
        events.add(second);
        return new DESConfiguration<>(new Counter(), 0, 10, events);
    }

    private static final class Counter implements Clonable<Counter> {
        int value;

        @Override
        public Counter clone() {
            var copy = new Counter();
            copy.value = value;
            return copy;
        }
    }
}
