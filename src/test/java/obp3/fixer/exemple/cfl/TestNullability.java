package obp3.fixer.exemple.cfl;

import obp3.fixer.exemple.cfl.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestNullability {
    @Test
    public void testEmpty() {
        Nullability isNullable = new Nullability();
        assertFalse(isNullable.apply(Empty.INSTANCE));
    }

    @Test
    void testEpsilon() {
        Nullability isNullable = new Nullability();
        assertTrue(isNullable.apply(new Epsilon()));
    }

    @Test
    void testToken() {
        Nullability isNullable = new Nullability();
        assertFalse(isNullable.apply(new Token<>(32)));
    }
    //t[42] | t[32] . isNullable
    @Test
    void testSumTokens() {
        Nullability isNullable = new Nullability();
        assertFalse(isNullable.apply(new Sum( new Token<>(32), new Token<>(42) ) ));
    }

    //t[42] | eps . isNullable
    @Test
    void testSumWithEpsilon() {
        Nullability isNullable = new Nullability();
        assertTrue(isNullable.apply(new Sum( new Token<>(32), new Epsilon() ) ));
    }

    //t[32] ∘ t[42] . isNullable
    @Test
    void testProductTokens() {
        Nullability isNullable = new Nullability();
        assertFalse(isNullable.apply(new Product( new Token<>(32), new Token<>(42) ) ));
    }

    //t[32] ∘ eps
    @Test
    void testProductWithEpsilon() {
        Nullability isNullable = new Nullability();
        assertFalse(isNullable.apply(new Product( new Token<>(32), new Epsilon() ) ));
    }

    ///S -> A B
    ///A -> a | epsilon
    ///B -> b | epsilon
    @Test
    void testGivenC() {
        Nullability isNullable = new Nullability();
        var rS = new Reference('S');
        var rA = new Reference('A');
        var rB = new Reference('B');
        var s = new Product(rA, rB);
        var a = new Sum(new Token<>('a'), new Epsilon());
        var b = new Sum(new Token<>('b'), new Epsilon());
        rS.target = s;
        rA.target = a;
        rB.target = b;
        assertTrue(isNullable.apply(s));
        assertTrue(isNullable.apply(a));
        assertTrue(isNullable.apply(b));
    }

    ///S -> A B
    ///A -> C | epsilon
    ///B -> D | epsilon
    ///C -> a A D
    ///D -> b B C
    @Test
    void testComplex() {
        Nullability isNullable = new Nullability();
        var rS = new Reference('S');
        var rA = new Reference('A');
        var rB = new Reference('B');
        var rC = new Reference('C');
        var rD = new Reference('D');
        var s  = new Product(rA, rB);
        var a  = new Sum (rC, new Epsilon());
        var b  = new Sum (rD, new Epsilon());
        var c  = new Product(new Token<>('a'), new Product(rA, rD));
        var d  = new Product(new Token<>('b'), new Product(rB, rC));
        rS.target = s;
        rA.target = a;
        rB.target = b;
        rC.target = c;
        rD.target = d;

        assertFalse(isNullable.apply(d));
        assertFalse(isNullable.apply(c));
        assertTrue(isNullable.apply(b));
        assertTrue(isNullable.apply(a));
        assertTrue(isNullable.apply(s));
    }
}
