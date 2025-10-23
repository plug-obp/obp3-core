package obp3.des;

import obp3.sli.core.support.Clonable;
import obp3.sli.core.SemanticRelation;

import java.util.List;
import java.util.stream.Collectors;

public class DESSemantics<D extends Clonable<D>> implements SemanticRelation<DESAction<D>, DESConfiguration<D>> {
    DESConfiguration<D> initialConfiguration;

    public DESSemantics(DESConfiguration<D> initialConfiguration) {
        this.initialConfiguration = initialConfiguration;
    }

    @Override
    public List<DESConfiguration<D>> initial() {
        return List.of(initialConfiguration);
    }

    @Override
    public List<DESAction<D>> actions(DESConfiguration<D> configuration) {
        long delta = configuration.delta();
        var nextEvents = configuration.getNextEvents(delta);
        if (nextEvents.isEmpty() && delta == 0) {
            return List.of();
        }
        if (nextEvents.isEmpty()) {
            return List.of(new DESAction<>(delta, null));
        }
        return nextEvents.stream().map(e -> new DESAction<>(delta, e)).collect(Collectors.toList());
    }

    @Override
    public List<DESConfiguration<D>> execute(DESAction<D> action, DESConfiguration<D> configuration) {
        var source = configuration.clone();
        System.out.print(configuration + "--" + action + "->");
        var target = action.execute(source);
        System.out.println(target);
        return List.of(target);
    }
}
