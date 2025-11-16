package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class UnificatorTest {

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSimpleUnification() {
        // X = a
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new App("a")
        );

        Function<Var, UnificationAnswer<Term>> valuation = unificator.solveTop(constraints);
        
        var result = valuation.apply(new Var("X"));
        assertTrue(result.hasSolution(), "X should have a solution");
        assertEquals(new App("a"), result.toOptional().get());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testChainedUnification() {
        // X = Y, Y = Z, Z = a
        // Expected: X = a, Y = a, Z = a
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new Var("Y"),
                new Var("Y"), new Var("Z"),
                new Var("Z"), new App("a")
        );

        Function<Var, UnificationAnswer<Term>> valuation = unificator.solveTop(constraints);
        
        var xResult = valuation.apply(new Var("X"));
        var yResult = valuation.apply(new Var("Y"));
        var zResult = valuation.apply(new Var("Z"));
        
        assertTrue(xResult.hasSolution(), "X should have a solution");
        assertTrue(yResult.hasSolution(), "Y should have a solution");
        assertTrue(zResult.hasSolution(), "Z should have a solution");
        
        assertEquals(new App("a"), xResult.toOptional().get());
        assertEquals(new App("a"), yResult.toOptional().get());
        assertEquals(new App("a"), zResult.toOptional().get());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testNestedUnification() {
        // X = f(Y), Y = m(Z, Z), Z = a
        // Expected: X = f(m(a, a)), Y = m(a, a), Z = a
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new App("f", new Var("Y")),
                new Var("Y"), new App("m", new Var("Z"), new Var("Z")),
                new Var("Z"), new App("a")
        );

        Function<Var, UnificationAnswer<Term>> valuation = unificator.solveTop(constraints);
        
        var xResult = valuation.apply(new Var("X"));
        var yResult = valuation.apply(new Var("Y"));
        var zResult = valuation.apply(new Var("Z"));
        
        assertTrue(xResult.hasSolution(), "X should have a solution");
        assertTrue(yResult.hasSolution(), "Y should have a solution");
        assertTrue(zResult.hasSolution(), "Z should have a solution");
        
        // Check the nested structure
        assertEquals(new App("a"), zResult.toOptional().get());
        assertEquals(new App("m", new App("a"), new App("a")), yResult.toOptional().get());
        assertEquals(new App("f", new App("m", new App("a"), new App("a"))), xResult.toOptional().get());
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testOccursCheckFailure() {
        // X = f(X) - should fail occurs check
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new App("f", new Var("X"))
        );

        Function<Var, UnificationAnswer<Term>> valuation = unificator.solveTop(constraints);
        
        var xResult = valuation.apply(new Var("X"));
        
        assertTrue(xResult.isFailure(), "X should fail occurs check");
        assertTrue(xResult.failureReason().toString().contains("Occurs check"));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testUnconstrainedVariable() {
        // X = a, but ask for Y which is unconstrained
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new App("a")
        );

        Function<Var, UnificationAnswer<Term>> valuation = unificator.solveTop(constraints);
        
        var yResult = valuation.apply(new Var("Y"));
        
        assertTrue(yResult.isUnknown(), "Y should be unknown (free variable)");
    }
    
    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testComplexNestedStructure() {
        // X = cons(Y, Z), Y = pair(A, B), Z = nil, A = 1, B = 2
        // Expected: X = cons(pair(1, 2), nil)
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new Var("X"), new App("cons", new Var("Y"), new Var("Z")),
                new Var("Y"), new App("pair", new Var("A"), new Var("B")),
                new Var("Z"), new App("nil"),
                new Var("A"), new App("1"),
                new Var("B"), new App("2")
        );

        Function<Var, UnificationAnswer<Term>> valuation = unificator.solveTop(constraints);
        
        var xResult = valuation.apply(new Var("X"));
        
        assertTrue(xResult.hasSolution(), "X should have a solution");
        
        var expected = new App("cons", 
                new App("pair", new App("1"), new App("2")), 
                new App("nil"));
        assertEquals(expected, xResult.toOptional().get());
    }
}

