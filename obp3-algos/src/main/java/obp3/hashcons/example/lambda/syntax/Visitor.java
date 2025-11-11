package obp3.hashcons.example.lambda.syntax;

public interface Visitor<I,O> {
    O visit(Term node, I input);
    O visit(Var node, I input);
    O visit(App node, I input);
    O visit(Lambda node, I input);
}
