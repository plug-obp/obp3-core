package obp3.hashcons;

public interface HashConsed<T> {
    T node();
    int tag();
    int hashKey();
    default boolean isHashConsed() {
        return true;
    }
}

