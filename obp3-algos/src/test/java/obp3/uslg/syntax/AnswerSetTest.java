package obp3.uslg.syntax;

import obp3.fixer.Lattice;
import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnswerSetTest {

    static Var v(String name) { return new Var(name); }
    static App a(String name, Term... args) { return new App(name, args); }

    static Substitution sub(String varName, Term term) {
        return Substitution.empty().extend(v(varName), term).orElseThrow();
    }

    @Nested
    class Construction {
        @Test
        void defaultConstructorCreatesEmpty() {
            var as = new AnswerSet();
            assertTrue(as.isEmpty());
            assertTrue(as.answers().isEmpty());
        }

        @Test
        void collectionConstructorCopiesElements() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var as = new AnswerSet(List.of(s1, s2));
            assertFalse(as.isEmpty());
            assertEquals(2, as.answers().size());
            assertTrue(as.contains(s1));
            assertTrue(as.contains(s2));
        }

        @Test
        void collectionConstructorDeduplicates() {
            var s = sub("X", a("a"));
            var as = new AnswerSet(List.of(s, s));
            assertEquals(1, as.answers().size());
        }
    }

    @Nested
    class AddTests {
        @Test
        void addToEmpty() {
            var s = sub("X", a("a"));
            var as = new AnswerSet().add(s);
            assertEquals(1, as.answers().size());
            assertTrue(as.contains(s));
        }

        @Test
        void addReturnsNewInstance() {
            var original = new AnswerSet();
            var s = sub("X", a("a"));
            var result = original.add(s);
            assertNotSame(original, result);
            assertTrue(original.isEmpty());
            assertFalse(result.isEmpty());
        }

        @Test
        void addDuplicateDoesNotIncrease() {
            var s = sub("X", a("a"));
            var as = new AnswerSet().add(s).add(s);
            assertEquals(1, as.answers().size());
        }

        @Test
        void addMultipleDistinct() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var as = new AnswerSet().add(s1).add(s2);
            assertEquals(2, as.answers().size());
            assertTrue(as.contains(s1));
            assertTrue(as.contains(s2));
        }
    }

    @Nested
    class UnionTests {
        @Test
        void unionOfTwoEmpty() {
            var result = new AnswerSet().union(new AnswerSet());
            assertTrue(result.isEmpty());
        }

        @Test
        void unionWithEmpty() {
            var s = sub("X", a("a"));
            var as = new AnswerSet().add(s);
            var result = as.union(new AnswerSet());
            assertEquals(1, result.answers().size());
            assertTrue(result.contains(s));
        }

        @Test
        void emptyUnionWithNonEmpty() {
            var s = sub("X", a("a"));
            var as = new AnswerSet().add(s);
            var result = new AnswerSet().union(as);
            assertEquals(1, result.answers().size());
            assertTrue(result.contains(s));
        }

        @Test
        void unionMergesDisjoint() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var a1 = new AnswerSet().add(s1);
            var a2 = new AnswerSet().add(s2);
            var result = a1.union(a2);
            assertEquals(2, result.answers().size());
            assertTrue(result.contains(s1));
            assertTrue(result.contains(s2));
        }

        @Test
        void unionDeduplicatesOverlap() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var a1 = new AnswerSet(List.of(s1, s2));
            var a2 = new AnswerSet(List.of(s1));
            var result = a1.union(a2);
            assertEquals(2, result.answers().size());
        }

        @Test
        void unionDoesNotMutateOriginals() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var a1 = new AnswerSet().add(s1);
            var a2 = new AnswerSet().add(s2);
            a1.union(a2);
            assertEquals(1, a1.answers().size());
            assertEquals(1, a2.answers().size());
        }
    }

    @Nested
    class ContainsTests {
        @Test
        void containsPresentSubstitution() {
            var s = sub("X", a("a"));
            var as = new AnswerSet().add(s);
            assertTrue(as.contains(s));
        }

        @Test
        void doesNotContainAbsentSubstitution() {
            var s1 = sub("X", a("a"));
            var s2 = sub("X", a("b"));
            var as = new AnswerSet().add(s1);
            assertFalse(as.contains(s2));
        }

        @Test
        void containsEqualSubstitution() {
            // Two separately created but equal substitutions
            var s1 = sub("X", a("a"));
            var s2 = sub("X", a("a"));
            var as = new AnswerSet().add(s1);
            assertTrue(as.contains(s2));
        }

        @Test
        void emptyDoesNotContainAnything() {
            assertFalse(new AnswerSet().contains(Substitution.empty()));
        }
    }

    @Nested
    class AnswersViewTests {
        @Test
        void answersReturnsUnmodifiableSet() {
            var s = sub("X", a("a"));
            var as = new AnswerSet().add(s);
            assertThrows(UnsupportedOperationException.class, () -> as.answers().add(Substitution.empty()));
        }

        @Test
        void answersReflectsContents() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var as = new AnswerSet(List.of(s1, s2));
            var answers = as.answers();
            assertEquals(2, answers.size());
            assertTrue(answers.contains(s1));
            assertTrue(answers.contains(s2));
        }
    }

    @Nested
    class IsEmptyTests {
        @Test
        void emptyIsEmpty() {
            assertTrue(new AnswerSet().isEmpty());
        }

        @Test
        void nonEmptyIsNotEmpty() {
            assertFalse(new AnswerSet().add(Substitution.empty()).isEmpty());
        }
    }

    @Nested
    class EqualityAndHashCode {
        @Test
        void equalAnswerSetsAreEqual() {
            var s1 = sub("X", a("a"));
            var a1 = new AnswerSet().add(s1);
            var a2 = new AnswerSet().add(s1);
            assertEquals(a1, a2);
            assertEquals(a1.hashCode(), a2.hashCode());
        }

        @Test
        void differentAnswerSetsAreNotEqual() {
            var a1 = new AnswerSet().add(sub("X", a("a")));
            var a2 = new AnswerSet().add(sub("X", a("b")));
            assertNotEquals(a1, a2);
        }

        @Test
        void emptyAnswerSetsAreEqual() {
            assertEquals(new AnswerSet(), new AnswerSet());
        }

        @Test
        void equalToSelf() {
            var as = new AnswerSet().add(sub("X", a("a")));
            assertEquals(as, as);
        }

        @Test
        void notEqualToNull() {
            assertNotEquals(null, new AnswerSet());
        }

        @Test
        void notEqualToDifferentType() {
            assertNotEquals("not an answer set", new AnswerSet());
        }

        @Test
        void orderDoesNotMatter() {
            var s1 = sub("X", a("a"));
            var s2 = sub("Y", a("b"));
            var a1 = new AnswerSet().add(s1).add(s2);
            var a2 = new AnswerSet().add(s2).add(s1);
            assertEquals(a1, a2);
        }
    }

    @Nested
    class ToStringTests {
        @Test
        void emptyToString() {
            assertEquals("{}", new AnswerSet().toString());
        }

        @Test
        void nonEmptyToStringContainsBraces() {
            var as = new AnswerSet().add(Substitution.empty());
            var str = as.toString();
            assertTrue(str.startsWith("{"));
            assertTrue(str.endsWith("}"));
        }
    }

    @Nested
    class LatticeTests {
        @Test
        void toLatticeBottomIsEmpty() {
            Lattice<AnswerSet> lattice = AnswerSet.toLattice();
            assertTrue(lattice.bottom().isEmpty());
        }

        @Test
        void toLatticeTopIsNull() {
            Lattice<AnswerSet> lattice = AnswerSet.toLattice();
            assertFalse(lattice.isMaximal(new AnswerSet()));
            assertFalse(lattice.isMaximal(new AnswerSet().add(Substitution.empty())));
        }

        @Test
        void toLatticeEqualityWorks() {
            Lattice<AnswerSet> lattice = AnswerSet.toLattice();
            var s = sub("X", a("a"));
            var a1 = new AnswerSet().add(s);
            var a2 = new AnswerSet().add(s);
            assertTrue(lattice.equality().test(a1, a2));
        }

        @Test
        void toLatticeEqualityDetectsDifference() {
            Lattice<AnswerSet> lattice = AnswerSet.toLattice();
            var a1 = new AnswerSet().add(sub("X", a("a")));
            var a2 = new AnswerSet().add(sub("X", a("b")));
            assertFalse(lattice.equality().test(a1, a2));
        }
    }
}
