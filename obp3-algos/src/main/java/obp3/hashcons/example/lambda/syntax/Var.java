package obp3.hashcons.example.lambda.syntax;

public record Var(int index) implements Term {
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
