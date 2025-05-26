package obp3.sli.core;

import java.util.List;

public interface IOSematicRelation<I, O, A, C> {
    List<C> initial();
    List<A> actions(I input, C configuration);
    Fanout<O, C> execute(A action, I input, C configuration);

    record Fanout<O, C>(O output, List<Pair<O, C>> targets) {
        public static <O,C> Fanout<O, C> fanout(O output, List<C> targets) {
            return new Fanout<>(output, targets.stream().map(c -> new Pair<O, C>(null, c)).toList());
        }

        public static <C> Fanout<Void, C> fanout(List<C> targets) {
            return new Fanout<>(null, targets.stream().map(c -> new Pair<Void, C>(null, c)).toList());
        }
    }
    record Pair<O, C>(O output, C configuration) {}
}
