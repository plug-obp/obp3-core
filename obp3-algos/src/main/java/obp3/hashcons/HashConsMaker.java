package obp3.hashcons;

@FunctionalInterface
public interface HashConsMaker<T> {
    HashConsed<T> create(T node, int tag, int hashKey);
}
