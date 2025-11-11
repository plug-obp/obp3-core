package obp3.hashcons.example.lambda.syntax;

public record Lambda(Term body) implements Term {
    @Override
    public <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}
