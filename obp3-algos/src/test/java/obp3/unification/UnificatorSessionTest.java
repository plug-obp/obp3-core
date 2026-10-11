package obp3.unification;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@Timeout(5)
class UnificatorSessionTest {
    @Test
    void subsequentSessionDoesNotReuseCachedAnswers() {
        var unificator = new Unificator();
        var x = new Var("X");
        var first = unificator.solveTop(Map.of(x, new App("a")));
        assertEquals(new App("a"), first.apply(x).solution());

        var second = unificator.solveTop(Map.of(x, new App("b")));

        assertNotSame(first, second);
        assertEquals(new App("b"), second.apply(x).solution());
        assertEquals(new App("a"), first.apply(x).solution());
    }

    @Test
    void earlierSessionRetainsEquationsForUnqueriedVariablesAndDependencies() {
        var unificator = new Unificator();
        var x = new Var("X");
        var y = new Var("Y");
        var first = unificator.solveTop(Map.of(x, new App("f", y), y, new App("a")));
        var second = unificator.solveTop(Map.of(x, new App("f", y), y, new App("b")));

        assertEquals(new App("f", new App("b")), second.apply(x).solution());
        assertEquals(new App("f", new App("a")), first.apply(x).solution());
        assertEquals(new App("a"), first.apply(y).solution());
        assertEquals(new App("b"), second.apply(y).solution());
    }

    @Test
    void returnedSessionRetainsDecomposedConstraintsAfterInputMapChanges() {
        var unificator = new Unificator();
        var x = new Var("X");
        var y = new Var("Y");
        var input = new HashMap<Term, Term>();
        input.put(new App("pair", x, y), new App("pair", new App("a"), new App("b")));
        var first = unificator.solveTop(input);

        input.clear();
        input.put(x, new App("c"));
        var second = unificator.solveTop(input);

        assertEquals(new App("a"), first.apply(x).solution());
        assertEquals(new App("b"), first.apply(y).solution());
        assertEquals(new App("c"), second.apply(x).solution());
    }
}
