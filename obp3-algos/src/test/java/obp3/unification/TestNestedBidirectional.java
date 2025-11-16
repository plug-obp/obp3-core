package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.Map;

public class TestNestedBidirectional {
    public static void main(String[] args) {
        System.out.println("=== Testing f(g(X, a), Y) = f(g(b, a), h(Z)) ===");
        
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new App("f", 
                    new App("g", new Var("X"), new App("a")),
                    new Var("Y")),
                new App("f",
                    new App("g", new App("b"), new App("a")),
                    new App("h", new Var("Z")))
        );

        System.out.println("Constraints:");
        for (var entry : constraints.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }

        var valuation = unificator.solveTop(constraints);
        
        System.out.println("\nQuerying variables:");
        var xResult = valuation.apply(new Var("X"));
        System.out.println("X = " + xResult);
        
        var yResult = valuation.apply(new Var("Y"));
        System.out.println("Y = " + yResult);
        
        var zResult = valuation.apply(new Var("Z"));
        System.out.println("Z = " + zResult);
    }
}
