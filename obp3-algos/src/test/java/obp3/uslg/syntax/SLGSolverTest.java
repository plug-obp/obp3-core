package obp3.uslg.syntax;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SLGSolverTest {

    static Var v(String name) { return new Var(name); }
    static App a(String name, Term... args) { return new App(name, args); }

    @Nested
    class CyclicTransitiveClosure {

        // connection(X,Y) :- connection(X,Z), connection(Z,Y).
        // connection(a, b).
        // connection(b, c).
        // connection(c, a).
        SLGSolver solver = new SLGSolver(List.of(
                new Rule(a("connection", v("X"), v("Y")),
                        a("connection", v("X"), v("Z")),
                        a("connection", v("Z"), v("Y"))),
                new Rule(a("connection", a("a"), a("b"))),
                new Rule(a("connection", a("b"), a("c"))),
                new Rule(a("connection", a("c"), a("a")))
        ));

        @Test
        void reachableFromA() {
            var result = solver.solve(a("connection", a("a"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("a", "b", "c"), values);
        }

        @Test
        void reachableFromB() {
            var result = solver.solve(a("connection", a("b"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("a", "b", "c"), values);
        }

        @Test
        void reachableFromC() {
            var result = solver.solve(a("connection", a("c"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("a", "b", "c"), values);
        }

        @Test
        void answersUseUserVariables() {
            var result = solver.solve(a("connection", a("a"), v("MyVar")));
            for (Substitution s : result.answers()) {
                assertTrue(s.contains(v("MyVar")), "Answer should use user variable MyVar");
                // Should not contain internal variables
                for (Var bound : s.bindings().keySet()) {
                    assertFalse(bound.name().startsWith("V"), "Should not contain canonical variables");
                    assertFalse(bound.name().startsWith("_R"), "Should not contain rule-renamed variables");
                }
            }
        }

        @Test
        void exactlyThreeAnswers() {
            var result = solver.solve(a("connection", a("a"), v("T")));
            assertEquals(3, result.answers().size());
        }
    }

    @Nested
    class AcyclicGraph {

        // connection(a, b).
        // connection(b, c).
        // connection(c, d).
        // connection(X,Y) :- connection(X,Z), connection(Z,Y).
        SLGSolver solver = new SLGSolver(List.of(
                new Rule(a("connection", a("a"), a("b"))),
                new Rule(a("connection", a("b"), a("c"))),
                new Rule(a("connection", a("c"), a("d"))),
                new Rule(a("connection", v("X"), v("Y")),
                        a("connection", v("X"), v("Z")),
                        a("connection", v("Z"), v("Y")))
        ));

        @Test
        void directConnection() {
            var result = solver.solve(a("connection", a("a"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("b", "c", "d"), values);
        }

        @Test
        void fromMiddle() {
            var result = solver.solve(a("connection", a("b"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("c", "d"), values);
        }

        @Test
        void fromEnd() {
            var result = solver.solve(a("connection", a("c"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("d"), values);
        }

        @Test
        void noConnectionFromD() {
            var result = solver.solve(a("connection", a("d"), v("T")));
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class FactsOnly {

        SLGSolver solver = new SLGSolver(List.of(
                new Rule(a("parent", a("tom"), a("bob"))),
                new Rule(a("parent", a("tom"), a("liz"))),
                new Rule(a("parent", a("bob"), a("ann")))
        ));

        @Test
        void queryWithVar() {
            var result = solver.solve(a("parent", a("tom"), v("Child")));
            var values = extractValues(result, "Child");
            assertEquals(Set.of("bob", "liz"), values);
        }

        @Test
        void groundQueryMatch() {
            var result = solver.solve(a("parent", a("tom"), a("bob")));
            assertFalse(result.isEmpty());
        }

        @Test
        void groundQueryNoMatch() {
            var result = solver.solve(a("parent", a("tom"), a("ann")));
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class MultiplePredicates {

        // parent(tom, bob). parent(bob, ann).
        // ancestor(X, Y) :- parent(X, Y).
        // ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).
        SLGSolver solver = new SLGSolver(List.of(
                new Rule(a("parent", a("tom"), a("bob"))),
                new Rule(a("parent", a("bob"), a("ann"))),
                new Rule(a("ancestor", v("X"), v("Y")),
                        a("parent", v("X"), v("Y"))),
                new Rule(a("ancestor", v("X"), v("Y")),
                        a("parent", v("X"), v("Z")),
                        a("ancestor", v("Z"), v("Y")))
        ));

        @Test
        void directAncestor() {
            var result = solver.solve(a("ancestor", a("tom"), v("D")));
            var values = extractValues(result, "D");
            assertEquals(Set.of("bob", "ann"), values);
        }

        @Test
        void fromBob() {
            var result = solver.solve(a("ancestor", a("bob"), v("D")));
            var values = extractValues(result, "D");
            assertEquals(Set.of("ann"), values);
        }
    }

    @Nested
    class LeftRecursion {

        // left-recursive rule first (would loop in Prolog)
        // path(X,Y) :- path(X,Z), edge(Z,Y).
        // edge(a, b). edge(b, c).
        SLGSolver solver = new SLGSolver(List.of(
                new Rule(a("path", v("X"), v("Y")),
                        a("edge", v("X"), v("Y"))),
                new Rule(a("path", v("X"), v("Y")),
                        a("path", v("X"), v("Z")),
                        a("edge", v("Z"), v("Y"))),
                new Rule(a("edge", a("a"), a("b"))),
                new Rule(a("edge", a("b"), a("c")))
        ));

        @Test
        void handlesLeftRecursion() {
            var result = solver.solve(a("path", a("a"), v("T")));
            var values = extractValues(result, "T");
            assertEquals(Set.of("b", "c"), values);
        }
    }

    // Utility: extract resolved ground values for a given variable name
    static Set<String> extractValues(AnswerSet as, String varName) {
        return as.answers().stream()
                .map(s -> s.get(new Var(varName)))
                .filter(t -> t instanceof App)
                .map(Term::name)
                .collect(Collectors.toSet());
    }
}
