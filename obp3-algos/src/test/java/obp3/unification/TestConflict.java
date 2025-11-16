package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;

import java.util.Map;

public class TestConflict {
    public static void main(String[] args) {
        System.out.println("=== Testing f(X, X) = f(a, b) ===");
        
        Unificator unificator = new Unificator();
        Map<Term, Term> constraints = Map.of(
                new App("f", new Var("X"), new Var("X")),
                new App("f", new App("a"), new App("b"))
        );

        System.out.println("Constraints:");
        for (var entry : constraints.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }

        var valuation = unificator.solveTop(constraints);
        
        System.out.println("\nQuerying X:");
        var xResult = valuation.apply(new Var("X"));
        System.out.println("X = " + xResult);
    }
}
