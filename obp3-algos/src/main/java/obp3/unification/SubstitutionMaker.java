package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import obp3.unification.syntax.Visitor;

import java.util.Arrays;
import java.util.function.Function;

class SubstitutionMaker implements Visitor<Function<Var, UnificationAnswer<Term>>, Term> {
    public Term normalize(Term term, Function<Var, UnificationAnswer<Term>> mapper) {
        return term.accept(this, mapper);
    }

    @Override
    public Term visit(Var node, Function<Var, UnificationAnswer<Term>> mapper) {
        var answer = mapper.apply(node);
        // Only substitute if we have a solution AND it's not the same variable
        // This prevents infinite loops
        return answer
                .map(e -> {
                    // Avoid infinite recursion: don't re-substitute if we got the same var back
                    if (e instanceof Var ve && ve.equals(node)) {
                        return node;
                    }
                    return e.accept(this, mapper);
                })
                .solutionOrElse(node);
    }

    @Override
    public Term visit(App node, Function<Var, UnificationAnswer<Term>> mapper) {
        var terms = node.terms().stream().map(t ->
                t.accept(this, mapper)
        ).toArray(Term[]::new);
        return new App(node.name(), terms);
    }
}
