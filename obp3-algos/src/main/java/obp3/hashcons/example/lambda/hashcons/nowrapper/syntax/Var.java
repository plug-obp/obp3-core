package obp3.hashcons.example.lambda.hashcons.nowrapper.syntax;

import obp3.hashcons.Arena;
import obp3.hashcons.HashConsed;

import java.util.Objects;

public class Var implements Term {
    int index; Arena arena; int tag = -1; int hashKey = -1;
    public Var(int index) {
        this.index = index;
    }
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public HashConsed<Term> toHashCons(Arena arena, int tag, int hashKey) {
        var copy = new Var(index);
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
        if (!(o instanceof Var var)) return false;
        if (HashConsed.sameArena(this, var)) return tag == var.tag;
        return index == var.index;
    }

    @Override
    public int hashCode() {
        if (isHashConsed()) return hashKey;
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        return "Var{" +
                "index=" + index +
                ", tag=" + tag +
                ", hashKey=" + hashKey +
                '}';
    }
}
