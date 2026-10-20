package obp3.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PeekableIteratorTest {

    private static PeekableIterator<String> of(String... values) {
        return new PeekableIterator<>(Arrays.asList(values).iterator());
    }

    @Test
    void peekedNullIsReturnedByNextAndNextElementIsKept() {
        var it = of(null, "next");
        assertNull(it.peek());
        assertTrue(it.hasNext());
        assertNull(it.next());
        assertTrue(it.hasNext());
        assertEquals("next", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void peekedNullKeepsHasNextTrueForLastElement() {
        var it = of((String) null);
        assertNull(it.peek());
        assertTrue(it.hasNext());
        assertNull(it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void repeatedPeekDoesNotAdvance() {
        var it = of(null, "a");
        assertNull(it.peek());
        assertNull(it.peek());
        assertNull(it.next());
        assertEquals("a", it.peek());
        assertEquals("a", it.next());
    }

    @Test
    void nonNullBehaviourIsUnchanged() {
        var it = of("a", "b");
        assertEquals("a", it.peek());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
        assertThrows(NoSuchElementException.class, it::peek);
    }
}
