package obp3.uslg.syntax;

import obp3.unification.syntax.App;
import obp3.unification.syntax.Term;
import obp3.unification.syntax.Var;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UnifierTest {

    // Convenience helpers
    static Var v(String name) { return new Var(name); }
    static App a(String name, Term... args) { return new App(name, args); }

    @Nested
    class IdenticalTerms {
        @Test
        void identicalConstants() {
            var result = Unifier.unify(a("a"), a("a"), Substitution.empty());
            assertTrue(result.isPresent());
            assertTrue(result.get().bindings().isEmpty());
        }

        @Test
        void identicalCompoundTerms() {
            var t = a("f", a("a"), a("b"));
            var result = Unifier.unify(t, t, Substitution.empty());
            assertTrue(result.isPresent());
        }

        @Test
        void identicalVariables() {
            var result = Unifier.unify(v("X"), v("X"), Substitution.empty());
            assertTrue(result.isPresent());
        }

        @Test
        void identicalNestedStructure() {
            var t = a("f", a("g", a("a")), a("h", a("b")));
            var result = Unifier.unify(t, t, Substitution.empty());
            assertTrue(result.isPresent());
        }
    }

    @Nested
    class VariableToConstant {
        @Test
        void varUnifiesWithConstant() {
            var result = Unifier.unify(v("X"), a("a"), Substitution.empty());
            assertTrue(result.isPresent());
            assertEquals(a("a"), result.get().get(v("X")));
        }

        @Test
        void constantUnifiesWithVar() {
            var result = Unifier.unify(a("a"), v("X"), Substitution.empty());
            assertTrue(result.isPresent());
            assertEquals(a("a"), result.get().get(v("X")));
        }
    }

    @Nested
    class VariableToVariable {
        @Test
        void twoDistinctVariables() {
            var result = Unifier.unify(v("X"), v("Y"), Substitution.empty());
            assertTrue(result.isPresent());
            // One should be bound to the other
            var sub = result.get();
            assertEquals(sub.get(v("X")), sub.get(v("Y")));
        }
    }

    @Nested
    class VariableToCompound {
        @Test
        void varUnifiesWithCompound() {
            var term = a("f", a("a"), a("b"));
            var result = Unifier.unify(v("X"), term, Substitution.empty());
            assertTrue(result.isPresent());
            assertEquals(term, result.get().get(v("X")));
        }

        @Test
        void compoundUnifiesWithVar() {
            var term = a("f", a("a"), a("b"));
            var result = Unifier.unify(term, v("X"), Substitution.empty());
            assertTrue(result.isPresent());
            assertEquals(term, result.get().get(v("X")));
        }

        @Test
        void varUnifiesWithDeeplyNested() {
            var term = a("f", a("g", a("h", a("a"))));
            var result = Unifier.unify(v("X"), term, Substitution.empty());
            assertTrue(result.isPresent());
            assertEquals(term, result.get().get(v("X")));
        }
    }

    @Nested
    class CompoundTermDecomposition {
        @Test
        void matchingFunctorsAndArities() {
            // f(X, b) = f(a, Y)  =>  X=a, Y=b
            var result = Unifier.unify(
                    a("f", v("X"), a("b")),
                    a("f", a("a"), v("Y")),
                    Substitution.empty()
            );
            assertTrue(result.isPresent());
            var sub = result.get();
            assertEquals(a("a"), sub.get(v("X")));
            assertEquals(a("b"), sub.get(v("Y")));
        }

        @Test
        void nestedDecomposition() {
            // f(g(X), h(Y)) = f(g(a), h(b))  =>  X=a, Y=b
            var result = Unifier.unify(
                    a("f", a("g", v("X")), a("h", v("Y"))),
                    a("f", a("g", a("a")), a("h", a("b"))),
                    Substitution.empty()
            );
            assertTrue(result.isPresent());
            var sub = result.get();
            assertEquals(a("a"), sub.get(v("X")));
            assertEquals(a("b"), sub.get(v("Y")));
        }

        @Test
        void deepNesting() {
            // f(g(h(X))) = f(g(h(a)))
            var result = Unifier.unify(
                    a("f", a("g", a("h", v("X")))),
                    a("f", a("g", a("h", a("a")))),
                    Substitution.empty()
            );
            assertTrue(result.isPresent());
            assertEquals(a("a"), result.get().get(v("X")));
        }

        @Test
        void multipleArgumentsMultipleVars() {
            // f(X, Y, Z) = f(a, b, c)
            var result = Unifier.unify(
                    a("f", v("X"), v("Y"), v("Z")),
                    a("f", a("a"), a("b"), a("c")),
                    Substitution.empty()
            );
            assertTrue(result.isPresent());
            var sub = result.get();
            assertEquals(a("a"), sub.get(v("X")));
            assertEquals(a("b"), sub.get(v("Y")));
            assertEquals(a("c"), sub.get(v("Z")));
        }

        @Test
        void sharedVariableInArguments() {
            // f(X, X) = f(a, a)  =>  X=a
            var result = Unifier.unify(
                    a("f", v("X"), v("X")),
                    a("f", a("a"), a("a")),
                    Substitution.empty()
            );
            assertTrue(result.isPresent());
            assertEquals(a("a"), result.get().get(v("X")));
        }

        @Test
        void sharedVariableConflict() {
            // f(X, X) = f(a, b)  =>  FAIL (X can't be both a and b)
            var result = Unifier.unify(
                    a("f", v("X"), v("X")),
                    a("f", a("a"), a("b")),
                    Substitution.empty()
            );
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Failures {
        @Test
        void differentConstants() {
            var result = Unifier.unify(a("a"), a("b"), Substitution.empty());
            assertTrue(result.isEmpty());
        }

        @Test
        void differentFunctors() {
            var result = Unifier.unify(a("f", a("a")), a("g", a("a")), Substitution.empty());
            assertTrue(result.isEmpty());
        }

        @Test
        void differentArities() {
            var result = Unifier.unify(
                    a("f", a("a")),
                    a("f", a("a"), a("b")),
                    Substitution.empty()
            );
            assertTrue(result.isEmpty());
        }

        @Test
        void conflictInNestedStructure() {
            // f(g(a), X) = f(g(b), c) => FAIL because g(a) != g(b)
            var result = Unifier.unify(
                    a("f", a("g", a("a")), v("X")),
                    a("f", a("g", a("b")), a("c")),
                    Substitution.empty()
            );
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class OccursCheck {
        @Test
        void directOccursCheck() {
            // X = f(X)  =>  FAIL (infinite term)
            var result = Unifier.unify(v("X"), a("f", v("X")), Substitution.empty());
            assertTrue(result.isEmpty());
        }

        @Test
        void indirectOccursCheck() {
            // X = f(g(X))  =>  FAIL
            var result = Unifier.unify(v("X"), a("f", a("g", v("X"))), Substitution.empty());
            assertTrue(result.isEmpty());
        }

        @Test
        void occursCheckThroughSubstitution() {
            // Given X=f(Y), unify Y=g(X) should fail (Y -> g(X) -> g(f(Y)) is circular)
            var sub = Substitution.empty().extend(v("X"), a("f", v("Y")));
            assertTrue(sub.isPresent());
            var result = Unifier.unify(v("Y"), a("g", v("X")), sub.get());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class WithExistingSubstitution {
        @Test
        void priorBindingIsRespected() {
            // Given X=a, unify f(X) = f(a) => succeeds
            var sub = Substitution.empty().extend(v("X"), a("a"));
            assertTrue(sub.isPresent());
            var result = Unifier.unify(a("f", v("X")), a("f", a("a")), sub.get());
            assertTrue(result.isPresent());
        }

        @Test
        void priorBindingCausesConflict() {
            // Given X=a, unify X = b => fails
            var sub = Substitution.empty().extend(v("X"), a("a"));
            assertTrue(sub.isPresent());
            var result = Unifier.unify(v("X"), a("b"), sub.get());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class Immutability {
        @Test
        void unifyDoesNotMutateOriginalSubstitution() {
            var original = Substitution.empty();
            var result = Unifier.unify(v("X"), a("a"), original);
            assertTrue(result.isPresent());
            // original must be unchanged
            assertTrue(original.bindings().isEmpty());
            assertNotSame(original, result.get());
        }

        @Test
        void failedUnifyDoesNotMutateSubstitution() {
            var sub = Substitution.empty().extend(v("X"), a("a")).orElseThrow();
            int sizeBefore = sub.bindings().size();
            // unify X=b should fail since X=a already
            var result = Unifier.unify(v("X"), a("b"), sub);
            assertTrue(result.isEmpty());
            assertEquals(sizeBefore, sub.bindings().size());
        }

        @Test
        void multiArgUnifyDoesNotMutateOnPartialFailure() {
            var original = Substitution.empty();
            // f(X, X) = f(a, b)  — first arg succeeds (X=a) but second fails (X!=b)
            var result = Unifier.unify(
                    a("f", v("X"), v("X")),
                    a("f", a("a"), a("b")),
                    original
            );
            assertTrue(result.isEmpty());
            // original must still be empty
            assertTrue(original.bindings().isEmpty());
        }
    }

    @Nested
    class VariableChains {
        @Test
        void transitiveBinding() {
            // X=Y, Y=a  =>  walking get resolves X all the way to a
            var r1 = Unifier.unify(v("X"), v("Y"), Substitution.empty());
            assertTrue(r1.isPresent());
            var r2 = Unifier.unify(v("Y"), a("a"), r1.get());
            assertTrue(r2.isPresent());
            assertEquals(a("a"), r2.get().get(v("X")));
            assertEquals(a("a"), r2.get().get(v("Y")));
        }

        @Test
        void threeVariableChain() {
            // X=Y, Y=Z, Z=a  =>  all resolve to a in one get
            var s1 = Unifier.unify(v("X"), v("Y"), Substitution.empty()).orElseThrow();
            var s2 = Unifier.unify(v("Y"), v("Z"), s1).orElseThrow();
            var s3 = Unifier.unify(v("Z"), a("a"), s2).orElseThrow();
            assertEquals(a("a"), s3.get(v("X")));
            assertEquals(a("a"), s3.get(v("Y")));
            assertEquals(a("a"), s3.get(v("Z")));
        }

        @Test
        void singleSubstituteFullyResolvesChain() {
            // With walking get, a single substitute call resolves the full chain
            var s1 = Unifier.unify(v("X"), v("Y"), Substitution.empty()).orElseThrow();
            var s2 = Unifier.unify(v("Y"), a("a"), s1).orElseThrow();
            var resolved = a("f", v("X")).substitute(s2::get);
            assertEquals(a("f", a("a")), resolved);
        }
    }

    @Nested
    class ZeroArityFunctions {
        @Test
        void zeroArityConstantsMatch() {
            var result = Unifier.unify(a("foo"), a("foo"), Substitution.empty());
            assertTrue(result.isPresent());
        }

        @Test
        void zeroArityConstantsClash() {
            var result = Unifier.unify(a("foo"), a("bar"), Substitution.empty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class SymmetryTests {
        @Test
        void unificationIsSymmetric() {
            var t1 = a("f", v("X"), a("b"));
            var t2 = a("f", a("a"), v("Y"));
            var r1 = Unifier.unify(t1, t2, Substitution.empty());
            var r2 = Unifier.unify(t2, t1, Substitution.empty());
            assertTrue(r1.isPresent());
            assertTrue(r2.isPresent());
            // Both should bind X=a, Y=b
            assertEquals(a("a"), r1.get().get(v("X")));
            assertEquals(a("b"), r1.get().get(v("Y")));
            assertEquals(a("a"), r2.get().get(v("X")));
            assertEquals(a("b"), r2.get().get(v("Y")));
        }
    }
}
