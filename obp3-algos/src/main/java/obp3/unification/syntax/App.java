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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return name.equals(app.name) && Arrays.equals(terms, app.terms);
    }
    
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + Arrays.hashCode(terms);
    }
}
