package obp3.utils;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Either<A, B>
        permits Either.Left, Either.Right {

    // ----- Constructors -----
    static <A, B> Either<A, B> left(A value) {
        return new Left<>(value);
    }

    static <A, B> Either<A, B> right(B value) {
        return new Right<>(value);
    }

    // ----- State Predicates -----
    boolean isLeft();
    boolean isRight();

    // ----- Main API -----
    <C> C fold(Function<? super A, ? extends C> leftFn,
               Function<? super B, ? extends C> rightFn);

    default Optional<A> leftOption() {
        return fold(Optional::ofNullable, b -> Optional.empty());
    }

    default Optional<B> rightOption() {
        return fold(a -> Optional.empty(), Optional::ofNullable);
    }

    default void ifLeft(Consumer<? super A> action) {
        leftOption().ifPresent(action);
    }

    default void ifRight(Consumer<? super B> action) {
        rightOption().ifPresent(action);
    }

    default <C> Either<C, B> mapLeft(Function<? super A, ? extends C> mapper) {
        return fold(a -> left(Objects.requireNonNull(mapper.apply(a))),
                Either::right);
    }

    default <C> Either<A, C> mapRight(Function<? super B, ? extends C> mapper) {
        return fold(Either::left,
                b -> right(Objects.requireNonNull(mapper.apply(b))));
    }

    default <C> Either<C, B> flatMapLeft(Function<? super A, Either<C, B>> mapper) {
        return fold(mapper, Either::right);
    }

    default <C> Either<A, C> flatMapRight(Function<? super B, Either<A, C>> mapper) {
        return fold(Either::left, mapper);
    }

    default Either<B, A> swap() {
        return fold(Either::right, Either::left);
    }

    // ----- Implementations -----
    record Left<A, B>(A value) implements Either<A, B> {
        public Left {
            Objects.requireNonNull(value, "Left value must not be null");
        }

        @Override public boolean isLeft() { return true; }
        @Override public boolean isRight() { return false; }

        @Override
        public <C> C fold(Function<? super A, ? extends C> leftFn,
                          Function<? super B, ? extends C> rightFn) {
            return leftFn.apply(value);
        }
    }

    record Right<A, B>(B value) implements Either<A, B> {
        public Right {
            Objects.requireNonNull(value, "Right value must not be null");
        }

        @Override public boolean isLeft() { return false; }
        @Override public boolean isRight() { return true; }

        @Override
        public <C> C fold(Function<? super A, ? extends C> leftFn,
                          Function<? super B, ? extends C> rightFn) {
            return rightFn.apply(value);
        }
    }
}
