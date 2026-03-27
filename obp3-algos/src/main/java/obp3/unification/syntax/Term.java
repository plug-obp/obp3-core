package obp3.unification.syntax;

import obp3.unification.UnificationAnswer;

import java.util.function.Function;

public sealed interface Term permits Var, App {
    default <I, O> O accept(Visitor<I, O> visitor, I input) {
        return visitor.visit(this, input);
    }
    Term substitute(Function<Var, Term> mapper);
    String name();
}

