package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class BidirectionalTest {
    @Test
    public void testTermEquationBothSides() {
        // f(X, Y) = f(a, b) should unify X=a and Y=b
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new App("f", new Var("X"), new Var("Y")), 
                new App("f", new App("a"), new App("b"))
        );

        var valuation = unificator.solveTop(constraints);
        
        var xResult = valuation.apply(new Var("X"));
        var yResult = valuation.apply(new Var("Y"));
        
        System.out.println("Constraints: f(X, Y) = f(a, b)");
        System.out.println("X = " + xResult);
        System.out.println("Y = " + yResult);
        
        if (xResult.hasSolution()) {
            System.out.println("X solution: " + xResult.toOptional().get());
        }
        if (yResult.hasSolution()) {
            System.out.println("Y solution: " + yResult.toOptional().get());
        }
    }
}
