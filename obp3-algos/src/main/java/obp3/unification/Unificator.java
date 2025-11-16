package obp3.unification;

import obp3.fixer.Fixer;
import obp3.fixer.Lattice;
import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.unification.syntax.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class Unificator {
    final Fixer<Var, UnificationAnswer<Term>> fixer = new Fixer<>(
            this::solve,
            new Lattice<>(UnificationAnswer.unknown(), Objects::equals));

    public static void main(String[] args) {
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new App("f", new Var("Y")),
                new Var("Y"), new App("m", new Var("Z"), new Var("Z")),
                new Var("Z"), new App("a")
//                ,new Var("M"), new App("f", new Var("M"))
        );

        var valuation = unificator.solveTop(constraints);
        System.out.println(constraints
                .entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .reduce("", (a, b) -> a + "\n" + b));
        System.out.println("---------------------");
        var v = new Var("X");
        System.out.println(v + "="+ valuation.apply(v));

        v = new Var("Y");
        System.out.println(v + "="+ valuation.apply(v));

        v = new Var("Z");
        System.out.println(v + "="+ valuation.apply(v));

        v = new Var("T");
        System.out.println(v + "="+ valuation.apply(v));

        v = new Var("M");
        System.out.println(v + "="+ valuation.apply(v));


        var no = unificator.occursIn(new Var("X"), new App("f", new Var("X")), valuation);
        var yes = unificator.occursIn(new Var("X"), new App("f", new App("m", new Var("Z"), new Var("Z"))), valuation);
        System.out.println("X occurs in: " + no + ", " + yes);
    }

    Function<Var, UnificationAnswer<Term>> solveTop(Map<Term, Term> constraints) {
        this.constraints = constraints;
        return fixer;
    }

    Map<Term, Term> constraints;
    SubstitutionMaker substitutionMaker = new SubstitutionMaker();

    UnificationAnswer<Term> solve(Var v, Function<Var, UnificationAnswer<Term>> mapper) {
        // Look up the direct constraint for this variable
        var directTerm = constraints.get(v);
        
        if (directTerm == null) {
            // Variable is not constrained - it remains unknown (free variable)
            return UnificationAnswer.unknown();
        }
        
        // Apply substitution to the right-hand side
        var substituted = directTerm.accept(substitutionMaker, mapper);
        
        // Simple syntactic occurs check
        if (occursIn(v, substituted, mapper)) {
            return UnificationAnswer.failure("Occurs check: " + v + " occurs in " + substituted);
        }
        
        return UnificationAnswer.of(substituted);
    }

    // Simple syntactic occurs check that doesn't use mapper (avoids infinite loops)
    private boolean simpleOccursCheckx(Var v, Term t) {
        if (t instanceof Var tv) {
            return tv.equals(v);
        }
        if (t instanceof App(String name, Term[] args)) {
            for (Term arg : args) {
                if (simpleOccursCheckx(v, arg)) {
                    return true;
                }
            }
        }
        return false;
    }


    boolean termEq(Term a, Term b) {
        if (a instanceof Var(String aName) && b instanceof Var(String bName)) {
            return aName.equals(bName);
        }
        if (a instanceof App(String aName, Term[] aTerms) && b instanceof App(String bName, Term[] bTerms)) {
            if (!aName.equals(bName)) return false;
            if (aTerms.length != bTerms.length) return false;
            for (int i = 0; i < aTerms.length; i++) {
                if (!termEq(aTerms[i], bTerms[i])) return false;
            }
            return true;
        }
        return false;
    }

    private static class SubstitutionMaker implements Visitor<Function<Var, UnificationAnswer<Term>>, Term> {
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
            var terms = Arrays.stream(node.terms()).map(t ->
                            t.accept(this, mapper)
                    ).toArray(Term[]::new);
            return new App(node.name(), terms);
        }
    }

    public boolean occursIn(Var v, Term t, Function<Var, UnificationAnswer<Term>> mapper) {
        var rr = new ToRootedGraph(t, mapper);
        var traversal = new SafetyDepthFirstTraversal<>(
                DepthFirstTraversal.Algorithm.WHILE,
                rr, -1, Function.identity(), (node) -> node instanceof Var(var x) && x.equals(v.name()));
        return !traversal.runAlone().holds;
    }

}
