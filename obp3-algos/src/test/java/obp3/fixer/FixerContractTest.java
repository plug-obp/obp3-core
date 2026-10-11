package obp3.fixer;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class FixerContractTest {
    private static final Lattice<Integer> INTEGERS = new Lattice<>(0, Integer::equals);
    private static final Lattice<Boolean> BOOLEANS_WITHOUT_TOP = new Lattice<>(false, Boolean::equals);

    private record Key(String name) {}

    @Test void defaultKeysUseIdentityAndCompletedQueriesAreMemoized() {
        var first = new Key("same");
        var second = new Key("same");
        var evaluations = new AtomicInteger();
        var fixer = new Fixer<Key, Integer>((key, request) -> {
            evaluations.incrementAndGet();
            return key.name().length();
        }, INTEGERS);

        assertEquals(4, fixer.apply(first));
        assertEquals(4, fixer.apply(first));
        assertEquals(1, evaluations.get());
        assertEquals(4, fixer.apply(second));
        assertEquals(2, evaluations.get(), "Equal but nonidentical default keys denote separate variables");
    }

    @Test void explicitHashMapsUseStructuralKeysForDependenciesAndCompletedQueries() {
        var evaluations = new HashMap<String, Integer>();
        var fixer = new Fixer<Key, Boolean>((key, request) -> {
            evaluations.merge(key.name(), 1, Integer::sum);
            return switch (key.name()) {
                case "root" -> request.apply(new Key("child"));
                case "child" -> true;
                default -> throw new AssertionError(key);
            };
        }, Lattice.BooleanLattice, HashMap::new);

        assertTrue(fixer.apply(new Key("root")));
        var completedEvaluations = Map.copyOf(evaluations);
        assertTrue(fixer.apply(new Key("root")));
        assertTrue(fixer.apply(new Key("child")));
        assertEquals(completedEvaluations, evaluations);
        assertEquals(1, evaluations.get("child"));
    }

    @Test void selfAndMutualDependenciesReachFixedPointsIncludingBottomCycles() {
        var bottomCycle = new Fixer<String, Boolean>((key, request) ->
                request.apply(key.equals("a") ? "b" : "a"), BOOLEANS_WITHOUT_TOP, HashMap::new);
        assertFalse(bottomCycle.apply("a"));
        assertFalse(bottomCycle.apply("b"));

        var self = new Fixer<String, Boolean>((key, request) ->
                request.apply(key) | true, BOOLEANS_WITHOUT_TOP, HashMap::new);
        assertTrue(self.apply("self"));

        var mutual = new Fixer<String, Boolean>((key, request) -> switch (key) {
            case "a" -> request.apply("b");
            case "b" -> request.apply("a") | request.apply("seed");
            case "seed" -> true;
            default -> throw new AssertionError(key);
        }, BOOLEANS_WITHOUT_TOP, HashMap::new);
        assertTrue(mutual.apply("a"));
        assertTrue(mutual.apply("b"));
    }

    @Test void changedDependenciesAreDiscoveredAndTheirLaterValuesPropagate() {
        var queried = new ArrayList<String>();
        var fixer = new Fixer<String, Boolean>((key, request) -> switch (key) {
            case "root" -> {
                if (!request.apply("gate")) yield false;
                queried.add("new dependency");
                yield request.apply("new");
            }
            case "gate" -> request.apply("seed");
            case "seed", "new" -> true;
            default -> throw new AssertionError(key);
        }, BOOLEANS_WITHOUT_TOP, HashMap::new);

        assertTrue(fixer.apply("root"));
        assertFalse(queried.isEmpty());
        assertTrue(fixer.apply("new"));
    }

    @Test void removedDependenciesDoNotRescheduleAnUnchangedObserver() {
        var rootObservations = new ArrayList<List<Boolean>>();
        var fixer = new Fixer<String, Boolean>((key, request) -> switch (key) {
            case "root" -> {
                boolean gate = request.apply("gate");
                if (gate) {
                    rootObservations.add(List.of(true));
                    yield true;
                }
                boolean old = request.apply("old");
                rootObservations.add(List.of(false, old));
                yield old;
            }
            case "gate" -> request.apply("seed");
            case "seed", "tail4" -> true;
            case "old" -> request.apply("tail1");
            case "tail1" -> request.apply("tail2");
            case "tail2" -> request.apply("tail3");
            case "tail3" -> request.apply("tail4");
            default -> throw new AssertionError(key);
        }, BOOLEANS_WITHOUT_TOP, HashMap::new);

        assertTrue(fixer.apply("root"));
        assertTrue(fixer.apply("old"), "The removed dependency really changes to true");
        assertEquals(rootObservations.size(), new HashSet<>(rootObservations).size(),
                "An obsolete dependency must not trigger a redundant evaluation after it is removed");
    }

    @Test void multipleChangedChildrenDoNotQueueDuplicateObserverEvaluations() {
        var rootObservations = new ArrayList<List<Integer>>();
        var fixer = new Fixer<String, Integer>((key, request) -> {
            if (!key.equals("root")) return 1;
            int left = request.apply("left");
            int right = request.apply("right");
            rootObservations.add(List.of(left, right));
            return left + right;
        }, INTEGERS, HashMap::new);

        assertEquals(2, fixer.apply("root"));
        assertEquals(rootObservations.size(), new HashSet<>(rootObservations).size(),
                "The same observer inputs must not be evaluated twice because both children notified it");
    }

    @Test void equalButNonidenticalTopShortCircuitsSelfDependencies() {
        record Property(int value, String representation) {}
        var lattice = new Lattice<>(new Property(0, "bottom"), new Property(1, "top"),
                (left, right) -> left.value() == right.value());
        var evaluations = new AtomicInteger();
        var fixer = new Fixer<String, Property>((key, request) -> {
            evaluations.incrementAndGet();
            request.apply(key);
            return new Property(1, "RHS result");
        }, lattice, HashMap::new);

        assertEquals(1, fixer.apply("self").value());
        assertEquals(1, evaluations.get(), "A value equal to top must not retain a self dependency");
    }

    @Test void publicApplyRejectsReentrancyEvenForPreviouslyCachedQueries() {
        for (String nested : List.of("cached", "new")) {
            var owner = new AtomicReference<Fixer<String, Boolean>>();
            var fixer = new Fixer<String, Boolean>((key, request) -> {
                if (key.equals("outer")) {
                    assertThrows(IllegalStateException.class, () -> owner.get().apply(nested));
                    return request.apply("cached");
                }
                return true;
            }, Lattice.BooleanLattice, HashMap::new);
            owner.set(fixer);
            assertTrue(fixer.apply("cached"));
            assertTrue(fixer.apply("outer"), "A rejected nested call must not corrupt the owner run");
            assertTrue(fixer.apply("new"));
        }
    }

    @Test void concurrentPublicCallsFailFastIncludingCacheHits() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            if (key.equals("slow")) {
                entered.countDown();
                await(release);
            }
            return true;
        }, Lattice.BooleanLattice, HashMap::new);
        assertTrue(fixer.apply("cached"));

        var executor = Executors.newSingleThreadExecutor();
        try {
            var active = executor.submit(() -> fixer.apply("slow"));
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            try {
                assertThrows(IllegalStateException.class, () -> fixer.apply("cached"));
                assertThrows(IllegalStateException.class, () -> fixer.apply("new"));
            } finally {
                release.countDown();
            }
            assertTrue(active.get(5, TimeUnit.SECONDS));
            assertTrue(fixer.apply("new"));
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test void requestIsRestrictedToItsOwnerThreadWhileItsRhsIsActive() {
        var executor = Executors.newSingleThreadExecutor();
        try {
            var fixer = new Fixer<String, Boolean>((key, request) -> {
                if (key.equals("child")) return true;
                var foreignRequest = executor.submit(() -> request.apply("child"));
                var failure = assertThrows(ExecutionException.class,
                        () -> foreignRequest.get(5, TimeUnit.SECONDS));
                assertInstanceOf(IllegalStateException.class, failure.getCause());
                return request.apply("child");
            }, Lattice.BooleanLattice, HashMap::new);
            assertTrue(fixer.apply("root"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test void requestExpiresAtTheEndOfItsOwnRhsEvenDuringTheSameRun() {
        var saved = new AtomicReference<Function<String, Boolean>>();
        var fixer = new Fixer<String, Boolean>((key, request) -> switch (key) {
            case "root" -> request.apply("early") | request.apply("later");
            case "early" -> {
                saved.set(request);
                yield false;
            }
            case "later" -> {
                assertNotNull(saved.get());
                assertThrows(IllegalStateException.class, () -> saved.get().apply("early"));
                yield true;
            }
            default -> throw new AssertionError(key);
        }, Lattice.BooleanLattice, HashMap::new);

        assertTrue(fixer.apply("root"));
        assertThrows(IllegalStateException.class, () -> saved.get().apply("early"));
    }

    @Test void rhsFailureExpiresItsRequestAndRetryGetsAFreshRequest() {
        var saved = new AtomicReference<Function<String, Boolean>>();
        var fail = new AtomicBoolean(true);
        var sentinel = new IllegalArgumentException("RHS failed");
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            saved.set(request);
            if (fail.get()) throw sentinel;
            return true;
        }, Lattice.BooleanLattice, HashMap::new);

        assertSame(sentinel, assertThrows(IllegalArgumentException.class, () -> fixer.apply("root")));
        var stale = saved.get();
        assertThrows(IllegalStateException.class, () -> stale.apply("root"));
        fail.set(false);
        assertTrue(fixer.apply("root"));
        assertNotSame(stale, saved.get());
        assertThrows(IllegalStateException.class, () -> stale.apply("root"));
    }

    @Test void runtimeExceptionsAndErrorsDiscardNewTopValuesButPreservePriorCache() {
        for (Throwable sentinel : List.of(new IllegalArgumentException("RHS failed"), new AssertionError("RHS failed"))) {
            var fail = new AtomicBoolean(true);
            var evaluations = new HashMap<String, Integer>();
            var fixer = new Fixer<String, Boolean>((key, request) -> {
                evaluations.merge(key, 1, Integer::sum);
                return switch (key) {
                    case "stable" -> false;
                    case "root" -> request.apply("top") & request.apply("crash");
                    case "top" -> true;
                    case "crash" -> fail.get() ? failWith(sentinel) : true;
                    default -> throw new AssertionError(key);
                };
            }, Lattice.BooleanLattice, HashMap::new);

            assertFalse(fixer.apply("stable"));
            assertSame(sentinel, assertThrows(Throwable.class, () -> fixer.apply("root")));
            assertEquals(1, evaluations.get("top"), "The aborted run had already computed a top value");
            assertFalse(fixer.apply("stable"));
            assertEquals(1, evaluations.get("stable"), "Prior completed cache survives failure");
            assertTrue(fixer.apply("top"));
            assertEquals(2, evaluations.get("top"), "Top values from an aborted run must be recomputed");
            fail.set(false);
            assertTrue(fixer.apply("root"));
            assertTrue(fixer.apply("crash"));
        }
    }

    @Test void nullConstructorInputsAndKeysAreRejected() {
        assertThrows(NullPointerException.class, () -> new Fixer<String, Boolean>(null, Lattice.BooleanLattice));
        assertThrows(NullPointerException.class, () -> new Fixer<String, Boolean>((key, request) -> true, null));
        assertThrows(NullPointerException.class,
                () -> new Fixer<String, Boolean>((key, request) -> true, Lattice.BooleanLattice, null));
        assertThrows(NullPointerException.class,
                () -> new Fixer<String, Boolean>((key, request) -> true, Lattice.BooleanLattice, () -> null));
        var fixer = new Fixer<String, Boolean>((key, request) -> true, Lattice.BooleanLattice);
        assertThrows(NullPointerException.class, () -> fixer.apply(null));
        assertTrue(fixer.apply("valid"));
    }

    @Test void equalityFailureDiscardsNewResultsAndPreservesPreviouslyCompletedCache() {
        var interrupt = new AtomicBoolean(true);
        var equalityShouldFail = new AtomicBoolean(false);
        var evaluations = new HashMap<String, Integer>();
        var sentinel = new IllegalStateException("domain equality failed");
        var lattice = new Lattice<>(false, true, (left, right) -> {
            if (equalityShouldFail.get()) throw sentinel;
            return left.equals(right);
        });
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            evaluations.merge(key, 1, Integer::sum);
            return switch (key) {
                case "stable" -> false;
                case "root" -> request.apply("new") & request.apply("bad-equality");
                case "new" -> true;
                case "bad-equality" -> {
                    equalityShouldFail.set(interrupt.get());
                    yield true;
                }
                default -> throw new AssertionError(key);
            };
        }, lattice, HashMap::new);

        assertFalse(fixer.apply("stable"));
        assertSame(sentinel, assertThrows(IllegalStateException.class, () -> fixer.apply("root")));
        assertEquals(1, evaluations.get("new"), "One new dependency reached top before equality failed");
        equalityShouldFail.set(false);
        interrupt.set(false);
        assertFalse(fixer.apply("stable"));
        assertEquals(1, evaluations.get("stable"));
        assertTrue(fixer.apply("new"));
        assertEquals(2, evaluations.get("new"), "The failed run did not publish its earlier dependency");
        assertTrue(fixer.apply("root"));
    }

    @Test void nonemptyAndSharedPermanentMapsAreRejectedWithoutKeepingTheSolverActive() {
        assertThrows(IllegalArgumentException.class, () -> new Fixer<String, Boolean>(
                (key, request) -> true, Lattice.BooleanLattice,
                () -> new HashMap<>(Map.of("preexisting", true))));

        var permanent = new HashMap<Object, Object>();
        var reusePermanent = new AtomicBoolean(true);
        var evaluations = new AtomicInteger();
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            evaluations.incrementAndGet();
            return true;
        }, Lattice.BooleanLattice, () -> reusePermanent.get() ? permanent : new HashMap<>());

        assertThrows(IllegalArgumentException.class, () -> fixer.apply("root"));
        assertEquals(0, evaluations.get(), "A shared map must be rejected before evaluating equations");
        assertTrue(permanent.isEmpty());
        reusePermanent.set(false);
        assertTrue(fixer.apply("root"), "A rejected map factory must release the active-call gate");
        assertEquals(1, evaluations.get());
    }

    @Test void transientMapsAreClearedAfterSuccessFailureAndRetry() {
        var suppliedMaps = new ArrayList<Map<?, ?>>();
        var fail = new AtomicBoolean(true);
        var savedRequest = new AtomicReference<Function<String, Boolean>>();
        var sentinel = new IllegalArgumentException("RHS failed");
        var fixer = new Fixer<String, Boolean>((key, request) -> switch (key) {
            case "stable" -> false;
            case "root" -> request.apply("new") & request.apply("failure");
            case "new" -> true;
            case "failure" -> {
                savedRequest.set(request);
                if (fail.get()) throw sentinel;
                yield true;
            }
            default -> throw new AssertionError(key);
        }, Lattice.BooleanLattice, () -> {
            // Observe the supplied maps to check cleanup, without mutating them.
            var map = new HashMap<>();
            suppliedMaps.add(map);
            return map;
        });

        assertFalse(fixer.apply("stable"));
        assertFalse(suppliedMaps.getFirst().isEmpty(), "Completed cache remains available");
        assertTransientMapsEmpty(suppliedMaps);
        assertSame(sentinel, assertThrows(IllegalArgumentException.class, () -> fixer.apply("root")));
        assertTransientMapsEmpty(suppliedMaps);
        assertEquals(Map.of("stable", false), suppliedMaps.getFirst(), "Aborted values never enter the cache");
        var stale = savedRequest.get();
        assertThrows(IllegalStateException.class, () -> stale.apply("new"));
        fail.set(false);
        assertTrue(fixer.apply("root"));
        assertTransientMapsEmpty(suppliedMaps);
        assertEquals(Map.of("stable", false, "root", true, "new", true, "failure", true),
                suppliedMaps.getFirst());
        assertThrows(IllegalStateException.class, () -> stale.apply("new"));
    }

    @Test void nullDependencyAndRhsResultAbortWithoutPoisoningRetries() {
        for (boolean nullDependency : List.of(false, true)) {
            var invalid = new AtomicBoolean(true);
            var fixer = new Fixer<String, Boolean>((key, request) -> {
                if (invalid.get()) return nullDependency ? request.apply(null) : null;
                return true;
            }, Lattice.BooleanLattice);
            assertThrows(NullPointerException.class, () -> fixer.apply("root"));
            invalid.set(false);
            assertTrue(fixer.apply("root"));
        }
    }

    @Test void cancellationIsCheckedBeforeCacheHitsAndNullPredicateIsRejected() {
        var evaluations = new AtomicInteger();
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            evaluations.incrementAndGet();
            return true;
        }, Lattice.BooleanLattice);
        assertTrue(fixer.apply("cached"));
        assertThrows(NullPointerException.class, () -> fixer.apply("cached", null));
        assertThrows(CancellationException.class, () -> fixer.apply("cached", () -> true));
        assertTrue(fixer.apply("cached", () -> false));
        assertEquals(1, evaluations.get());
    }

    @Test void cooperativeCancellationStopsAnAscendingEquationAndRetryStartsAtBottom() {
        var evaluations = new AtomicInteger();
        var approximations = new ArrayList<Integer>();
        var saved = new AtomicReference<Function<String, Integer>>();
        var fixer = new Fixer<String, Integer>((key, request) -> {
            if (key.equals("stable")) return 1;
            evaluations.incrementAndGet();
            saved.set(request);
            int previous = request.apply(key);
            approximations.add(previous);
            return previous + 1;
        }, INTEGERS, HashMap::new);

        assertThrows(CancellationException.class, () -> fixer.apply("ascending", () -> evaluations.get() >= 8));
        assertEquals(8, evaluations.get(), "Cancellation cooperates at the RHS/request boundary, not after convergence");
        var stale = saved.get();
        assertThrows(IllegalStateException.class, () -> stale.apply("ascending"));
        assertEquals(1, fixer.apply("stable", () -> false));
        int retryStart = approximations.size();
        assertThrows(CancellationException.class, () -> fixer.apply("ascending", () -> evaluations.get() >= 12));
        assertTrue(approximations.size() > retryStart);
        assertEquals(0, approximations.get(retryStart), "Retry must not inherit a cancelled approximation");
        assertThrows(IllegalStateException.class, () -> stale.apply("ascending"));
    }

    @Test void cancellationAfterRhsDiscardsItsResultBeforePublication() {
        var cancelled = new AtomicBoolean(false);
        var evaluations = new AtomicInteger();
        var cancelInRhs = new AtomicBoolean(true);
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            evaluations.incrementAndGet();
            if (cancelInRhs.get()) cancelled.set(true);
            return true;
        }, Lattice.BooleanLattice);

        assertThrows(CancellationException.class, () -> fixer.apply("root", cancelled::get));
        cancelled.set(false);
        cancelInRhs.set(false);
        assertTrue(fixer.apply("root", cancelled::get));
        assertEquals(2, evaluations.get(), "The cancelled top result was not published");
    }

    @Test void observedCancellationStaysLatchedEvenIfTheRhsCatchesItAndClearsTheFlag() {
        var cancelled = new AtomicBoolean(false);
        var cancelAtRequest = new AtomicBoolean(true);
        var saved = new AtomicReference<Function<String, Boolean>>();
        var childEvaluations = new AtomicInteger();
        var fixer = new Fixer<String, Boolean>((key, request) -> {
            if (key.equals("child")) {
                childEvaluations.incrementAndGet();
                return true;
            }
            saved.set(request);
            if (cancelAtRequest.get()) {
                cancelled.set(true);
                assertThrows(CancellationException.class, () -> request.apply("child"));
                cancelled.set(false);
                return true;
            }
            return request.apply("child");
        }, Lattice.BooleanLattice, HashMap::new);

        assertThrows(CancellationException.class, () -> fixer.apply("root", cancelled::get));
        assertFalse(cancelled.get(), "The caller's cancellation flag was cleared inside its RHS");
        assertEquals(0, childEvaluations.get(), "Cancelled requests do not evaluate new dependencies");
        var stale = saved.get();
        assertThrows(IllegalStateException.class, () -> stale.apply("child"));
        cancelAtRequest.set(false);
        assertTrue(fixer.apply("root", cancelled::get));
        assertEquals(1, childEvaluations.get());
        assertThrows(IllegalStateException.class, () -> stale.apply("child"));
    }

    @Test void cancellationAndPredicateFailureDiscardEarlierNewTopValuesOnly() {
        for (boolean throwFromPredicate : List.of(false, true)) {
            var cancelled = new AtomicBoolean(false);
            var interrupt = new AtomicBoolean(true);
            var evaluations = new HashMap<String, Integer>();
            var sentinel = new IllegalArgumentException("cancellation predicate failed");
            var fixer = new Fixer<String, Boolean>((key, request) -> {
                evaluations.merge(key, 1, Integer::sum);
                return switch (key) {
                    case "stable" -> false;
                    case "root" -> request.apply("top") & request.apply("interrupt");
                    case "top" -> true;
                    case "interrupt" -> {
                        if (interrupt.get()) cancelled.set(true);
                        yield true;
                    }
                    default -> throw new AssertionError(key);
                };
            }, Lattice.BooleanLattice, HashMap::new);
            assertFalse(fixer.apply("stable"));

            var failure = assertThrows(RuntimeException.class, () -> fixer.apply("root", () -> {
                if (cancelled.get() && throwFromPredicate) throw sentinel;
                return cancelled.get();
            }));
            if (throwFromPredicate) assertSame(sentinel, failure);
            else assertInstanceOf(CancellationException.class, failure);
            assertEquals(1, evaluations.get("top"));
            assertFalse(fixer.apply("stable"));
            assertEquals(1, evaluations.get("stable"));
            assertTrue(fixer.apply("top"));
            assertEquals(2, evaluations.get("top"));
            interrupt.set(false);
            assertTrue(fixer.apply("root"));
        }
    }

    @Test void requestLifetimeChecksAlsoRunWithAssertionsDisabled() throws Exception {
        String classpath = Path.of(FixerContractTest.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                + File.pathSeparator
                + Path.of(Fixer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-da", "-cp", classpath, AssertionDisabledProbe.class.getName())
                .redirectErrorStream(true).start();
        try {
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "Assertion-disabled probe timed out");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), output);
        } finally {
            process.destroyForcibly();
        }
    }

    /** Independent process entry, deliberately free of JUnit dependencies. */
    public static final class AssertionDisabledProbe {
        public static void main(String[] arguments) {
            if (Fixer.class.desiredAssertionStatus()) throw new AssertionError("Assertions were not disabled");
            for (boolean fail : List.of(false, true)) {
                var saved = new AtomicReference<Function<String, Boolean>>();
                var fixer = new Fixer<String, Boolean>((key, request) -> {
                    saved.set(request);
                    if (fail) throw new IllegalArgumentException("failed RHS");
                    return false;
                }, Lattice.BooleanLattice);
                try {
                    fixer.apply("root");
                    if (fail) throw new AssertionError("Expected RHS failure");
                } catch (IllegalArgumentException failure) {
                    if (!fail) throw failure;
                }
                try {
                    saved.get().apply("child");
                    throw new AssertionError("Stale request was accepted without assertions");
                } catch (IllegalStateException expected) {
                    // Required runtime guard, independent of -ea/-da.
                }
            }
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for test coordination");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new AssertionError(failure);
        }
    }

    private static void assertTransientMapsEmpty(List<Map<?, ?>> suppliedMaps) {
        assertTrue(suppliedMaps.size() >= 2, "A run uses storage separate from its completed cache");
        for (int index = 1; index < suppliedMaps.size(); index++)
            assertTrue(suppliedMaps.get(index).isEmpty(), "Transient map " + index + " retained solved nodes");
    }

    private static <T> T failWith(Throwable failure) {
        if (failure instanceof Error error) throw error;
        throw (RuntimeException) failure;
    }
}
