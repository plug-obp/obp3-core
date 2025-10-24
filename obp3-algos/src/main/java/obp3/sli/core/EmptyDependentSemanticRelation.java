package obp3.sli.core;

import obp3.runtime.sli.DependentSemanticRelation;

import java.util.List;

public class EmptyDependentSemanticRelation<T> implements DependentSemanticRelation<T, Void, Void> {
    @Override
    public List<Void> initial() {
        return List.of();
    }

    @Override
    public List<Void> execute(Void action, T input, Void configuration) {
        return List.of();
    }

    @Override
    public List<Void> actions(T input, Void configuration) {
        return List.of();
    }
}
