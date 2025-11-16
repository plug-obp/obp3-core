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
        // Preprocess: decompose all structural constraints into atomic constraints
        this.constraints = decomposeConstraints(constraints);
        return fixer;
    }

    /**
     * Recursively decompose structural constraints like f(X,Y) = f(a,b) into X=a, Y=b
     * Returns a multimap to handle cases like f(X,X) = f(a,b) -> {X:[a,b]}
     */
    private Map<Term, java.util.Set<Term>> decomposeConstraints(Map<Term, Term> original) {
        var decomposed = new java.util.HashMap<Term, java.util.Set<Term>>();
        var workQueue = new java.util.LinkedList<>(original.entrySet());
        
        while (!workQueue.isEmpty()) {
            var entry = workQueue.poll();
            var lhs = entry.getKey();
            var rhs = entry.getValue();
            
            // If both sides are Apps with same name and arity, decompose
            if (lhs instanceof App(String lName, Term[] lArgs) && 
                rhs instanceof App(String rName, Term[] rArgs) &&
                lName.equals(rName) && lArgs.length == rArgs.length) {
                
                // Decompose: f(s1, s2, ...) = f(t1, t2, ...) becomes s1=t1, s2=t2, ...
                for (int i = 0; i < lArgs.length; i++) {
                    workQueue.add(Map.entry(lArgs[i], rArgs[i]));
                }
            } else {
                // Atomic constraint (at least one side is a Var or structures don't match)
                decomposed.computeIfAbsent(lhs, k -> new HashSet<>()).add(rhs);
            }
        }
        
        return decomposed;
    }

    Map<Term, java.util.Set<Term>> constraints;
    SubstitutionMaker substitutionMaker = new SubstitutionMaker();

    UnificationAnswer<Term> solve(Var v, Function<Var, UnificationAnswer<Term>> mapper) {
        // Collect all candidate terms for variable v by examining all constraints
        var candidates = new HashSet<Term>();

        // Process each constraint equation lhs = rhs
        // Note: structural constraints are already decomposed in solveTop()
        // constraints is now a Map<Term, Set<Term>>
        for (var entry : constraints.entrySet()) {
            var lhs = entry.getKey();
            var rhsSet = entry.getValue();
            
            // Check direct variable constraints
            // Only process when v appears on the LHS (v = something)
            if (lhs instanceof Var lv && lv.equals(v)) {
                candidates.addAll(rhsSet);
            }
        }

        // If no constraints found, return unknown
        if (candidates.isEmpty()) {
            return UnificationAnswer.unknown();
        }

        // Pick the "best" candidate:
        // 1. Strongly prefer non-variables over variables (concrete terms)
        // 2. Check for conflicts: if multiple non-Vars exist, they must be equal
        // 3. Among variables, pick any one (they should converge via fixpoint)
        Term bestCandidate = null;
        boolean hasNonVar = false;
        
        for (var candidate : candidates) {
            if (bestCandidate == null) {
                bestCandidate = candidate;
                hasNonVar = !(candidate instanceof Var);
            } else if (candidate instanceof Var) {
                // If bestCandidate is not a Var, keep it (prefer non-Vars)
                // If both are Vars, keep searching for a non-Var
                continue;
            } else {
                // candidate is a non-Var
                if (hasNonVar && !termEq(bestCandidate, candidate)) {
                    // Conflict: two different non-Var candidates
                    return UnificationAnswer.failure(
                        "Conflicting constraints for " + v + ": " + bestCandidate + " vs " + candidate);
                }
                // Replace Var with non-Var, or keep current non-Var
                if (!hasNonVar) {
                    bestCandidate = candidate;
                    hasNonVar = true;
                }
            }
        }

        // Now apply substitution to resolve nested variables
        var substituted = bestCandidate.accept(substitutionMaker, mapper);

        // Simple syntactic occurs check on the substituted result
        if (occursIn(v, substituted, mapper)) {
            return UnificationAnswer.failure("Occurs check: " + v + " occurs in " + substituted);
        }

        return UnificationAnswer.of(substituted);
    }

    // Simple syntactic occurs check that doesn't use mapper (avoids infinite loops)
    private boolean simpleOccursCheck(Var v, Term t) {
        if (t instanceof Var tv) {
            return tv.equals(v);
        }
        if (t instanceof App(String _, Term[] args)) {
            for (Term arg : args) {
                if (simpleOccursCheck(v, arg)) {
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
