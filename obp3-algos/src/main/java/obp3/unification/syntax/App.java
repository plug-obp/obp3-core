package obp3.unification.syntax;

import java.util.Arrays;

public record App(String name, Term ... terms) implements Term {
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public String toString() {
        return name + (terms.length > 0 ? "(" + Arrays.stream(terms).map(Object::toString).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)+ ")" : "")
                ;
    }
}
