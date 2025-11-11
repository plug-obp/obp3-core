package obp3.hashcons;

public interface Hashable<T> {
    boolean equal(T x, T y);
    int hash(T x);
}
