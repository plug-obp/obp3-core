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
        Map<Var, Term> constraints = Map.of(
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

    Function<Var, UnificationAnswer<Term>> solveTop(Map<Var, Term> constraints) {
        this.constraints = constraints;
        return fixer;
    }

    Map<Var, Term> constraints;
    SubstitutionMaker substitutionMaker = new SubstitutionMaker();

    UnificationAnswer<Term> solve(Var v, Function<Var, UnificationAnswer<Term>> mapper) {
        // for each constraint do:
        // process equation A = B

//        var candidates = new HashSet<Term>();
//
//        constraints.forEach((lhs, rhs) -> {
//            var s = lhs.accept(substitutionMaker, mapper);
//            var t = rhs.accept(substitutionMaker, mapper);
//
//
//            if (!termEq(s, t)) {
//                if (s instanceof Var && s.equals(v)) {
//                    //candidates add t
//                    candidates.add(t);
//                } else if (t instanceof Var && t.equals(v)) {
//                    candidates.add(s);
//                } else if (s instanceof App(String sName, Term[] sArgs) && t instanceof App(String tName, Term[] tArgs)) {
//                    if (! (sName.equals(tName) && sArgs.length == tArgs.length)) {
//                        throw new RuntimeException("Structural mismatch, incompatible applications: " + sName + " " + tName);
//                    }
//                }
//            }
//
//            UnificationAnswer<Term> answer;
//            if (candidates.isEmpty()) {
//                answer = UnificationAnswer.<Term>unknown();
//                return answer;
//            }
//            var term = candidates.stream().findAny().get();
//            var allEqual = candidates.stream().allMatch((p) -> termEq(term, p));
//            if (!allEqual) {
//                answer = UnificationAnswer.<Term>failure("Incompatible equations: " + s + " != " + t);
//                return answer;
//            }
//
//            if (occursIn(v, rhs, mapper))
//                answer = UnificationAnswer.<Term>failure("Term occurs in RHS: " + rhs);
//            answer = UnificationAnswer.of(rhs);
//
//        });





        var term = constraints.get(v);
        if (term == null) return UnificationAnswer.failure("Unknown var: " + v + ")");
        return UnificationAnswer.of( term.accept(substitutionMaker, mapper) );
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
            return mapper.apply(node)
                    .map(e -> e.accept(this, mapper))
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
