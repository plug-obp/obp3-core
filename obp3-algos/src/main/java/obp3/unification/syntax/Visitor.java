package obp3.unification.syntax;

public interface Visitor<I, O> {
    O visit(Term node, I input);
    O visit(Var node, I input);
    O visit(App node, I input);
}
