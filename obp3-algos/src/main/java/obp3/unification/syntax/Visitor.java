package obp3.unification.syntax;

public interface Visitor<I, O> {
    default O visit(Term node, I input) { throw new UnsupportedOperationException("visit term is unsupported"); }
    O visit(Var node, I input);
    O visit(App node, I input);
}
