package obp3.hashcons.example.lambda.hashcons.nowrapper.syntax;

import obp3.hashcons.HashConsed;

import java.util.Objects;

public class Var implements Term {
    int index; int tag = -1; int hashKey = -1;
    public Var(int index) {
        this.index = index;
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
        if (!(o instanceof Var var)) return false;
        if (tag > 0 && var.tag > 0) return tag == var.tag;
        return index == var.index;
    }

    @Override
    public int hashCode() {
        if (hashKey > 0) return hashKey;
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
