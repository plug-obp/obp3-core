package obp3.unification.syntax;

public sealed interface Term permits Var, App {
    default <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
}

