package obp3.hashcons.example.lambda.hashcons.nowrapper.syntax;

import obp3.hashcons.Arena;
import obp3.hashcons.HashConsed;

import java.util.Objects;

public class App implements Term {
    Term left; Term right; Arena arena; int tag = -1; int hashKey = -1;
    public App(Term left, Term right) {
        this.left = left;
        this.right = right;
    }
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public HashConsed<Term> toHashCons(Arena arena, int tag, int hashKey) {
        var copy = new App(left, right);
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
        if (!(o instanceof App app)) return false;
        //if hashConsed use the hashCons tag
        if (HashConsed.sameArena(this, app)) return tag == app.tag;
        //otherwise default to structural equality
        return Objects.equals(left, app.left) && Objects.equals(right, app.right);
    }

    @Override
    public int hashCode() {
        //if hashConsed use the hashCons tag
        if (isHashConsed()) return hashKey;
        //otherwise structural hash
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "App{" +
                "left=" + left +
                ", right=" + right +
                ", tag=" + tag +
                ", hashKey=" + hashKey +
                '}';
    }
}
