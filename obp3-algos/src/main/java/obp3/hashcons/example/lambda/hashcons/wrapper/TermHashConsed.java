package obp3.hashcons.example.lambda.hashcons.wrapper;

import obp3.hashcons.HashConsed;
import obp3.hashcons.example.lambda.syntax.Term;

public record TermHashConsed(Term node, int tag, int hashKey) implements Term, HashConsed<Term> {
    public <I, O> O accept(VisitorHashCons<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

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
