package obp3.unification;

import obp3.fixer.Fixer;
import obp3.fixer.Lattice;
import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import obp3.unification.syntax.Visitor;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Unificator {
    final Fixer<Var, UnificationAnswer<Term>> fixer = new Fixer<>(
            this::solve,
            new Lattice<>(UnificationAnswer.<Term>unknown(), Objects::equals));

    public static void main(String[] args) {
        Unificator unificator = new Unificator();
        Map<Var, Term> constraints = Map.of(
                new Var("X"), new App("f", new Var("Y")),
                new Var("Y"), new App("m", new Var("Z"), new Var("Z")),
                new Var("Z"), new App("a"));

        var valuation = unificator.solveTop(constraints);
        System.out.println(constraints
                .entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce("", (a, b) -> a + "\n" + b));
        System.out.println("---------------------");
        var v = new Var("Y");
        System.out.println(v + "="+ valuation.apply(v));
    }

    Function<Var, UnificationAnswer<Term>> solveTop(Map<Var, Term> constraints) {
        this.constraints = constraints;
        return fixer;
    }

    Map<Var, Term> constraints;

    UnificationAnswer<Term> solve(Var queryVar, Function<Var, UnificationAnswer<Term>> mapper) {
        var term = constraints.get(queryVar);
        if (term == null) return UnificationAnswer.failure("Unknown query var: " + queryVar + ")");
        return term.accept(new EquationSolver(), mapper);
    }

    private static class EquationSolver implements Visitor<Function<Var, UnificationAnswer<Term>>, UnificationAnswer<Term>> {

        @Override
        public UnificationAnswer<Term> visit(Var node, Function<Var, UnificationAnswer<Term>> mapper) {
            return mapper.apply(node).fold(
                    e -> e.accept(this, mapper),
                    UnificationAnswer::failure,
                    () -> UnificationAnswer.of(node));
        }

        @Override
        public UnificationAnswer<Term> visit(App node, Function<Var, UnificationAnswer<Term>> mapper) {
            var terms = Arrays.stream(node.terms()).map(t ->
                            t.accept(this, mapper).fold(e -> e, UnificationAnswer::failure, () -> node)
                    ).toArray(Term[]::new);
            return UnificationAnswer.of(new App(node.name(), terms));
        }
    }

}
