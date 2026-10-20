package obp3.hashcons;

import obp3.hashcons.example.lambda.hashcons.nowrapper.DirectTermHashConsedFactory;
import obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Term;
import obp3.hashcons.example.lambda.hashcons.wrapper.TermHashConsed;
import obp3.hashcons.example.lambda.hashcons.wrapper.TermHashConsedFactory;
import obp3.utils.Hashable;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class HashConsArenaTest {
    record Node(String name) {}
    record Cons<T>(T node, Arena arena, int tag, int hashKey) implements HashConsed<T> {}

    private static HashConsTable<Node> table() {
        return new HashConsTable<>(Hashable.standard(), Cons<Node>::new);
    }

    @Test
    void distinctTablesHaveDistinctArenas() {
        assertNotSame(table().arena(), table().arena());
    }

    @Test
    void seededTableReinternsAndDoesNotShareAllocator() {
        var source = table();
        var a = source.hashCons(new Node("a"));
        var b = source.hashCons(new Node("b"));
        var copy = new HashConsTable<>(Hashable.standard(), Cons<Node>::new, source.map());
        assertNotSame(source.arena(), copy.arena());
        var ca = copy.hashCons(new Node("a"));
        var cb = copy.hashCons(new Node("b"));
        var cc = copy.hashCons(new Node("c"));
        assertSame(copy.arena(), ca.arena());
        assertNotSame(a, ca);
        assertEquals(3, new HashSet<>(java.util.List.of(ca.tag(), cb.tag(), cc.tag())).size());
        // the source keeps allocating independently, but its values stay distinct within it
        var sc = source.hashCons(new Node("c"));
        assertNotEquals(a.tag(), sc.tag());
        assertNotEquals(b.tag(), sc.tag());
        assertEquals(3, source.map().size());
    }

    @Test
    void mapViewIsReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> table().map().clear());
    }

    @Test
    void foreignInternedValueIsReinterned() {
        var t1 = table();
        var t2 = table();
        t2.hashCons(new Node("pad"));
        var foreign = t1.hashCons(new Node("x"));
        var local = t2.hashCons(foreign.node());
        assertSame(t2.arena(), local.arena());
        assertSame(local, t2.hashCons(new Node("x")));
        assertSame(local, t2.hashCons(local.node()));
        assertSame(t1.arena(), foreign.arena());
    }

    @Test
    void sameTagDifferentArenasAreNotEqual() {
        var f1 = new TermHashConsedFactory();
        var f2 = new TermHashConsedFactory();
        var x = f1.var(1);
        var y = f2.var(2);
        assertEquals(((TermHashConsed) x).tag(), ((TermHashConsed) y).tag());
        assertNotEquals(x, y);
        assertNotEquals(y, x);
    }

    @Test
    void structurallyEqualAcrossArenasHaveEqualHashCodes() {
        var x = new TermHashConsedFactory().app(new TermHashConsedFactory().var(1), new TermHashConsedFactory().var(2));
        var f = new TermHashConsedFactory();
        var y = f.app(f.var(1), f.var(2));
        assertNotSame(x, y);
        assertEquals(x, y);
        assertEquals(x.hashCode(), y.hashCode());
    }

    @Test
    void directTermsSameTagDifferentArenasAreNotEqual() {
        var x = new DirectTermHashConsedFactory().var(1);
        var y = new DirectTermHashConsedFactory().var(2);
        assertEquals(x.tag(), y.tag());
        assertNotEquals(x, y);
        var f = new DirectTermHashConsedFactory();
        assertEquals(x, f.var(1));
        assertEquals(x.hashCode(), f.var(1).hashCode());
    }

    @Test
    void directFactoryDoesNotMutateForeignTerms() {
        var f1 = new DirectTermHashConsedFactory();
        var f2 = new DirectTermHashConsedFactory();
        f2.var(7);
        Term x = f1.var(1);
        int tag = x.tag();
        Term moved = f2.hashConsed(x);
        assertEquals(tag, x.tag());
        assertSame(f1Arena(x), x.arena());
        assertNotSame(x, moved);
        assertNotSame(x.arena(), moved.arena());
    }

    private static Arena f1Arena(Term t) {
        return t.arena();
    }

    @Test
    void uninternedSentinelTagsUseOrdinaryEquality() {
        var a = new Uninterned("a");
        var b = new Uninterned("b");
        assertTrue(HashConsed.equalityFunction(a, new Uninterned("a")));
        assertFalse(HashConsed.equalityFunction(a, b));
    }

    record Uninterned(String node) implements HashConsed<String> {
        public int tag() { return -1; }
        public Arena arena() { return null; }
        public int hashKey() { return -1; }
        @Override public boolean isHashConsed() { return false; }
    }

    @Test
    void rawAndInternedDirectTermsAgreeOnEqualityAndHash() {
        var h = HashConsed.<Term>hashable();
        Term raw = new obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Var(1);
        Term interned = new DirectTermHashConsedFactory().var(1);
        assertFalse(raw.isHashConsed());
        assertNotEquals(-1, h.hash(raw));
        assertEquals(h.hash(raw), h.hash(interned));
        assertTrue(h.equal(raw, interned));
        assertTrue(h.equal(interned, raw));
    }

    @Test
    void equalImpliesEqualHashAcrossRawAndInterned() {
        var h = HashConsed.<Term>hashable();
        var f1 = new DirectTermHashConsedFactory();
        var f2 = new DirectTermHashConsedFactory();
        Term[] values = {
                new obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Var(1),
                new obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Var(2),
                f1.var(1), f1.var(2), f2.var(1), f2.var(2), f2.var(9), f1.lambda(f1.var(1))
        };
        for (var x : values) for (var y : values) {
            if (h.equal(x, y)) assertEquals(h.hash(x), h.hash(y), x + " vs " + y);
        }
    }

    @Test
    void linearScanSetFindsInternedValueWhenRawWasInserted() {
        var set = new obp3.datastructures.collections.linearscan.LinearScanHashSet<Term>(10, HashConsed.<Term>hashable());
        set.add(new obp3.hashcons.example.lambda.hashcons.nowrapper.syntax.Var(1));
        assertTrue(set.contains(new DirectTermHashConsedFactory().var(1)));
    }
}
