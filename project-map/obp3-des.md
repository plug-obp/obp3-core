# obp3-des

Discrete-event simulation represented as a semantic relation so the generic traversal/model-checking algorithms can explore its configurations.

## Build Boundary

- Gradle project: `obp3-des`; JPMS name: `obp.des`.
- Java 23; depends on `obp3-runtime` and `obp3-algos`.
- [`module-info.java`](../obp3-des/src/main/java/module-info.java) exports `obp3.des`.
- No Java test sources were present at review time.

## Source Map

- [`DESConfiguration`](../obp3-des/src/main/java/obp3/des/DESConfiguration.java): current/end time, simulation data, priority queue, scheduling, time delta, event removal, and cloning.
- [`Event`](../obp3-des/src/main/java/obp3/des/Event.java): timestamped callback; ordering compares timestamps only.
- [`DESAction`](../obp3-des/src/main/java/obp3/des/DESAction.java): advances time and executes one event against a configuration.
- [`DESSemantics`](../obp3-des/src/main/java/obp3/des/DESSemantics.java): translates next scheduled events into relation actions and executes them.
- [`Clonable`](../obp3-des/src/main/java/obp3/des/Clonable.java): simulation data clone contract. Correct branch isolation depends on implementations deeply copying mutable state.
- `exemples/pingpong/`: the PingPong scheduling example (the directory spelling is part of the current source path).
- [`Main`](../obp3-des/src/main/java/obp3/des/Main.java): runnable example that explores the semantics using algorithms from `obp3-algos`.

## Main Flow

`DESSemantics.actions(configuration)` computes a time delta and returns one `DESAction` for each event at that time. `execute(action, configuration)` clones the configuration, then `DESAction.execute` advances the clone and invokes the callback. Event callbacks commonly mutate `configuration.data` and schedule later events.

## Static Review Notes

- Same-timestamp event handling needs regression coverage. `getNextEvents` polls all events with the next time during `actions()`, then `actions()` returns separate event choices. Since action enumeration mutates the source configuration, a branch that executes one event can lose the other events already polled. Consider making action discovery pure and representing simultaneous events/branch state explicitly.
- `DESSemantics.execute` writes every transition to standard output. This is noisy for library use and can dominate simulation cost; prefer an injected observer/logger if tracing is required.
- There are no module tests at review time. Highest-value initial tests: multiple simultaneous events across branches, event exactly at `endTime`, event after `endTime`, empty schedule, scheduling in the past, and clone isolation for mutable data.