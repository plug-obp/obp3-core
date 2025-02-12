package fr.ensta;

import java.util.Iterator;
import java.util.Objects;

public class PeekableIterator<X> implements Iterator<X> {
    X peeked;
    Iterator<X> operand;

    public PeekableIterator(Iterator<X> operand) {
        this.operand = operand;
        this.peeked = null;
    }

    @Override
    public boolean hasNext() {
        if (peeked == null) return this.operand.hasNext();
        return true;
    }

    @Override
    public X next() {
        if (peeked == null) return this.operand.next();
        X value = peeked;
        peeked = null;
        return value;
    }

    public X peek() {
        if (peeked == null) {
            return peeked = this.operand.next();
        }
        return peeked;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PeekableIterator<?> that)) return false;
        return Objects.equals(peeked, that.peeked) && Objects.equals(operand, that.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peeked, operand);
    }

    @Override
    public String toString() {
        return "PeekableIterator{" +
                "peeked=" + peeked +
                ", operand=" + operand +
                '}';
    }
}
