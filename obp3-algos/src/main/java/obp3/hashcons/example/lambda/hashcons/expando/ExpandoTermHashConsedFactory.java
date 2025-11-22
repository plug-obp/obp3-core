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

public class ExpandoTermHashConsedFactory extends TermFactory {
    private final HashConsTable<Term> table;

    record TermHashConsed(Term node, int tag, int hashKey) implements HashConsed<Term> {}

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
