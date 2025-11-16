package obp3.unification;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A monadic type representing the result of a unification computation.
 * <p>
 * This type has three possible states:
 * <ul>
 *   <li>{@link Solution} - successful unification with a solution</li>
 *   <li>{@link Failure} - failed unification with a reason</li>
 *   <li>{@link Unknown} - unification not yet attempted or indeterminate (⊥ bottom)</li>
 * </ul>
 * <p>
 * <b>Meet Semi-Lattice Structure:</b>
 * <p>
 * This type forms a meet semi-lattice with partial order:
 * <pre>
 *     Solution    Failure
 *          \      /
 *           \    /
 *          Unknown (⊥)
 * </pre>
 * Where:
 * <ul>
 *   <li>{@code Unknown} is the bottom element (⊥)</li>
 *   <li>{@code Unknown ⊑ Solution} (Unknown is less than Solution)</li>
 *   <li>{@code Unknown ⊑ Failure} (Unknown is less than Failure)</li>
 *   <li>{@code Solution} and {@code Failure} are incomparable</li>
 * </ul>
 * <p>
 * The meet operation (⊓) is defined as:
 * <ul>
 *   <li>{@code Unknown ⊓ x = Unknown} for any x</li>
 *   <li>{@code x ⊓ Unknown = Unknown} for any x</li>
 *   <li>{@code Solution(a) ⊓ Solution(b) = Solution(a)} if a equals b, else {@code Unknown}</li>
 *   <li>{@code Failure(r1) ⊓ Failure(r2) = Failure(r1)} if r1 equals r2, else {@code Unknown}</li>
 *   <li>{@code Solution ⊓ Failure = Unknown} (incomparable elements meet at bottom)</li>
 * </ul>
 * <p>
 * This implementation follows the monad laws:
 * <ol>
 *   <li><b>Left Identity:</b> {@code of(a).flatMap(f) ≡ f.apply(a)}</li>
 *   <li><b>Right Identity:</b> {@code m.flatMap(UnificationAnswer::of) ≡ m}</li>
 *   <li><b>Associativity:</b> {@code m.flatMap(f).flatMap(g) ≡ m.flatMap(x -> f.apply(x).flatMap(g))}</li>
 * </ol>
 *
 * @param <T> the type of the unification solution
 */
public sealed interface UnificationAnswer<T> 
    permits UnificationAnswer.Solution, 
            UnificationAnswer.Failure, 
            UnificationAnswer.Unknown {
    
    // ========== Factory Methods ==========
    
    /**
     * Creates a {@code UnificationAnswer} representing a successful unification.
     *
     * @param solution the unification solution, must not be {@code null}
     * @param <T> the type of the solution
     * @return a {@code Solution} instance containing the solution
     * @throws NullPointerException if solution is {@code null}
     */
    static <T> UnificationAnswer<T> of(T solution) {
        return new Solution<>(Objects.requireNonNull(solution, "solution must not be null"));
    }
    
    /**
     * Creates a {@code UnificationAnswer} representing a failed unification.
     *
     * @param reason the failure reason, must not be {@code null}
     * @param <T> the type of the solution (phantom type)
     * @return a {@code Failure} instance containing the reason
     * @throws NullPointerException if reason is {@code null}
     */
    static <T> UnificationAnswer<T> failure(Object reason) {
        return new Failure<>(Objects.requireNonNull(reason, "failure reason must not be null"));
    }
    
    /**
     * Returns the singleton {@code UnificationAnswer} representing an unknown state.
     *
     * @param <T> the type of the solution (phantom type)
     * @return an {@code Unknown} instance
     */
    static <T> UnificationAnswer<T> unknown() {
        @SuppressWarnings("unchecked")
        var result = (UnificationAnswer<T>) Unknown.INSTANCE;
        return result;
    }
    
    // ========== Monadic Operations ==========
    
    /**
     * Maps the solution value using the given function.
     * <p>
     * If this is a {@code Solution}, applies the function to the solution value.
     * If this is a {@code Failure} or {@code Unknown}, propagates unchanged.
     *
     * @param f the mapping function
     * @param <U> the type of the result
     * @return a new {@code UnificationAnswer} with the mapped value
     * @throws NullPointerException if f is {@code null}
     */
    <U> UnificationAnswer<U> map(Function<? super T, ? extends U> f);
    
    /**
     * FlatMaps the solution value using the given function.
     * <p>
     * If this is a {@code Solution}, applies the function to the solution value.
     * If this is a {@code Failure} or {@code Unknown}, propagates unchanged.
     * <p>
     * This is the monadic bind operation.
     *
     * @param f the flatMapping function
     * @param <U> the type of the result
     * @return a new {@code UnificationAnswer} returned by the function
     * @throws NullPointerException if f is {@code null}
     */
    <U> UnificationAnswer<U> flatMap(Function<? super T, UnificationAnswer<U>> f);
    
    // ========== Recovery Operations ==========
    
    /**
     * Recovers from a failure state using the given handler.
     * <p>
     * If this is a {@code Failure}, applies the handler to convert it to a {@code Solution}.
     * If this is a {@code Solution} or {@code Unknown}, returns unchanged.
     *
     * @param handler the failure recovery function
     * @return a {@code UnificationAnswer} with the failure recovered
     * @throws NullPointerException if handler is {@code null}
     */
    UnificationAnswer<T> recover(Function<? super Object, ? extends T> handler);
    
    /**
     * Recovers from an unknown state using the given supplier.
     * <p>
     * If this is {@code Unknown}, applies the supplier to produce a {@code Solution}.
     * If this is a {@code Solution} or {@code Failure}, returns unchanged.
     *
     * @param supplier the unknown recovery supplier
     * @return a {@code UnificationAnswer} with unknown recovered
     * @throws NullPointerException if supplier is {@code null}
     */
    UnificationAnswer<T> recoverUnknown(Supplier<? extends T> supplier);
    
    /**
     * Maps the failure reason using the given function.
     * <p>
     * If this is a {@code Failure}, applies the function to the failure reason.
     * If this is a {@code Solution} or {@code Unknown}, returns unchanged.
     *
     * @param f the mapping function for failure reasons
     * @return a {@code UnificationAnswer} with the mapped failure reason
     * @throws NullPointerException if f is {@code null}
     */
    UnificationAnswer<T> mapFailure(Function<? super Object, ?> f);
    
    // ========== Extraction Operations ==========
    
    /**
     * Returns the solution value if present, otherwise returns the fallback value.
     *
     * @param fallback the fallback value
     * @return the solution if present, otherwise fallback
     */
    T solutionOrElse(T fallback);
    
    /**
     * Returns the solution value if present, otherwise computes it using the supplier.
     *
     * @param supplier the fallback supplier
     * @return the solution if present, otherwise the supplied value
     * @throws NullPointerException if supplier is {@code null}
     */
    T solutionOrElse(Supplier<? extends T> supplier);
    
    /**
     * Returns an {@code Optional} containing the solution if present.
     *
     * @return an {@code Optional} with the solution, or empty if not a solution
     */
    Optional<T> toOptional();
    
    /**
     * Returns a {@code Stream} containing the solution if present, or an empty stream otherwise.
     * <p>
     * This method is useful for:
     * <ul>
     *   <li>Converting to an iterator: {@code answer.stream().iterator()}</li>
     *   <li>Filtering and mapping: {@code answer.stream().filter(...).map(...)}</li>
     *   <li>Collecting results: {@code answer.stream().collect(...)}</li>
     *   <li>Integrating with Stream APIs</li>
     * </ul>
     *
     * @return a {@code Stream} containing the solution, or empty if Failure or Unknown
     */
    default Stream<T> stream() {
        return toOptional().stream();
    }
    
    /**
     * Returns the solution value if present.
     *
     * @return the solution value
     * @throws NoSuchElementException if not a solution
     */
    T solution();
    
    /**
     * Returns the failure reason if present.
     *
     * @return the failure reason
     * @throws NoSuchElementException if not a failure
     */
    Object failureReason();
    
    // ========== State Checks ==========
    
    /**
     * Returns {@code true} if this is a {@code Solution}.
     *
     * @return {@code true} if solution is present
     */
    boolean hasSolution();
    
    /**
     * Returns {@code true} if this is a {@code Failure}.
     *
     * @return {@code true} if failure
     */
    boolean isFailure();
    
    /**
     * Returns {@code true} if this is {@code Unknown}.
     *
     * @return {@code true} if unknown
     */
    boolean isUnknown();
    
    // ========== Meet Semi-Lattice Operations ==========
    
    /**
     * Computes the meet (greatest lower bound) of this and another UnificationAnswer.
     * <p>
     * The meet operation (⊓) follows these rules:
     * <ul>
     *   <li>{@code Unknown ⊓ x = Unknown} for any x (bottom absorbs)</li>
     *   <li>{@code x ⊓ Unknown = Unknown} for any x (bottom absorbs)</li>
     *   <li>{@code Solution(a) ⊓ Solution(b) = Solution(a)} if a equals b</li>
     *   <li>{@code Solution(a) ⊓ Solution(b) = Unknown} if a ≠ b (inconsistent)</li>
     *   <li>{@code Failure(r1) ⊓ Failure(r2) = Failure(r1)} if r1 equals r2</li>
     *   <li>{@code Failure(r1) ⊓ Failure(r2) = Unknown} if r1 ≠ r2 (inconsistent)</li>
     *   <li>{@code Solution ⊓ Failure = Unknown} (incomparable)</li>
     *   <li>{@code Failure ⊓ Solution = Unknown} (incomparable)</li>
     * </ul>
     *
     * @param other the other UnificationAnswer
     * @return the meet of this and other
     * @throws NullPointerException if other is {@code null}
     */
    UnificationAnswer<T> meet(UnificationAnswer<? extends T> other);
    
    /**
     * Checks if this UnificationAnswer is less than or equal to another in the lattice order.
     * <p>
     * The partial order (⊑) is defined as:
     * <ul>
     *   <li>{@code Unknown ⊑ x} for any x (bottom is less than everything)</li>
     *   <li>{@code x ⊑ x} for any x (reflexivity)</li>
     *   <li>{@code Solution} and {@code Failure} are incomparable</li>
     * </ul>
     *
     * @param other the other UnificationAnswer
     * @return {@code true} if this ⊑ other
     * @throws NullPointerException if other is {@code null}
     */
    boolean lessOrEqual(UnificationAnswer<? extends T> other);
    
    /**
     * Returns the bottom element of the lattice (Unknown).
     *
     * @param <T> the type parameter
     * @return the bottom element (Unknown)
     */
    static <T> UnificationAnswer<T> bottom() {
        return unknown();
    }


    /**
     * Folds over this UnificationAnswer by applying one of three functions based on its state.
     * This is the catamorphism (universal fold) for the UnificationAnswer sum type.
     *
     * <p>This method allows you to handle all three cases in a single expression:
     * <pre>{@code
     * String result = answer.fold(
     *     solution -> "Success: " + solution,
     *     failure -> "Error: " + failure,
     *     () -> "Unknown state"
     * );
     * }</pre>
     *
     * @param <R> the result type
     * @param onSolution function to apply if this is a Solution
     * @param onFailure function to apply if this is a Failure
     * @param onUnknown supplier to call if this is Unknown
     * @return the result of applying the appropriate function
     */
    default <R> R fold(
            Function<? super T, ? extends R> onSolution,
            Function<Object, ? extends R> onFailure,
            Supplier<? extends R> onUnknown
    ) {
        return switch (this) {
            case Solution<T> sol -> onSolution.apply(sol.solution());
            case Failure<T> fail -> onFailure.apply(fail.reason());
            case Unknown<T> _ -> onUnknown.get();
        };
    }

    // ========== Case Classes ==========
    
    /**
     * Represents a successful unification with a solution.
     *
     * @param solution the unification solution
     * @param <T> the type of the solution
     */
    record Solution<T>(T solution) implements UnificationAnswer<T> {
        public Solution {
            Objects.requireNonNull(solution, "solution must not be null");
        }
        
        @Override
        public <U> UnificationAnswer<U> map(Function<? super T, ? extends U> f) {
            Objects.requireNonNull(f, "mapping function must not be null");
            try {
                return UnificationAnswer.of(f.apply(solution));
            } catch (Exception e) {
                return UnificationAnswer.failure(e);
            }
        }
        
        @Override
        public <U> UnificationAnswer<U> flatMap(Function<? super T, UnificationAnswer<U>> f) {
            Objects.requireNonNull(f, "flatMap function must not be null");
            try {
                return f.apply(solution);
            } catch (Exception e) {
                return UnificationAnswer.failure(e);
            }
        }
        
        @Override
        public UnificationAnswer<T> recover(Function<? super Object, ? extends T> handler) {
            Objects.requireNonNull(handler, "recovery handler must not be null");
            return this;
        }
        
        @Override
        public UnificationAnswer<T> recoverUnknown(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier, "recovery supplier must not be null");
            return this;
        }
        
        @Override
        public UnificationAnswer<T> mapFailure(Function<? super Object, ?> f) {
            Objects.requireNonNull(f, "failure mapping function must not be null");
            return this;
        }
        
        @Override
        public T solutionOrElse(T fallback) {
            return solution;
        }
        
        @Override
        public T solutionOrElse(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier, "fallback supplier must not be null");
            return solution;
        }
        
        @Override
        public Optional<T> toOptional() {
            return Optional.of(solution);
        }
        
        @Override
        public boolean hasSolution() {
            return true;
        }
        
        @Override
        public boolean isFailure() {
            return false;
        }
        
        @Override
        public boolean isUnknown() {
            return false;
        }
        
        @Override
        public Object failureReason() {
            throw new NoSuchElementException("Not a failure");
        }
        
        @Override
        public UnificationAnswer<T> meet(UnificationAnswer<? extends T> other) {
            Objects.requireNonNull(other, "other must not be null");
            return switch (other) {
                case Solution<?> otherSol -> {
                    // Solution ⊓ Solution = Solution if equal, else Unknown
                    if (Objects.equals(this.solution, otherSol.solution())) {
                        yield this;
                    } else {
                        yield UnificationAnswer.unknown();
                    }
                }
                case Failure<?> _ -> UnificationAnswer.unknown(); // Solution ⊓ Failure = Unknown (incomparable)
                case Unknown<?> _ -> UnificationAnswer.unknown(); // Solution ⊓ Unknown = Unknown (bottom absorbs)
            };
        }
        
        @Override
        public boolean lessOrEqual(UnificationAnswer<? extends T> other) {
            Objects.requireNonNull(other, "other must not be null");
            return switch (other) {
                case Solution<?> otherSol -> Objects.equals(this.solution, otherSol.solution()); // Solution ⊑ Solution iff equal
                case Failure<?> _ -> false; // Solution and Failure are incomparable
                case Unknown<?> _ -> false; // Nothing is ⊑ Unknown except Unknown itself
            };
        }

        @Override
        public String toString() {
            return "Solution[" + solution + ']';
        }
    }
    
    /**
     * Represents a failed unification with a reason.
     *
     * @param reason the failure reason
     * @param <T> the type of the solution (phantom type)
     */
    record Failure<T>(Object reason) implements UnificationAnswer<T> {
        public Failure {
            Objects.requireNonNull(reason, "failure reason must not be null");
        }
        
        @Override
        public <U> UnificationAnswer<U> map(Function<? super T, ? extends U> f) {
            Objects.requireNonNull(f, "mapping function must not be null");
            return UnificationAnswer.failure(reason);
        }
        
        @Override
        public <U> UnificationAnswer<U> flatMap(Function<? super T, UnificationAnswer<U>> f) {
            Objects.requireNonNull(f, "flatMap function must not be null");
            return UnificationAnswer.failure(reason);
        }
        
        @Override
        public UnificationAnswer<T> recover(Function<? super Object, ? extends T> handler) {
            Objects.requireNonNull(handler, "recovery handler must not be null");
            try {
                return UnificationAnswer.of(handler.apply(reason));
            } catch (Exception e) {
                return UnificationAnswer.failure(e);
            }
        }
        
        @Override
        public UnificationAnswer<T> recoverUnknown(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier, "recovery supplier must not be null");
            return this;
        }
        
        @Override
        public UnificationAnswer<T> mapFailure(Function<? super Object, ?> f) {
            Objects.requireNonNull(f, "failure mapping function must not be null");
            try {
                return UnificationAnswer.failure(f.apply(reason));
            } catch (Exception e) {
                return UnificationAnswer.failure(e);
            }
        }
        
        @Override
        public T solutionOrElse(T fallback) {
            return fallback;
        }
        
        @Override
        public T solutionOrElse(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier, "fallback supplier must not be null");
            return supplier.get();
        }
        
        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }
        
        @Override
        public T solution() {
            throw new NoSuchElementException("Not a solution: " + reason);
        }
        
        @Override
        public Object failureReason() {
            return reason;
        }
        
        @Override
        public boolean hasSolution() {
            return false;
        }
        
        @Override
        public boolean isFailure() {
            return true;
        }
        
        @Override
        public boolean isUnknown() {
            return false;
        }
        
        @Override
        public UnificationAnswer<T> meet(UnificationAnswer<? extends T> other) {
            Objects.requireNonNull(other, "other must not be null");
            return switch (other) {
                case Solution<?> _ -> UnificationAnswer.unknown(); // Failure ⊓ Solution = Unknown (incomparable)
                case Failure<?> otherFail -> {
                    // Failure ⊓ Failure = Failure if equal, else Unknown
                    if (Objects.equals(this.reason, otherFail.reason())) {
                        yield this;
                    } else {
                        yield UnificationAnswer.unknown();
                    }
                }
                case Unknown<?> _ -> UnificationAnswer.unknown(); // Failure ⊓ Unknown = Unknown (bottom absorbs)
            };
        }
        
        @Override
        public boolean lessOrEqual(UnificationAnswer<? extends T> other) {
            Objects.requireNonNull(other, "other must not be null");
            return switch (other) {
                case Solution<?> _ -> false; // Failure and Solution are incomparable
                case Failure<?> otherFail -> Objects.equals(this.reason, otherFail.reason()); // Failure ⊑ Failure iff equal
                case Unknown<?> _ -> false; // Nothing is ⊑ Unknown except Unknown itself
            };
        }
    }
    
    /**
     * Represents an unknown unification state.
     *
     * @param <T> the type of the solution (phantom type)
     */
    final class Unknown<T> implements UnificationAnswer<T> {
        private static final Unknown<?> INSTANCE = new Unknown<>();
        
        private Unknown() {
            // Private constructor for singleton
        }
        
        @Override
        public <U> UnificationAnswer<U> map(Function<? super T, ? extends U> f) {
            Objects.requireNonNull(f, "mapping function must not be null");
            return UnificationAnswer.unknown();
        }
        
        @Override
        public <U> UnificationAnswer<U> flatMap(Function<? super T, UnificationAnswer<U>> f) {
            Objects.requireNonNull(f, "flatMap function must not be null");
            return UnificationAnswer.unknown();
        }
        
        @Override
        public UnificationAnswer<T> recover(Function<? super Object, ? extends T> handler) {
            Objects.requireNonNull(handler, "recovery handler must not be null");
            return this;
        }
        
        @Override
        public UnificationAnswer<T> recoverUnknown(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier, "recovery supplier must not be null");
            try {
                return UnificationAnswer.of(supplier.get());
            } catch (Exception e) {
                return UnificationAnswer.failure(e);
            }
        }
        
        @Override
        public UnificationAnswer<T> mapFailure(Function<? super Object, ?> f) {
            Objects.requireNonNull(f, "failure mapping function must not be null");
            return this;
        }
        
        @Override
        public T solutionOrElse(T fallback) {
            return fallback;
        }
        
        @Override
        public T solutionOrElse(Supplier<? extends T> supplier) {
            Objects.requireNonNull(supplier, "fallback supplier must not be null");
            return supplier.get();
        }
        
        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }
        
        @Override
        public T solution() {
            throw new NoSuchElementException("Unknown state - no solution");
        }
        
        @Override
        public Object failureReason() {
            throw new NoSuchElementException("Unknown state - no failure reason");
        }
        
        @Override
        public boolean hasSolution() {
            return false;
        }
        
        @Override
        public boolean isFailure() {
            return false;
        }
        
        @Override
        public boolean isUnknown() {
            return true;
        }
        
        @Override
        public UnificationAnswer<T> meet(UnificationAnswer<? extends T> other) {
            Objects.requireNonNull(other, "other must not be null");
            // Unknown ⊓ x = Unknown for any x (bottom absorbs everything)
            return this;
        }
        
        @Override
        public boolean lessOrEqual(UnificationAnswer<? extends T> other) {
            Objects.requireNonNull(other, "other must not be null");
            // Unknown ⊑ x for any x (bottom is less than or equal to everything)
            return true;
        }
        
        @Override
        public String toString() {
            return "Unknown";
        }
        
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Unknown<?>;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
    }
}