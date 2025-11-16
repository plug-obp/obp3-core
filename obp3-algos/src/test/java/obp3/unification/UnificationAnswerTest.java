package obp3.unification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for UnificationAnswer monad.
 * Tests all monad laws, operations, and edge cases.
 */
@DisplayName("UnificationAnswer Monad Tests")
class UnificationAnswerTest {

    // ========== Factory Methods Tests ==========
    
    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethodsTests {
        
        @Test
        @DisplayName("of() creates Solution with non-null value")
        void testOfCreatesValidSolution() {
            var answer = UnificationAnswer.of("test");
            assertTrue(answer.hasSolution());
            assertEquals("test", answer.solution());
        }
        
        @Test
        @DisplayName("of() throws NPE for null value")
        void testOfRejectsNull() {
            assertThrows(NullPointerException.class, () -> 
                UnificationAnswer.of(null)
            );
        }
        
        @Test
        @DisplayName("failure() creates Failure with non-null reason")
        void testFailureCreatesValidFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            assertTrue(answer.isFailure());
            assertEquals("error", answer.failureReason());
        }
        
        @Test
        @DisplayName("failure() throws NPE for null reason")
        void testFailureRejectsNull() {
            assertThrows(NullPointerException.class, () -> 
                UnificationAnswer.<String>failure(null)
            );
        }
        
        @Test
        @DisplayName("unknown() creates Unknown singleton")
        void testUnknownCreatesSingleton() {
            var answer1 = UnificationAnswer.<String>unknown();
            var answer2 = UnificationAnswer.<String>unknown();
            assertTrue(answer1.isUnknown());
            assertTrue(answer2.isUnknown());
            assertEquals(answer1, answer2); // Singleton behavior
        }
    }
    
    // ========== Monad Laws Tests ==========
    
    @Nested
    @DisplayName("Monad Laws")
    class MonadLawsTests {
        
        @Test
        @DisplayName("Left Identity: of(a).flatMap(f) ≡ f(a)")
        void testLeftIdentity() {
            String value = "hello";
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.of(s.length());
            
            var left = UnificationAnswer.of(value).flatMap(f);
            var right = f.apply(value);
            
            assertEquals(right.solution(), left.solution());
        }
        
        @Test
        @DisplayName("Left Identity holds for failure case")
        void testLeftIdentityWithFailure() {
            String value = "test";
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.failure("error");
            
            var left = UnificationAnswer.of(value).flatMap(f);
            var right = f.apply(value);
            
            assertTrue(left.isFailure());
            assertTrue(right.isFailure());
            assertEquals(right.failureReason(), left.failureReason());
        }
        
        @Test
        @DisplayName("Right Identity: m.flatMap(of) ≡ m")
        void testRightIdentity() {
            var answer = UnificationAnswer.of(42);
            var result = answer.flatMap(UnificationAnswer::of);
            
            assertEquals(answer.solution(), result.solution());
        }
        
        @Test
        @DisplayName("Right Identity holds for Failure")
        void testRightIdentityWithFailure() {
            var answer = UnificationAnswer.<Integer>failure("error");
            var result = answer.flatMap(UnificationAnswer::of);
            
            assertTrue(result.isFailure());
            assertEquals(answer.failureReason(), result.failureReason());
        }
        
        @Test
        @DisplayName("Right Identity holds for Unknown")
        void testRightIdentityWithUnknown() {
            var answer = UnificationAnswer.<Integer>unknown();
            var result = answer.flatMap(UnificationAnswer::of);
            
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("Associativity: m.flatMap(f).flatMap(g) ≡ m.flatMap(x -> f(x).flatMap(g))")
        void testAssociativity() {
            var m = UnificationAnswer.of("hello");
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.of(s.length());
            Function<Integer, UnificationAnswer<String>> g = i -> UnificationAnswer.of("length:" + i);
            
            var left = m.flatMap(f).flatMap(g);
            var right = m.flatMap(x -> f.apply(x).flatMap(g));
            
            assertEquals(left.solution(), right.solution());
        }
        
        @Test
        @DisplayName("Associativity holds with failure in first function")
        void testAssociativityWithFailureInF() {
            var m = UnificationAnswer.of("hello");
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.failure("f-error");
            Function<Integer, UnificationAnswer<String>> g = i -> UnificationAnswer.of("length:" + i);
            
            var left = m.flatMap(f).flatMap(g);
            var right = m.flatMap(x -> f.apply(x).flatMap(g));
            
            assertTrue(left.isFailure());
            assertTrue(right.isFailure());
            assertEquals("f-error", left.failureReason());
            assertEquals("f-error", right.failureReason());
        }
        
        @Test
        @DisplayName("Associativity holds with failure in second function")
        void testAssociativityWithFailureInG() {
            var m = UnificationAnswer.of("hello");
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.of(s.length());
            Function<Integer, UnificationAnswer<String>> g = i -> UnificationAnswer.failure("g-error");
            
            var left = m.flatMap(f).flatMap(g);
            var right = m.flatMap(x -> f.apply(x).flatMap(g));
            
            assertTrue(left.isFailure());
            assertTrue(right.isFailure());
            assertEquals("g-error", left.failureReason());
            assertEquals("g-error", right.failureReason());
        }
        
        @Test
        @DisplayName("Associativity holds starting from Failure")
        void testAssociativityFromFailure() {
            var m = UnificationAnswer.<String>failure("initial-error");
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.of(s.length());
            Function<Integer, UnificationAnswer<String>> g = i -> UnificationAnswer.of("length:" + i);
            
            var left = m.flatMap(f).flatMap(g);
            var right = m.flatMap(x -> f.apply(x).flatMap(g));
            
            assertTrue(left.isFailure());
            assertTrue(right.isFailure());
            assertEquals("initial-error", left.failureReason());
            assertEquals("initial-error", right.failureReason());
        }
        
        @Test
        @DisplayName("Associativity holds starting from Unknown")
        void testAssociativityFromUnknown() {
            var m = UnificationAnswer.<String>unknown();
            Function<String, UnificationAnswer<Integer>> f = s -> UnificationAnswer.of(s.length());
            Function<Integer, UnificationAnswer<String>> g = i -> UnificationAnswer.of("length:" + i);
            
            var left = m.flatMap(f).flatMap(g);
            var right = m.flatMap(x -> f.apply(x).flatMap(g));
            
            assertTrue(left.isUnknown());
            assertTrue(right.isUnknown());
        }
    }
    
    // ========== Functor Laws Tests ==========
    
    @Nested
    @DisplayName("Functor Laws")
    class FunctorLawsTests {
        
        @Test
        @DisplayName("Identity: map(id) ≡ id")
        void testFunctorIdentity() {
            var answer = UnificationAnswer.of(42);
            var result = answer.map(x -> x);
            
            assertEquals(answer.solution(), result.solution());
        }
        
        @Test
        @DisplayName("Composition: map(f).map(g) ≡ map(g ∘ f)")
        void testFunctorComposition() {
            var answer = UnificationAnswer.of(5);
            Function<Integer, Integer> f = x -> x * 2;
            Function<Integer, Integer> g = x -> x + 3;
            
            var left = answer.map(f).map(g);
            var right = answer.map(x -> g.apply(f.apply(x)));
            
            assertEquals(left.solution(), right.solution());
        }
        
        @Test
        @DisplayName("Functor laws hold for Failure")
        void testFunctorLawsForFailure() {
            var answer = UnificationAnswer.<Integer>failure("error");
            var result = answer.map(x -> x * 2);
            
            assertTrue(result.isFailure());
            assertEquals("error", result.failureReason());
        }
        
        @Test
        @DisplayName("Functor laws hold for Unknown")
        void testFunctorLawsForUnknown() {
            var answer = UnificationAnswer.<Integer>unknown();
            var result = answer.map(x -> x * 2);
            
            assertTrue(result.isUnknown());
        }
    }
    
    // ========== Map Operation Tests ==========
    
    @Nested
    @DisplayName("Map Operation")
    class MapOperationTests {
        
        @Test
        @DisplayName("map() transforms Solution value")
        void testMapTransformsSolution() {
            var answer = UnificationAnswer.of("hello");
            var result = answer.map(String::length);
            
            assertTrue(result.hasSolution());
            assertEquals(5, result.solution());
        }
        
        @Test
        @DisplayName("map() propagates Failure unchanged")
        void testMapPropagatesFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            var result = answer.map(String::length);
            
            assertTrue(result.isFailure());
            assertEquals("error", result.failureReason());
        }
        
        @Test
        @DisplayName("map() propagates Unknown unchanged")
        void testMapPropagatesUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            var result = answer.map(String::length);
            
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("map() converts exception to Failure")
        void testMapConvertsExceptionToFailure() {
            var answer = UnificationAnswer.of("test");
            var result = answer.map(s -> {
                throw new RuntimeException("map error");
            });
            
            assertTrue(result.isFailure());
            assertTrue(result.failureReason() instanceof RuntimeException);
        }
        
        @Test
        @DisplayName("map() throws NPE for null function")
        void testMapRejectsNullFunction() {
            var answer = UnificationAnswer.of("test");
            assertThrows(NullPointerException.class, () -> 
                answer.map(null)
            );
        }
        
        @Test
        @DisplayName("map() can change type")
        void testMapChangesType() {
            var answer = UnificationAnswer.of(42);
            var result = answer.map(Object::toString);
            
            assertTrue(result.hasSolution());
            assertEquals("42", result.solution());
        }
    }
    
    // ========== FlatMap Operation Tests ==========
    
    @Nested
    @DisplayName("FlatMap Operation")
    class FlatMapOperationTests {
        
        @Test
        @DisplayName("flatMap() chains Solution computations")
        void testFlatMapChainsSolutions() {
            var answer = UnificationAnswer.of("hello");
            var result = answer.flatMap(s -> UnificationAnswer.of(s.length()));
            
            assertTrue(result.hasSolution());
            assertEquals(5, result.solution());
        }
        
        @Test
        @DisplayName("flatMap() propagates Failure from source")
        void testFlatMapPropagatesSourceFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            var result = answer.flatMap(s -> UnificationAnswer.of(s.length()));
            
            assertTrue(result.isFailure());
            assertEquals("error", result.failureReason());
        }
        
        @Test
        @DisplayName("flatMap() propagates Unknown from source")
        void testFlatMapPropagatesSourceUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            var result = answer.flatMap(s -> UnificationAnswer.of(s.length()));
            
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("flatMap() propagates Failure from function")
        void testFlatMapPropagatesFunctionFailure() {
            var answer = UnificationAnswer.of("hello");
            var result = answer.flatMap(s -> UnificationAnswer.<Integer>failure("function error"));
            
            assertTrue(result.isFailure());
            assertEquals("function error", result.failureReason());
        }
        
        @Test
        @DisplayName("flatMap() propagates Unknown from function")
        void testFlatMapPropagatesFunctionUnknown() {
            var answer = UnificationAnswer.of("hello");
            var result = answer.flatMap(s -> UnificationAnswer.<Integer>unknown());
            
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("flatMap() converts exception to Failure")
        void testFlatMapConvertsExceptionToFailure() {
            var answer = UnificationAnswer.of("test");
            var result = answer.flatMap(s -> {
                throw new RuntimeException("flatMap error");
            });
            
            assertTrue(result.isFailure());
            assertTrue(result.failureReason() instanceof RuntimeException);
        }
        
        @Test
        @DisplayName("flatMap() throws NPE for null function")
        void testFlatMapRejectsNullFunction() {
            var answer = UnificationAnswer.of("test");
            assertThrows(NullPointerException.class, () -> 
                answer.flatMap(null)
            );
        }
        
        @Test
        @DisplayName("flatMap() allows complex chaining")
        void testFlatMapComplexChaining() {
            var result = UnificationAnswer.of("hello")
                .flatMap(s -> UnificationAnswer.of(s.length()))
                .flatMap(len -> UnificationAnswer.of(len * 2))
                .flatMap(doubled -> UnificationAnswer.of("result:" + doubled));
            
            assertTrue(result.hasSolution());
            assertEquals("result:10", result.solution());
        }
    }
    
    // ========== Recovery Operations Tests ==========
    
    @Nested
    @DisplayName("Recovery Operations")
    class RecoveryOperationTests {
        
        @Test
        @DisplayName("recover() converts Failure to Solution")
        void testRecoverConvertsFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            var result = answer.recover(reason -> "recovered: " + reason);
            
            assertTrue(result.hasSolution());
            assertEquals("recovered: error", result.solution());
        }
        
        @Test
        @DisplayName("recover() preserves Solution")
        void testRecoverPreservesSolution() {
            var answer = UnificationAnswer.of("original");
            var result = answer.recover(reason -> "recovered");
            
            assertTrue(result.hasSolution());
            assertEquals("original", result.solution());
        }
        
        @Test
        @DisplayName("recover() preserves Unknown")
        void testRecoverPreservesUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            var result = answer.recover(reason -> "recovered");
            
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("recover() converts exception to Failure")
        void testRecoverConvertsException() {
            var answer = UnificationAnswer.<String>failure("error");
            var result = answer.recover(reason -> {
                throw new RuntimeException("recovery failed");
            });
            
            assertTrue(result.isFailure());
            assertTrue(result.failureReason() instanceof RuntimeException);
        }
        
        @Test
        @DisplayName("recoverUnknown() converts Unknown to Solution")
        void testRecoverUnknownConverts() {
            var answer = UnificationAnswer.<String>unknown();
            var result = answer.recoverUnknown(() -> "default");
            
            assertTrue(result.hasSolution());
            assertEquals("default", result.solution());
        }
        
        @Test
        @DisplayName("recoverUnknown() preserves Solution")
        void testRecoverUnknownPreservesSolution() {
            var answer = UnificationAnswer.of("original");
            var result = answer.recoverUnknown(() -> "default");
            
            assertTrue(result.hasSolution());
            assertEquals("original", result.solution());
        }
        
        @Test
        @DisplayName("recoverUnknown() preserves Failure")
        void testRecoverUnknownPreservesFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            var result = answer.recoverUnknown(() -> "default");
            
            assertTrue(result.isFailure());
            assertEquals("error", result.failureReason());
        }
        
        @Test
        @DisplayName("recoverUnknown() converts exception to Failure")
        void testRecoverUnknownConvertsException() {
            var answer = UnificationAnswer.<String>unknown();
            var result = answer.recoverUnknown(() -> {
                throw new RuntimeException("recovery failed");
            });
            
            assertTrue(result.isFailure());
            assertTrue(result.failureReason() instanceof RuntimeException);
        }
        
        @Test
        @DisplayName("mapFailure() transforms failure reason")
        void testMapFailureTransforms() {
            var answer = UnificationAnswer.<String>failure("original error");
            var result = answer.mapFailure(reason -> "mapped: " + reason);
            
            assertTrue(result.isFailure());
            assertEquals("mapped: original error", result.failureReason());
        }
        
        @Test
        @DisplayName("mapFailure() preserves Solution")
        void testMapFailurePreservesSolution() {
            var answer = UnificationAnswer.of("value");
            var result = answer.mapFailure(reason -> "mapped");
            
            assertTrue(result.hasSolution());
            assertEquals("value", result.solution());
        }
        
        @Test
        @DisplayName("mapFailure() preserves Unknown")
        void testMapFailurePreservesUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            var result = answer.mapFailure(reason -> "mapped");
            
            assertTrue(result.isUnknown());
        }
    }
    
    // ========== Extraction Operations Tests ==========
    
    @Nested
    @DisplayName("Extraction Operations")
    class ExtractionOperationTests {
        
        @Test
        @DisplayName("getOrElse() returns solution value")
        void testGetOrElseReturnsSolution() {
            var answer = UnificationAnswer.of("value");
            assertEquals("value", answer.solutionOrElse("fallback"));
        }
        
        @Test
        @DisplayName("getOrElse() returns fallback for Failure")
        void testGetOrElseReturnsFallbackForFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            assertEquals("fallback", answer.solutionOrElse("fallback"));
        }
        
        @Test
        @DisplayName("getOrElse() returns fallback for Unknown")
        void testGetOrElseReturnsFallbackForUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            assertEquals("fallback", answer.solutionOrElse("fallback"));
        }
        
        @Test
        @DisplayName("getOrElse(Supplier) returns solution value")
        void testGetOrElseSupplierReturnsSolution() {
            var answer = UnificationAnswer.of("value");
            assertEquals("value", answer.solutionOrElse(() -> "fallback"));
        }
        
        @Test
        @DisplayName("getOrElse(Supplier) returns supplied value for Failure")
        void testGetOrElseSupplierForFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            assertEquals("supplied", answer.solutionOrElse(() -> "supplied"));
        }
        
        @Test
        @DisplayName("getOrElse(Supplier) returns supplied value for Unknown")
        void testGetOrElseSupplierForUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            assertEquals("supplied", answer.solutionOrElse(() -> "supplied"));
        }
        
        @Test
        @DisplayName("getOrElse(Supplier) is lazy")
        void testGetOrElseSupplierIsLazy() {
            var answer = UnificationAnswer.of("value");
            var called = new AtomicInteger(0);
            answer.solutionOrElse(() -> {
                called.incrementAndGet();
                return "fallback";
            });
            assertEquals(0, called.get(), "Supplier should not be called for Solution");
        }
        
        @Test
        @DisplayName("toOptional() returns present for Solution")
        void testToOptionalForSolution() {
            var answer = UnificationAnswer.of("value");
            var optional = answer.toOptional();
            
            assertTrue(optional.isPresent());
            assertEquals("value", optional.get());
        }
        
        @Test
        @DisplayName("toOptional() returns empty for Failure")
        void testToOptionalForFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            var optional = answer.toOptional();
            
            assertTrue(optional.isEmpty());
        }
        
        @Test
        @DisplayName("toOptional() returns empty for Unknown")
        void testToOptionalForUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            var optional = answer.toOptional();
            
            assertTrue(optional.isEmpty());
        }
        
        @Test
        @DisplayName("solution() returns value for Solution")
        void testSolutionReturnsValue() {
            var answer = UnificationAnswer.of("value");
            assertEquals("value", answer.solution());
        }
        
        @Test
        @DisplayName("solution() throws for Failure")
        void testSolutionThrowsForFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            assertThrows(NoSuchElementException.class, answer::solution);
        }
        
        @Test
        @DisplayName("solution() throws for Unknown")
        void testSolutionThrowsForUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            assertThrows(NoSuchElementException.class, answer::solution);
        }
        
        @Test
        @DisplayName("failureReason() returns reason for Failure")
        void testFailureReasonReturnsReason() {
            var answer = UnificationAnswer.<String>failure("error");
            assertEquals("error", answer.failureReason());
        }
        
        @Test
        @DisplayName("failureReason() throws for Solution")
        void testFailureReasonThrowsForSolution() {
            var answer = UnificationAnswer.of("value");
            assertThrows(NoSuchElementException.class, answer::failureReason);
        }
        
        @Test
        @DisplayName("failureReason() throws for Unknown")
        void testFailureReasonThrowsForUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            assertThrows(NoSuchElementException.class, answer::failureReason);
        }
    }
    
    // ========== State Checks Tests ==========
    
    @Nested
    @DisplayName("State Checks")
    class StateChecksTests {
        
        @Test
        @DisplayName("Solution state checks")
        void testSolutionStateChecks() {
            var answer = UnificationAnswer.of("value");
            assertTrue(answer.hasSolution());
            assertFalse(answer.isFailure());
            assertFalse(answer.isUnknown());
        }
        
        @Test
        @DisplayName("Failure state checks")
        void testFailureStateChecks() {
            var answer = UnificationAnswer.<String>failure("error");
            assertFalse(answer.hasSolution());
            assertTrue(answer.isFailure());
            assertFalse(answer.isUnknown());
        }
        
        @Test
        @DisplayName("Unknown state checks")
        void testUnknownStateChecks() {
            var answer = UnificationAnswer.<String>unknown();
            assertFalse(answer.hasSolution());
            assertFalse(answer.isFailure());
            assertTrue(answer.isUnknown());
        }
    }
    
    // ========== Equality and ToString Tests ==========
    
    @Nested
    @DisplayName("Equality and ToString")
    class EqualityAndToStringTests {
        
        @Test
        @DisplayName("Solution equality")
        void testSolutionEquality() {
            var answer1 = UnificationAnswer.of("test");
            var answer2 = UnificationAnswer.of("test");
            
            assertEquals(answer1, answer2);
            assertEquals(answer1.hashCode(), answer2.hashCode());
        }
        
        @Test
        @DisplayName("Failure equality")
        void testFailureEquality() {
            var answer1 = UnificationAnswer.<String>failure("error");
            var answer2 = UnificationAnswer.<String>failure("error");
            
            assertEquals(answer1, answer2);
            assertEquals(answer1.hashCode(), answer2.hashCode());
        }
        
        @Test
        @DisplayName("Unknown equality")
        void testUnknownEquality() {
            var answer1 = UnificationAnswer.<String>unknown();
            var answer2 = UnificationAnswer.<Integer>unknown();
            
            assertEquals(answer1, answer2);
            assertEquals(answer1.hashCode(), answer2.hashCode());
        }
        
        @Test
        @DisplayName("Unknown toString()")
        void testUnknownToString() {
            var answer = UnificationAnswer.<String>unknown();
            assertEquals("Unknown", answer.toString());
        }
    }
    
    // ========== Integration Tests ==========
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Complex pipeline with all states")
        void testComplexPipeline() {
            // Start with solution
            var result1 = UnificationAnswer.of(10)
                .map(x -> x * 2)
                .flatMap(x -> UnificationAnswer.of(x + 5))
                .map(x -> "result:" + x);
            
            assertTrue(result1.hasSolution());
            assertEquals("result:25", result1.solution());
            
            // Failure propagation
            var result2 = UnificationAnswer.of(10)
                .flatMap(x -> UnificationAnswer.<Integer>failure("error"))
                .map(x -> x * 2)
                .recover(r -> -1);
            
            assertTrue(result2.hasSolution());
            assertEquals(-1, result2.solution());
            
            // Unknown propagation
            var result3 = UnificationAnswer.<Integer>unknown()
                .map(x -> x * 2)
                .flatMap(x -> UnificationAnswer.of(x + 5))
                .recoverUnknown(() -> 42);
            
            assertTrue(result3.hasSolution());
            assertEquals(42, result3.solution());
        }
        
        @Test
        @DisplayName("Nested flatMap operations")
        void testNestedFlatMap() {
            var result = UnificationAnswer.of(5)
                .flatMap(x -> 
                    UnificationAnswer.of(x * 2)
                        .flatMap(y -> 
                            UnificationAnswer.of(y + 3)
                                .flatMap(z -> UnificationAnswer.of("final:" + z))
                        )
                );
            
            assertTrue(result.hasSolution());
            assertEquals("final:13", result.solution());
        }
        
        @Test
        @DisplayName("Recovery chain")
        void testRecoveryChain() {
            var result = UnificationAnswer.<Integer>failure("initial error")
                .recover(r -> {
                    throw new RuntimeException("recovery failed");
                })
                .mapFailure(r -> "mapped: " + ((Exception)r).getMessage())
                .recover(r -> -999);
            
            assertTrue(result.hasSolution());
            assertEquals(-999, result.solution());
        }
        
        @Test
        @DisplayName("Short-circuit on first failure")
        void testShortCircuitOnFailure() {
            var counter = new AtomicInteger(0);
            
            var result = UnificationAnswer.of(1)
                .map(x -> {
                    counter.incrementAndGet();
                    return x * 2;
                })
                .flatMap(x -> UnificationAnswer.<Integer>failure("error"))
                .map(x -> {
                    counter.incrementAndGet(); // Should not execute
                    return x * 3;
                })
                .flatMap(x -> {
                    counter.incrementAndGet(); // Should not execute
                    return UnificationAnswer.of(x + 1);
                });
            
            assertTrue(result.isFailure());
            assertEquals(1, counter.get(), "Should short-circuit after failure");
        }
    }
    
    // ========== Meet Semi-Lattice Laws Tests ==========
    
    @Nested
    @DisplayName("Meet Semi-Lattice Laws")
    class MeetSemiLatticeLawsTests {
        
        @Test
        @DisplayName("Idempotency: x ⊓ x = x")
        void testMeetIdempotency() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            var unknown = UnificationAnswer.<String>unknown();
            
            assertEquals(solution, solution.meet(solution));
            assertEquals(failure, failure.meet(failure));
            assertEquals(unknown, unknown.meet(unknown));
        }
        
        @Test
        @DisplayName("Commutativity: x ⊓ y = y ⊓ x")
        void testMeetCommutativity() {
            var sol1 = UnificationAnswer.of("test");
            var sol2 = UnificationAnswer.of("other");
            var fail1 = UnificationAnswer.<String>failure("error1");
            var fail2 = UnificationAnswer.<String>failure("error2");
            var unknown = UnificationAnswer.<String>unknown();
            
            // Solution cases
            assertEquals(sol1.meet(sol2), sol2.meet(sol1));
            assertEquals(sol1.meet(fail1), fail1.meet(sol1));
            assertEquals(sol1.meet(unknown), unknown.meet(sol1));
            
            // Failure cases
            assertEquals(fail1.meet(fail2), fail2.meet(fail1));
            assertEquals(fail1.meet(unknown), unknown.meet(fail1));
            
            // All should be Unknown (bottom)
            assertTrue(sol1.meet(sol2).isUnknown());
            assertTrue(sol1.meet(fail1).isUnknown());
            assertTrue(fail1.meet(fail2).isUnknown());
        }
        
        @Test
        @DisplayName("Associativity: (x ⊓ y) ⊓ z = x ⊓ (y ⊓ z)")
        void testMeetAssociativity() {
            var sol1 = UnificationAnswer.of("test");
            var sol2 = UnificationAnswer.of("test"); // Same value
            var fail = UnificationAnswer.<String>failure("error");
            var unknown = UnificationAnswer.<String>unknown();
            
            // Test with solutions
            assertEquals(
                sol1.meet(sol2).meet(unknown),
                sol1.meet(sol2.meet(unknown))
            );
            
            // Test with mixed types
            assertEquals(
                sol1.meet(fail).meet(unknown),
                sol1.meet(fail.meet(unknown))
            );
            
            // Test with failures
            assertEquals(
                fail.meet(unknown).meet(sol1),
                fail.meet(unknown.meet(sol1))
            );
        }
        
        @Test
        @DisplayName("Bottom element: Unknown ⊓ x = Unknown")
        void testBottomElementAbsorbs() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            var unknown = UnificationAnswer.<String>unknown();
            
            assertTrue(unknown.meet(solution).isUnknown());
            assertTrue(unknown.meet(failure).isUnknown());
            assertTrue(unknown.meet(unknown).isUnknown());
            
            assertTrue(solution.meet(unknown).isUnknown());
            assertTrue(failure.meet(unknown).isUnknown());
        }
        
        @Test
        @DisplayName("Solution ⊓ Solution = Solution when equal")
        void testSolutionMeetEqual() {
            var sol1 = UnificationAnswer.of("test");
            var sol2 = UnificationAnswer.of("test");
            
            var result = sol1.meet(sol2);
            assertTrue(result.hasSolution());
            assertEquals("test", result.solution());
        }
        
        @Test
        @DisplayName("Solution ⊓ Solution = Unknown when different")
        void testSolutionMeetDifferent() {
            var sol1 = UnificationAnswer.of("test1");
            var sol2 = UnificationAnswer.of("test2");
            
            var result = sol1.meet(sol2);
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("Failure ⊓ Failure = Failure when equal")
        void testFailureMeetEqual() {
            var fail1 = UnificationAnswer.<String>failure("error");
            var fail2 = UnificationAnswer.<String>failure("error");
            
            var result = fail1.meet(fail2);
            assertTrue(result.isFailure());
            assertEquals("error", result.failureReason());
        }
        
        @Test
        @DisplayName("Failure ⊓ Failure = Unknown when different")
        void testFailureMeetDifferent() {
            var fail1 = UnificationAnswer.<String>failure("error1");
            var fail2 = UnificationAnswer.<String>failure("error2");
            
            var result = fail1.meet(fail2);
            assertTrue(result.isUnknown());
        }
        
        @Test
        @DisplayName("Solution ⊓ Failure = Unknown (incomparable)")
        void testSolutionFailureIncomparable() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            
            assertTrue(solution.meet(failure).isUnknown());
            assertTrue(failure.meet(solution).isUnknown());
        }
    }
    
    // ========== Partial Order Tests ==========
    
    @Nested
    @DisplayName("Partial Order (⊑) Properties")
    class PartialOrderTests {
        
        @Test
        @DisplayName("Reflexivity: x ⊑ x")
        void testReflexivity() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            var unknown = UnificationAnswer.<String>unknown();
            
            assertTrue(solution.lessOrEqual(solution));
            assertTrue(failure.lessOrEqual(failure));
            assertTrue(unknown.lessOrEqual(unknown));
        }
        
        @Test
        @DisplayName("Antisymmetry: x ⊑ y && y ⊑ x => x = y")
        void testAntisymmetry() {
            var sol1 = UnificationAnswer.of("test");
            var sol2 = UnificationAnswer.of("test");
            var fail1 = UnificationAnswer.<String>failure("error");
            var fail2 = UnificationAnswer.<String>failure("error");
            
            // If both lessOrEqual, they should be equal
            if (sol1.lessOrEqual(sol2) && sol2.lessOrEqual(sol1)) {
                assertEquals(sol1, sol2);
            }
            
            if (fail1.lessOrEqual(fail2) && fail2.lessOrEqual(fail1)) {
                assertEquals(fail1, fail2);
            }
        }
        
        @Test
        @DisplayName("Transitivity: x ⊑ y && y ⊑ z => x ⊑ z")
        void testTransitivity() {
            var unknown = UnificationAnswer.<String>unknown();
            var solution = UnificationAnswer.of("test");
            
            // Unknown ⊑ Solution (true)
            assertTrue(unknown.lessOrEqual(solution));
            
            // Solution ⊑ Solution (true, same value)
            assertTrue(solution.lessOrEqual(solution));
            
            // Therefore Unknown ⊑ Solution (transitivity holds)
            assertTrue(unknown.lessOrEqual(solution));
        }
        
        @Test
        @DisplayName("Unknown ⊑ x for any x (bottom)")
        void testUnknownIsBottom() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            var unknown = UnificationAnswer.<String>unknown();
            
            assertTrue(unknown.lessOrEqual(solution));
            assertTrue(unknown.lessOrEqual(failure));
            assertTrue(unknown.lessOrEqual(unknown));
        }
        
        @Test
        @DisplayName("Solution and Failure are incomparable")
        void testSolutionFailureIncomparable() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            
            assertFalse(solution.lessOrEqual(failure));
            assertFalse(failure.lessOrEqual(solution));
        }
        
        @Test
        @DisplayName("Only Unknown is ⊑ Unknown")
        void testNothingLessOrEqualToUnknown() {
            var solution = UnificationAnswer.of("test");
            var failure = UnificationAnswer.<String>failure("error");
            var unknown = UnificationAnswer.<String>unknown();
            
            assertFalse(solution.lessOrEqual(unknown));
            assertFalse(failure.lessOrEqual(unknown));
            assertTrue(unknown.lessOrEqual(unknown)); // Reflexivity
        }
        
        @Test
        @DisplayName("Solution ⊑ Solution only when equal")
        void testSolutionPartialOrder() {
            var sol1 = UnificationAnswer.of("test");
            var sol2 = UnificationAnswer.of("test");
            var sol3 = UnificationAnswer.of("other");
            
            assertTrue(sol1.lessOrEqual(sol2)); // Equal solutions
            assertFalse(sol1.lessOrEqual(sol3)); // Different solutions
        }
        
        @Test
        @DisplayName("Failure ⊑ Failure only when equal")
        void testFailurePartialOrder() {
            var fail1 = UnificationAnswer.<String>failure("error");
            var fail2 = UnificationAnswer.<String>failure("error");
            var fail3 = UnificationAnswer.<String>failure("other");
            
            assertTrue(fail1.lessOrEqual(fail2)); // Equal failures
            assertFalse(fail1.lessOrEqual(fail3)); // Different failures
        }
    }
    
    // ========== Lattice Integration Tests ==========
    
    @Nested
    @DisplayName("Lattice Integration")
    class LatticeIntegrationTests {
        
        @Test
        @DisplayName("bottom() returns Unknown")
        void testBottomFactory() {
            var bottom = UnificationAnswer.<String>bottom();
            assertTrue(bottom.isUnknown());
        }
        
        @Test
        @DisplayName("Meet respects partial order")
        void testMeetRespectsOrder() {
            var unknown = UnificationAnswer.<String>unknown();
            var solution = UnificationAnswer.of("test");
            
            // x ⊓ y ⊑ x and x ⊓ y ⊑ y
            var meet = solution.meet(unknown);
            assertTrue(meet.lessOrEqual(solution));
            assertTrue(meet.lessOrEqual(unknown));
        }
        
        @Test
        @DisplayName("Meet is greatest lower bound")
        void testMeetIsGreatestLowerBound() {
            var sol1 = UnificationAnswer.of("test");
            var sol2 = UnificationAnswer.of("test");
            var unknown = UnificationAnswer.<String>unknown();
            
            var meet = sol1.meet(sol2);
            
            // meet ⊑ sol1 and meet ⊑ sol2
            assertTrue(meet.lessOrEqual(sol1));
            assertTrue(meet.lessOrEqual(sol2));
            
            // For any z where z ⊑ sol1 and z ⊑ sol2, we have z ⊑ meet
            // Unknown is such a z
            assertTrue(unknown.lessOrEqual(meet));
        }
        
        @Test
        @DisplayName("Complex lattice operations chain")
        void testComplexLatticeChain() {
            var sol1 = UnificationAnswer.of("x");
            var sol2 = UnificationAnswer.of("x");
            var sol3 = UnificationAnswer.of("y");
            var fail = UnificationAnswer.<String>failure("error");
            
            // ((sol1 ⊓ sol2) ⊓ sol3) should be Unknown
            var result1 = sol1.meet(sol2).meet(sol3);
            assertTrue(result1.isUnknown());
            
            // (sol1 ⊓ fail) should be Unknown
            var result2 = sol1.meet(fail);
            assertTrue(result2.isUnknown());
            
            // All paths lead to Unknown when incompatible
            var result3 = sol1.meet(sol2).meet(fail);
            assertTrue(result3.isUnknown());
        }
    }
    
    // ========== Fold (Catamorphism) Tests ==========
    
    @Nested
    @DisplayName("Fold Operations")
    class FoldTests {
        
        @Test
        @DisplayName("fold() on Solution applies onSolution function")
        void testFoldOnSolution() {
            var answer = UnificationAnswer.of(42);
            
            String result = answer.fold(
                val -> "Success: " + val,
                reason -> "Failed: " + reason,
                () -> "Unknown"
            );
            
            assertEquals("Success: 42", result);
        }
        
        @Test
        @DisplayName("fold() on Failure applies onFailure function")
        void testFoldOnFailure() {
            var answer = UnificationAnswer.<Integer>failure("error");
            
            String result = answer.fold(
                val -> "Success: " + val,
                reason -> "Failed: " + reason,
                () -> "Unknown"
            );
            
            assertEquals("Failed: error", result);
        }
        
        @Test
        @DisplayName("fold() on Unknown applies onUnknown supplier")
        void testFoldOnUnknown() {
            var answer = UnificationAnswer.<Integer>unknown();
            
            String result = answer.fold(
                val -> "Success: " + val,
                reason -> "Failed: " + reason,
                () -> "Unknown"
            );
            
            assertEquals("Unknown", result);
        }
        
        @Test
        @DisplayName("fold() allows different result types")
        void testFoldWithDifferentResultType() {
            var answer = UnificationAnswer.of("test");
            
            Integer result = answer.fold(
                String::length,
                reason -> -1,
                () -> 0
            );
            
            assertEquals(4, result);
        }
        
        @Test
        @DisplayName("fold() is exhaustive - covers all cases")
        void testFoldExhaustive() {
            var solution = UnificationAnswer.of(10);
            var failure = UnificationAnswer.<Integer>failure("err");
            var unknown = UnificationAnswer.<Integer>unknown();
            
            var counter = new AtomicInteger(0);
            
            solution.fold(
                v -> { counter.incrementAndGet(); return null; },
                r -> { counter.incrementAndGet(); return null; },
                () -> { counter.incrementAndGet(); return null; }
            );
            assertEquals(1, counter.get(), "Solution should call only onSolution");
            
            counter.set(0);
            failure.fold(
                v -> { counter.incrementAndGet(); return null; },
                r -> { counter.incrementAndGet(); return null; },
                () -> { counter.incrementAndGet(); return null; }
            );
            assertEquals(1, counter.get(), "Failure should call only onFailure");
            
            counter.set(0);
            unknown.fold(
                v -> { counter.incrementAndGet(); return null; },
                r -> { counter.incrementAndGet(); return null; },
                () -> { counter.incrementAndGet(); return null; }
            );
            assertEquals(1, counter.get(), "Unknown should call only onUnknown");
        }
        
        @Test
        @DisplayName("fold() throws NPE if onSolution is null")
        void testFoldRejectsNullOnSolution() {
            var answer = UnificationAnswer.of("test");
            
            assertThrows(NullPointerException.class, () ->
                answer.fold(null, r -> "fail", () -> "unknown")
            );
        }
        
        @Test
        @DisplayName("fold() throws NPE if onFailure is null")
        void testFoldRejectsNullOnFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            
            assertThrows(NullPointerException.class, () ->
                answer.fold(s -> "success", null, () -> "unknown")
            );
        }
        
        @Test
        @DisplayName("fold() throws NPE if onUnknown is null")
        void testFoldRejectsNullOnUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            
            assertThrows(NullPointerException.class, () ->
                answer.fold(s -> "success", r -> "fail", null)
            );
        }
        
        @Test
        @DisplayName("fold() enables concise conditional logic")
        void testFoldConditionalLogic() {
            Function<UnificationAnswer<String>, String> describe = answer ->
                answer.fold(
                    val -> "Got value: " + val,
                    reason -> "Error: " + reason,
                    () -> "Not computed yet"
                );
            
            assertEquals("Got value: hello", describe.apply(UnificationAnswer.of("hello")));
            assertEquals("Error: oops", describe.apply(UnificationAnswer.failure("oops")));
            assertEquals("Not computed yet", describe.apply(UnificationAnswer.unknown()));
        }
        
        @Test
        @DisplayName("fold() can perform side effects")
        void testFoldSideEffects() {
            var sideEffect = new StringBuilder();
            
            UnificationAnswer.of("data").fold(
                val -> { sideEffect.append("solution:").append(val); return null; },
                reason -> { sideEffect.append("failure:").append(reason); return null; },
                () -> { sideEffect.append("unknown"); return null; }
            );
            
            assertEquals("solution:data", sideEffect.toString());
        }
        
        @Test
        @DisplayName("fold() composes with map")
        void testFoldComposesWithMap() {
            var answer = UnificationAnswer.of(5);
            
            String result = answer
                .map(x -> x * 2)
                .fold(
                    val -> "Result: " + val,
                    reason -> "Error",
                    () -> "None"
                );
            
            assertEquals("Result: 10", result);
        }
        
        @Test
        @DisplayName("fold() composes with flatMap")
        void testFoldComposesWithFlatMap() {
            var answer = UnificationAnswer.of(5);
            
            String result = answer
                .flatMap(x -> UnificationAnswer.of(x * 2))
                .fold(
                    val -> "Result: " + val,
                    reason -> "Error",
                    () -> "None"
                );
            
            assertEquals("Result: 10", result);
        }
    }
    
    // ========== Stream Operations Tests ==========
    
    @Nested
    @DisplayName("Stream Operations")
    class StreamTests {
        
        @Test
        @DisplayName("stream() on Solution returns single element stream")
        void testStreamOnSolution() {
            var answer = UnificationAnswer.of("test");
            
            var list = answer.stream().toList();
            
            assertEquals(1, list.size());
            assertEquals("test", list.get(0));
        }
        
        @Test
        @DisplayName("stream() on Failure returns empty stream")
        void testStreamOnFailure() {
            var answer = UnificationAnswer.<String>failure("error");
            
            var list = answer.stream().toList();
            
            assertTrue(list.isEmpty());
        }
        
        @Test
        @DisplayName("stream() on Unknown returns empty stream")
        void testStreamOnUnknown() {
            var answer = UnificationAnswer.<String>unknown();
            
            var list = answer.stream().toList();
            
            assertTrue(list.isEmpty());
        }
        
        @Test
        @DisplayName("stream() integrates with Stream API")
        void testStreamIntegration() {
            var answers = java.util.List.of(
                UnificationAnswer.of("a"),
                UnificationAnswer.<String>failure("error"),
                UnificationAnswer.of("b"),
                UnificationAnswer.<String>unknown(),
                UnificationAnswer.of("c")
            );
            
            var solutions = answers.stream()
                .flatMap(UnificationAnswer::stream)
                .toList();
            
            assertEquals(3, solutions.size());
            assertEquals(java.util.List.of("a", "b", "c"), solutions);
        }
        
        @Test
        @DisplayName("stream() can be used for filtering")
        void testStreamFiltering() {
            var answers = java.util.List.of(
                UnificationAnswer.of(1),
                UnificationAnswer.of(2),
                UnificationAnswer.<Integer>failure("error"),
                UnificationAnswer.of(3),
                UnificationAnswer.<Integer>unknown()
            );
            
            var evenSolutions = answers.stream()
                .flatMap(UnificationAnswer::stream)
                .filter(x -> x % 2 == 0)
                .toList();
            
            assertEquals(java.util.List.of(2), evenSolutions);
        }
        
        @Test
        @DisplayName("stream() can be mapped")
        void testStreamMapping() {
            var answer = UnificationAnswer.of(5);
            
            var result = answer.stream()
                .map(x -> x * 2)
                .findFirst();
            
            assertTrue(result.isPresent());
            assertEquals(10, result.get());
        }
        
        @Test
        @DisplayName("stream() supports flatMap")
        void testStreamFlatMap() {
            var answer = UnificationAnswer.of("hello");
            
            var chars = answer.stream()
                .flatMap(s -> s.chars().mapToObj(c -> (char) c))
                .toList();
            
            assertEquals(5, chars.size());
        }
        
        @Test
        @DisplayName("stream() count matches optional behavior")
        void testStreamCount() {
            assertEquals(1, UnificationAnswer.of("test").stream().count());
            assertEquals(0, UnificationAnswer.failure("error").stream().count());
            assertEquals(0, UnificationAnswer.unknown().stream().count());
        }
        
        @Test
        @DisplayName("stream() can be converted to iterator")
        void testStreamToIterator() {
            var answer = UnificationAnswer.of("data");
            
            var iterator = answer.stream().iterator();
            
            assertTrue(iterator.hasNext());
            assertEquals("data", iterator.next());
            assertFalse(iterator.hasNext());
        }
    }
}
