package obp3.hashcons.example.lambda.hashcons.nowrapper;

import obp3.hashcons.HashConsTable;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.App;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Lambda;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Term;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Var;

public class DirectTermHashConsedFactory {
    private final HashConsTable<Term> table;

    public DirectTermHashConsedFactory() {
        this(new HashConsTable<>(new HashableTerm(), Term::toHashCons));
    }
    public DirectTermHashConsedFactory(HashConsTable<Term> table) {
        this.table = table;
    }

    public Term hashConsed(Term term) {
        return (Term) table.hashCons(term);
    }

    public Term var(int index) {
        return hashConsed(new Var(index));
    }

    public Term app(Term func, Term arg) {
        return hashConsed(new App(hashConsed(func), hashConsed(arg)));
    }

    public Term lambda(Term body) {
        return hashConsed(new Lambda(hashConsed(body)));
    }
}
