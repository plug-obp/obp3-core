package obp3.unification.syntax;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record App(String name, List<Term> terms) implements Term {
    public App(String name, Term... terms) {
        this(name, Arrays.asList(terms));
    }
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public String toString() {
        return name + (!terms.isEmpty() ? "(" + terms.stream().map(Object::toString).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b)+ ")" : "")
                ;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return name.equals(app.name) && terms.equals(app.terms);
    }
    
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + Objects.hashCode(terms);
    }

    @Override
    public Term substitute(Function<Var, Term> mapper) {
        List<Term> newTerms = terms.stream().map(t -> t.substitute(mapper)).toList();
        return new App(name, newTerms.toArray(new Term[0]));
    }
}
