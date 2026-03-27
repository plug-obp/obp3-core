package obp3.monitoring.trace;

import obp3.runtime.sli.DeterministicIOSemanticRelation;
import obp3.runtime.sli.IOSematicRelation;
import obp3.runtime.sli.Step;

import java.time.Instant;
import java.util.Optional;

public class TraceSemantics<M> implements DeterministicIOSemanticRelation<M, Step<TraceTiming, M>, TraceAction, TraceConfiguration<M>> {


    @Override
    public Optional<TraceConfiguration<M>> initial() {
        return Optional.empty();
    }

    @Override
    public Optional<TraceAction> actions(M input, TraceConfiguration<M> configuration) {
        if (input == null) {
            return Optional.empty();
        }
        return Optional.of(new TraceAction());
    }

    @Override
    public Optional<IOSematicRelation.Pair<Step<TraceTiming, M>, TraceConfiguration<M>>> execute(TraceAction action, M input, TraceConfiguration<M> configuration) {
        if (action == null) {
            return Optional.empty();
        }
        if (configuration.measurement() == null) {
            if (input == null) {
                return Optional.empty();
            }
            return Optional.of(new IOSematicRelation.Pair<>(null, new TraceConfiguration<>(input, Instant.now())));
        }

        if (input == null) {
            return Optional.of(new IOSematicRelation.Pair<>(null, configuration));
        }
        var instant = Instant.now();
        var current = new TraceConfiguration<>(input, instant);
        return Optional.of(
                new IOSematicRelation.Pair<>(
                        new Step<>(configuration.measurement(), Optional.of(new TraceTiming(configuration.instant(), instant)), input),
                        current));
    }

    @Override
    public boolean actionsIsPure() {
        return true;
    }

    @Override
    public boolean executeIsPure() {
        return true;
    }
}

