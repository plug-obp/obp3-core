package obp3.hashcons.example.lambda.hashcons.wrapper;

import obp3.hashcons.HashConsTable;
import obp3.hashcons.example.lambda.TermFactory;
import obp3.hashcons.example.lambda.syntax.App;
import obp3.hashcons.example.lambda.syntax.Lambda;
import obp3.hashcons.example.lambda.syntax.Term;
import obp3.hashcons.example.lambda.syntax.Var;
import obp3.utils.Hashable;

public class TermHashConsedFactory extends TermFactory {
    private final HashConsTable<Term> table;

    public TermHashConsedFactory() {
        this(new HashConsTable<>(Hashable.standard(), TermHashConsed::new));
    }
    public TermHashConsedFactory(HashConsTable<Term> table) {
        this.table = table;
    }

    public TermHashConsed hashConsed(Term term) {
        return (TermHashConsed) table.hashCons(term);
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
