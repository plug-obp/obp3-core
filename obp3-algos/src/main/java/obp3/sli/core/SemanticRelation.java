package obp3.sli.core;

import java.util.Iterator;
import java.util.List;

public interface SemanticRelation<A, C> {
    List<C> initial();
    List<A> actions(C configuration);
    List<C> execute(A action, C configuration);

    default IteratorSemanticRelation<A, C> toIteratorSemanticRelation() {
        return new IteratorSemanticRelation<A, C>() {
            @Override
            public Iterator<C> initial() { return SemanticRelation.this.initial().iterator(); }

            @Override
            public Iterator<A> actions(C configuration) {
                return SemanticRelation.this.actions(configuration).iterator();
            }

            @Override
            public Iterator<C> execute(A action, C configuration) {
                return SemanticRelation.this.execute(action, configuration).iterator();
            }
        };
    }

    default IOSematicRelation<Void, Void, A, C> toIOSematicRelation() {
        return new IOSematicRelation<>() {

            @Override
            public List<C> initial() {
                return SemanticRelation.this.initial();
            }

            @Override
            public List<A> actions(Void input, C configuration) {
                return SemanticRelation.this.actions(configuration);
            }

            @Override
            public Fanout<Void, C> execute(A action, Void input, C configuration) {
                return Fanout.fanout(SemanticRelation.this.execute(action, configuration));
            }
        };
    }
}
