package obp3.hashcons.example.lambda.hashcons.nowrapper.syntax;

import obp3.hashcons.Arena;
import obp3.hashcons.HashConsed;

import java.util.Objects;

public class Lambda implements Term {
    Term body; Arena arena; int tag = -1; int hashKey = -1;
    public Lambda(Term body) {
        this.body = body;
    }
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public HashConsed<Term> toHashCons(Arena arena, int tag, int hashKey) {
        var copy = new Lambda(body);
        copy.arena = arena;
        copy.tag = tag;
        copy.hashKey = hashKey;
        return copy;
    }

    @Override
    public Term node() {
        return this;
    }

    @Override
    public Arena arena() {
        return arena;
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
        if (HashConsed.sameArena(this, lambda)) return tag == lambda.tag;
        //otherwise default to structural equality
        return Objects.equals(body, lambda.body);
    }

    @Override
    public int hashCode() {
        if (isHashConsed()) return hashKey;
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
