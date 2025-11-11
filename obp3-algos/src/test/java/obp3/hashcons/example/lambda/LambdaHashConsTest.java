package obp3.hashcons.example.lambda;

import obp3.hashcons.example.lambda.hashcons.nowrapper.DirectTermHashConsedFactory;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Var;
import obp3.hashcons.example.lambda.hashcons.wrapper.TermHashConsed;
import obp3.hashcons.example.lambda.hashcons.wrapper.TermHashConsedFactory;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Lambda;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LambdaHashConsTest {
    @Test
    void testNoHashCons() {
        var factory = new TermFactory();
        var x = factory.var(1);
        var y = factory.var(1);
        var lambda = factory.lambda(factory.app(x, y));

        System.out.println(lambda);

        assertEquals(x, y);
        assertNotSame(x, y);
    }

    @Test
    void testHashConsWrapper() {
        var factory = new TermHashConsedFactory();
        var x = factory.var(1);
        var y = factory.var(1);
        var lambda = factory.lambda(factory.app(x, y));

        System.out.println(lambda);

        assertEquals(x, y);
        assertSame(x, y);
        assertInstanceOf(TermHashConsed.class, lambda);
        assertInstanceOf(TermHashConsed.class, x);
    }

    @Test
    void testHashConsNoWrapper() {
        var factory = new DirectTermHashConsedFactory();
        var x = factory.var(1);
        var y = factory.var(1);
        var lambda = factory.lambda(factory.app(x, y));

        System.out.println(lambda);

        assertEquals(x, y);
        assertSame(x, y);
        assertInstanceOf(Lambda.class, lambda);
        assertInstanceOf(Var.class, x);
    }
}
