package obp3.hashcons.example.lambda.hashcons.wrapper;

import obp3.hashcons.Hashable;
import obp3.hashcons.example.lambda.syntax.Term;

public class HashableTerm implements Hashable<Term> {
    @Override
    public boolean equal(Term x, Term y) {
        return x.equals(y);
    }

    @Override
    public int hash(Term x) {
        return x.hashCode();
    }
}
