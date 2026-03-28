package obp3.uslg.syntax;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SubstitutionTest {

    static Var v(String name) { return new Var(name); }
    static App a(String name, Term... args) { return new App(name, args); }

    @Nested
    class Empty {
        @Test
        void emptySubstitutionHasNoBindings() {
            var sub = Substitution.empty();
            assertTrue(sub.bindings().isEmpty());
        }

        @Test
        void emptySubstitutionReturnsVarItself() {
            var sub = Substitution.empty();
            assertEquals(v("X"), sub.get(v("X")));
        }

        @Test
        void emptyDoesNotContainAnyVar() {
            var sub = Substitution.empty();
            assertFalse(sub.contains(v("X")));
        }
    }

    @Nested
    class ExtendTests {
        @Test
        void extendAddsBinding() {
            var result = Substitution.empty().extend(v("X"), a("a"));
            assertTrue(result.isPresent());
            var sub = result.get();
            assertTrue(sub.contains(v("X")));
            assertEquals(a("a"), sub.get(v("X")));
        }

        @Test
        void extendReturnsNewSubstitution() {
            var original = Substitution.empty();
            var result = original.extend(v("X"), a("a"));
            assertTrue(result.isPresent());
            assertNotSame(original, result.get());
            // original unchanged
            assertTrue(original.bindings().isEmpty());
        }

        @Test
        void extendDoesNotMutateOriginal() {
            var original = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            int sizeBefore = original.bindings().size();
            var extended = original.extend(v("Y"), a("b"));
            assertTrue(extended.isPresent());
            // original still has only X
            assertEquals(sizeBefore, original.bindings().size());
            assertFalse(original.contains(v("Y")));
            // extended has both
            assertTrue(extended.get().contains(v("X")));
            assertTrue(extended.get().contains(v("Y")));
        }

        @Test
        void extendFailsOnDirectOccursCheck() {
            // X = f(X) should fail
            var result = Substitution.empty().extend(v("X"), a("f", v("X")));
            assertTrue(result.isEmpty());
        }

        @Test
        void extendFailsOnIndirectOccursCheck() {
            // X = f(Y), then Y = g(X) should fail
            var sub = Substitution.empty().extend(v("X"), a("f", v("Y"))).orElseThrow();
            var result = sub.extend(v("Y"), a("g", v("X")));
            assertTrue(result.isEmpty());
        }

        @Test
        void extendSucceedsWhenVarNotInTerm() {
            var result = Substitution.empty().extend(v("X"), a("f", v("Y"), a("a")));
            assertTrue(result.isPresent());
            assertEquals(a("f", v("Y"), a("a")), result.get().get(v("X")));
        }

        @Test
        void extendToAnotherVariable() {
            var result = Substitution.empty().extend(v("X"), v("Y"));
            assertTrue(result.isPresent());
            assertEquals(v("Y"), result.get().get(v("X")));
        }

        @Test
        void multipleExtends() {
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = s1.extend(v("Y"), a("b")).orElseThrow();
            var s3 = s2.extend(v("Z"), a("c")).orElseThrow();
            assertEquals(a("a"), s3.get(v("X")));
            assertEquals(a("b"), s3.get(v("Y")));
            assertEquals(a("c"), s3.get(v("Z")));
            // earlier substitutions unchanged
            assertEquals(1, s1.bindings().size());
            assertEquals(2, s2.bindings().size());
        }

        @Test
        void extendDoesNotMutateOnFailure() {
            var original = Substitution.empty().extend(v("X"), a("f", v("Y"))).orElseThrow();
            int sizeBefore = original.bindings().size();
            // This should fail (occurs check: Y = g(X) -> g(f(Y)))
            var result = original.extend(v("Y"), a("g", v("X")));
            assertTrue(result.isEmpty());
            assertEquals(sizeBefore, original.bindings().size());
        }
    }

    @Nested
    class GetTests {
        @Test
        void getBoundVariable() {
            var sub = Substitution.empty().extend(v("X"), a("hello")).orElseThrow();
            assertEquals(a("hello"), sub.get(v("X")));
        }

        @Test
        void getUnboundVariableReturnsItself() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            assertEquals(v("Y"), sub.get(v("Y")));
        }
    }

    @Nested
    class ContainsTests {
        @Test
        void containsBoundVariable() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            assertTrue(sub.contains(v("X")));
        }

        @Test
        void doesNotContainUnboundVariable() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            assertFalse(sub.contains(v("Y")));
        }
    }

    @Nested
    class ComposeTests {
        @Test
        void composeWithEmpty() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var composed = sub.compose(Substitution.empty());
            assertEquals(a("a"), composed.get(v("X")));
        }

        @Test
        void emptyComposeWithNonEmpty() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var composed = Substitution.empty().compose(sub);
            assertEquals(a("a"), composed.get(v("X")));
        }

        @Test
        void composeAppliesFirstToSecondTerms() {
            // s1 = {X -> a}, s2 = {Y -> f(X)}
            // compose should give {X -> a, Y -> f(a)}
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = Substitution.empty().extend(v("Y"), a("f", v("X"))).orElseThrow();
            var composed = s1.compose(s2);
            assertEquals(a("a"), composed.get(v("X")));
            assertEquals(a("f", a("a")), composed.get(v("Y")));
        }

        @Test
        void composeOverridesWithThisBindings() {
            // s1 = {X -> a}, s2 = {X -> b}
            // s1.compose(s2): this (s1) wins for shared keys
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = Substitution.empty().extend(v("X"), a("b")).orElseThrow();
            var composed = s1.compose(s2);
            assertEquals(a("a"), composed.get(v("X")));
        }

        @Test
        void composeMergesDisjointBindings() {
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = Substitution.empty().extend(v("Y"), a("b")).orElseThrow();
            var composed = s1.compose(s2);
            assertEquals(a("a"), composed.get(v("X")));
            assertEquals(a("b"), composed.get(v("Y")));
        }

        @Test
        void composeDoesNotMutateOriginals() {
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = Substitution.empty().extend(v("Y"), a("b")).orElseThrow();
            s1.compose(s2);
            assertEquals(1, s1.bindings().size());
            assertEquals(1, s2.bindings().size());
        }
    }

    @Nested
    class ProjectTests {
        @Test
        void projectKeepsOnlyRequestedVars() {
            var sub = Substitution.empty()
                    .extend(v("X"), a("a")).orElseThrow()
                    .extend(v("Y"), a("b")).orElseThrow()
                    .extend(v("Z"), a("c")).orElseThrow();
            var projected = sub.project(Set.of(v("X"), v("Z")));
            assertTrue(projected.contains(v("X")));
            assertFalse(projected.contains(v("Y")));
            assertTrue(projected.contains(v("Z")));
            assertEquals(a("a"), projected.get(v("X")));
            assertEquals(a("c"), projected.get(v("Z")));
        }

        @Test
        void projectEmptySetReturnsEmptySubstitution() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var projected = sub.project(Set.of());
            assertTrue(projected.bindings().isEmpty());
        }

        @Test
        void projectMissingVarsAreIgnored() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var projected = sub.project(Set.of(v("X"), v("Y")));
            assertTrue(projected.contains(v("X")));
            assertFalse(projected.contains(v("Y")));
            assertEquals(1, projected.bindings().size());
        }

        @Test
        void projectDoesNotMutateOriginal() {
            var sub = Substitution.empty()
                    .extend(v("X"), a("a")).orElseThrow()
                    .extend(v("Y"), a("b")).orElseThrow();
            var projected = sub.project(Set.of(v("X")));
            assertEquals(2, sub.bindings().size());
            assertEquals(1, projected.bindings().size());
        }

        @Test
        void projectOnEmptySubstitution() {
            var projected = Substitution.empty().project(Set.of(v("X")));
            assertTrue(projected.bindings().isEmpty());
        }

        @Test
        void projectAllVarsReturnsCopy() {
            var sub = Substitution.empty()
                    .extend(v("X"), a("a")).orElseThrow()
                    .extend(v("Y"), a("b")).orElseThrow();
            var projected = sub.project(Set.of(v("X"), v("Y")));
            assertEquals(sub, projected);
            assertNotSame(sub, projected);
        }
    }

    @Nested
    class EqualityAndHashCode {
        @Test
        void equalSubstitutionsAreEqual() {
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            assertEquals(s1, s2);
            assertEquals(s1.hashCode(), s2.hashCode());
        }

        @Test
        void differentSubstitutionsAreNotEqual() {
            var s1 = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var s2 = Substitution.empty().extend(v("X"), a("b")).orElseThrow();
            assertNotEquals(s1, s2);
        }

        @Test
        void emptySubstitutionsAreEqual() {
            assertEquals(Substitution.empty(), Substitution.empty());
        }

        @Test
        void substitutionNotEqualToNull() {
            assertNotEquals(null, Substitution.empty());
        }

        @Test
        void substitutionNotEqualToDifferentType() {
            assertNotEquals("not a substitution", Substitution.empty());
        }
    }

    @Nested
    class WalkingGet {
        @Test
        void walksTwoStepChain() {
            // {X -> Y, Y -> a}  =>  get(X) = a
            var sub = Substitution.empty()
                    .extend(v("X"), v("Y")).orElseThrow()
                    .extend(v("Y"), a("a")).orElseThrow();
            assertEquals(a("a"), sub.get(v("X")));
        }

        @Test
        void walksThreeStepChain() {
            // {X -> Y, Y -> Z, Z -> a}
            var sub = Substitution.empty()
                    .extend(v("X"), v("Y")).orElseThrow()
                    .extend(v("Y"), v("Z")).orElseThrow()
                    .extend(v("Z"), a("a")).orElseThrow();
            assertEquals(a("a"), sub.get(v("X")));
            assertEquals(a("a"), sub.get(v("Y")));
            assertEquals(a("a"), sub.get(v("Z")));
        }

        @Test
        void stopsAtUnboundVar() {
            // {X -> Y}  where Y is unbound  =>  get(X) = Y
            var sub = Substitution.empty()
                    .extend(v("X"), v("Y")).orElseThrow();
            assertEquals(v("Y"), sub.get(v("X")));
        }

        @Test
        void stopsAtCompoundTerm() {
            // {X -> Y, Y -> f(Z)}  =>  get(X) = f(Z), not walking inside f
            var sub = Substitution.empty()
                    .extend(v("X"), v("Y")).orElseThrow()
                    .extend(v("Y"), a("f", v("Z"))).orElseThrow();
            assertEquals(a("f", v("Z")), sub.get(v("X")));
        }

        @Test
        void directBindingStillWorks() {
            // {X -> a}  =>  get(X) = a (no chain)
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            assertEquals(a("a"), sub.get(v("X")));
        }

        @Test
        void unboundVarReturnsItself() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            assertEquals(v("W"), sub.get(v("W")));
        }

        @Test
        void substituteWithWalkingGetResolvesInOnePass() {
            // {X -> Y, Y -> a}  =>  f(X) substituted once gives f(a)
            var sub = Substitution.empty()
                    .extend(v("X"), v("Y")).orElseThrow()
                    .extend(v("Y"), a("a")).orElseThrow();
            var result = a("f", v("X")).substitute(sub::get);
            assertEquals(a("f", a("a")), result);
        }

        @Test
        void longChainResolvesCompletely() {
            // V0 -> V1 -> V2 -> V3 -> V4 -> done
            var sub = Substitution.empty()
                    .extend(v("V0"), v("V1")).orElseThrow()
                    .extend(v("V1"), v("V2")).orElseThrow()
                    .extend(v("V2"), v("V3")).orElseThrow()
                    .extend(v("V3"), v("V4")).orElseThrow()
                    .extend(v("V4"), a("done")).orElseThrow();
            assertEquals(a("done"), sub.get(v("V0")));
        }
    }

    @Nested
    class EdgeCases {
        @Test
        void extendWithConstantTerm() {
            var result = Substitution.empty().extend(v("X"), a("constant"));
            assertTrue(result.isPresent());
        }

        @Test
        void extendVarToItself() {
            // X -> X should not trigger occurs check (Var.occursIn should handle this)
            // The result depends on implementation: Var.substitute returns itself,
            // so occursIn may detect X in substitution(X)=X 
            // This is a degenerate case — just document what happens
            var result = Substitution.empty().extend(v("X"), v("X"));
            // Either it succeeds (X=X is a no-op) or fails (strict occurs check)
            // The current implementation: occursIn walks the term via substitute;
            // since X is not bound, substitute returns X, so occursIn returns true => fail
            assertTrue(result.isEmpty());
        }

        @Test
        void composeWithSelf() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            var composed = sub.compose(sub);
            assertEquals(a("a"), composed.get(v("X")));
        }
    }
}
