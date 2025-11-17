package obp3.unification;

import obp3.modelchecking.safety.SafetyDepthFirstTraversal;
import obp3.traversal.dfs.DepthFirstTraversal;
import obp3.unification.syntax.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

public class Unificator1 {
    public static void main(String[] args) {
        Unificator1 unificator = new Unificator1();

        BiFunction<Term, Term, Function<Var, UnificationAnswer<Term>>> runUnify = (Term lhs, Term rhs) -> {
            System.out.println(lhs + " = " + rhs);
            unificator.substitutions.clear();
            var vx = unificator.unify(lhs, rhs, unificator::substitution);
            if (vx.isEmpty()) {
                System.out.println("No solution found");
                return (t) -> UnificationAnswer.unknown();
            }
            System.out.println("Substs: " + unificator.substitutions);
            SubstitutionMaker sm = new SubstitutionMaker();
            System.out.println(sm.normalize(lhs, vx.get()) + " = " + sm.normalize(rhs, vx.get()));
            return vx.get();
        };


        Term lhs = new App("f",
                        new App("g", new Var("X"), new App("a")),
                        new Var("Y"));
        Term rhs = new App("f",
                        new App("g", new App("b"), new App("a")),
                        new App("h", new Var("Z")));


        var valuation = runUnify.apply(lhs, rhs);

        System.out.println("Substs: " + unificator.substitutions);

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

        lhs = new App("f",
                        new App("g", new Var("X"), new App("a")),
                        new App("h", new Var("Y")));
        rhs = new App("f",
                        new App("g", new App("b"), new App("a")),
                        new App("h", new App("m", new Var("Z"), new Var("Z"))));
        runUnify.apply(lhs, rhs);

        lhs = new App("f", new Var("X"));
        rhs = new Var("X");
        runUnify.apply(lhs, rhs);

        lhs = new App("p", new Var("X"), new Var("Y"));
        rhs = new App("p", new Var("X"), new Var("Y"));
        runUnify.apply(lhs, rhs);

        lhs = new App("p", new Var("X"), new Var("Y"));
        rhs = new App("p", new Var("Y"), new Var("X"));
        runUnify.apply(lhs, rhs);

        lhs = new App("p", new Var("X"), new Var("Y"), new App("a"));
        rhs = new App("p", new Var("Y"), new Var("X"), new Var("X"));
        runUnify.apply(lhs, rhs);

        lhs = new App("q",
                new App("p", new Var("X"), new Var("Y")),
                new App("p", new Var("Y"), new Var("X")));
        rhs = new App("q", new Var("Z"), new Var("Z"));
        runUnify.apply(lhs, rhs);
    }



    Map<Var, UnificationAnswer<Term>> substitutions = new HashMap<>();
    UnificationAnswer<Term> substitution(Var v) {
        return substitutions.getOrDefault(v, UnificationAnswer.unknown());
    }

    boolean extend(Var t1, Term t2) {
        if (occursIn(t1, t2, this::substitution)) {
            System.out.println("[Occurs-check] Infinite loop detected");
            return false;
        }
        return substitutions.put(t1, UnificationAnswer.of(t2)) == null;
    }


    Optional<Function<Var, UnificationAnswer<Term>>> unify(Term lhs, Term rhs, Function<Var, UnificationAnswer<Term>> mapper) {
        var t1 = substitutionMaker.normalize(lhs, mapper);
        var t2 = substitutionMaker.normalize(rhs, mapper);
        //if they are the same
        if (termEq(t1, t2)) {
            return Optional.of(mapper);
        }
        //if one is a var
        if (t1 instanceof Var v) {
            if (!extend(v, t2)) return Optional.empty();
            return Optional.of(mapper);
        }
        //if the other is a var
        if (t2 instanceof Var v) {
            return unify(t2, t1, mapper);
        }
        if (t1 instanceof App(String name1, Term[] args1) && t2 instanceof App(String name2, Term[] args2)) {
            if (!name1.equals(name2) || args1.length != args2.length) return Optional.empty();
            return IntStream.range(0, args1.length)
                    .boxed().
                    reduce(
                            Optional.of(mapper),
                            (ons, i) -> ons.flatMap( ns -> unify(args1[i], args2[i], ns)),
                            (os1, os2) -> os1.isPresent() ? os1 : os2
                    );
        }
        return Optional.empty();
    }




    SubstitutionMaker substitutionMaker = new SubstitutionMaker();

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
