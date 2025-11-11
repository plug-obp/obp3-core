package obp3.hashcons.example.lambda.syntax;

public interface Term {
    default <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}

