package obp3.unification.syntax;

public record Var(String name) implements Term {
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }

    @Override
    public String toString() {
        return "?"+name;
    }
}
