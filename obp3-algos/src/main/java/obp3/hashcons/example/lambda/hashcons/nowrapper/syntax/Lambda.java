package obp3.hashcons.example.lambda.hashcons.nowrapper.syntax;

import obp3.hashcons.HashConsed;

import java.util.Objects;

public class Lambda implements Term {
    Term body; int tag = -1; int hashKey = -1;
    public Lambda(Term body) {
        this.body = body;
    }
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public HashConsed<Term> toHashCons(int tag, int hashKey) {
        this.tag = tag;
        this.hashKey = hashKey;
        return this;
    }

    @Override
    public Term node() {
        return this;
    }

    @Override
    public int tag() {
        return tag;
    }

    @Override
    public int hashKey() {
        return hashKey;
    }

    @Override
    public boolean isHashConsed() {
        return tag > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Lambda lambda)) return false;
        //if hashConsed use the hashCons tag
        if (tag > 0 && lambda.tag > 0) return tag == lambda.tag;
        //otherwise default to structural equality
        return Objects.equals(body, lambda.body);
    }

    @Override
    public int hashCode() {
        if (hashKey > 0) return hashKey;
        return Objects.hash(body);
    }

    @Override
    public String toString() {
        return "Lambda{" +
                "body=" + body +
                ", tag=" + tag +
                ", hashKey=" + hashKey +
                '}';
    }
}
