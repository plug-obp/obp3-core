package obp3.utils;

import java.util.Iterator;
import java.util.Objects;

public class PeekableIterator<X> implements Iterator<X> {
    X peeked;
    boolean hasPeeked;
    Iterator<X> operand;

    public PeekableIterator(Iterator<X> operand) {
        this.operand = operand;
    }

    @Override
    public boolean hasNext() {
        return hasPeeked || this.operand.hasNext();
    }

    @Override
    public X next() {
        if (!hasPeeked) return this.operand.next();
        X value = peeked;
        peeked = null;
        hasPeeked = false;
        return value;
    }

    public X peek() {
        if (!hasPeeked) {
            peeked = this.operand.next();
            hasPeeked = true;
        }
        return peeked;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PeekableIterator<?> that)) return false;
        return hasPeeked == that.hasPeeked && Objects.equals(peeked, that.peeked) && Objects.equals(operand, that.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peeked, hasPeeked, operand);
    }

    @Override
    public String toString() {
        return "PeekableIterator{" +
                "peeked=" + peeked +
                ", operand=" + operand +
                '}';
    }
}
