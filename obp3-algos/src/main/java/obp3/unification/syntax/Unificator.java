package obp3.unification.syntax;

import obp3.fixer.Fixer;
import obp3.fixer.Lattice;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Unificator {
    final Fixer<Var, Optional<Term>> fixer = new Fixer<>(
            this::solve,
            new Lattice<>(Optional.<Term>empty(), Objects::equals));

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
        var v = new Var("T");
        System.out.println(v + "="+ valuation.apply(v));
    }

    Function<Var, Optional<Term>> solveTop(Map<Var, Term> constraints) {
        this.constraints = constraints;
        return fixer;
    }

    Map<Var, Term> constraints;

    Optional<Term> solve(Var queryVar, Function<Var, Optional<Term>> mapper) {
        var term = constraints.get(queryVar);
        if (term == null) return Optional.empty();
        return term.accept(new EquationSolver(), mapper);
    }

    private static class EquationSolver implements Visitor<Function<Var, Optional<Term>>, Optional<Term>> {

        @Override
        public Optional<Term> visit(Term node, Function<Var, Optional<Term>> mapper) {
            return null;
        }

        @Override
        public Optional<Term> visit(Var node, Function<Var, Optional<Term>> mapper) {
            return switch (mapper.apply(node)) {
                case Optional<Term> e when e.isPresent() -> e.get().accept(this, mapper);
                default -> Optional.of(node);
            };
        }

        @Override
        public Optional<Term> visit(App node, Function<Var, Optional<Term>> mapper) {
            var terms = Arrays.stream(node.terms()).map(t ->
                    switch (t.accept(this, mapper)) {
                        case Optional<Term> e when e.isPresent() -> e.get();
                        default -> Optional.of(node);
                    }
                    ).toArray(Term[]::new);
            return Optional.of (new App(node.name(), terms));
        }
    }

}
