package obp3.hashcons.example.lambda.hashcons.expando;

import obp3.hashcons.HashConsTable;
import obp3.hashcons.HashConsed;
import obp3.hashcons.example.lambda.TermFactory;
import obp3.hashcons.example.lambda.syntax.App;
import obp3.hashcons.example.lambda.syntax.Lambda;
import obp3.hashcons.example.lambda.syntax.Term;
import obp3.hashcons.example.lambda.syntax.Var;
import obp3.utils.Hashable;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

public class ExpandoTermHashConsedFactory extends TermFactory {
    private final HashConsTable<Term> table;

    record TermHashConsed(Term node, int tag, int hashKey) implements HashConsed<Term> {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TermHashConsed other)) return false;
            return this.tag == other.tag;
        }

        @Override
        public int hashCode() {
            return hashKey;
        }
    }

    public ExpandoTermHashConsedFactory() {
        this.table = new HashConsTable<>(Hashable.standard(), TermHashConsed::new);
    }
    public ExpandoTermHashConsedFactory(HashConsTable<Term> table) {
        this.table = table;
    }

    public Map<Term, HashConsed<Term>> getHashConsMap() {
        return table.map();
    }

    public Term hashConsed(Term term) {
        return table.hashCons(term).node();
    }

    @Override
    public Term var(int index) {
        return hashConsed(new Var(index));
    }

    @Override
    public Term app(Term func, Term arg) {
        return hashConsed(new App(hashConsed(func), hashConsed(arg)));
    }

    @Override
    public Term lambda(Term body) {
        return hashConsed(new Lambda(hashConsed(body)));
    }
}
