package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.Map;

public class TestBidirectional {
    public static void main(String[] args) {
        System.out.println("=== Testing f(X, Y) = f(a, b) ===");
        
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new App("f", new Var("X"), new Var("Y")), 
                new App("f", new App("a"), new App("b"))
        );

        System.out.println("Constraints map keys:");
        for (Term key : constraints.keySet()) {
            System.out.println("  Key: " + key + " (type: " + key.getClass().getSimpleName() + ")");
            System.out.println("  Val: " + constraints.get(key));
        }

        var valuation = unificator.solveTop(constraints);
        
        System.out.println("\nQuerying X:");
        var xResult = valuation.apply(new Var("X"));
        System.out.println("X = " + xResult);
        
        System.out.println("\nQuerying Y:");
        var yResult = valuation.apply(new Var("Y"));
        System.out.println("Y = " + yResult);
    }
}
