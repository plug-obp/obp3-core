package fr.ensta.traversal.bfs.relational;

// The parent abstract union type
public abstract class BreadthFirstSearchAction<V> {

    // Factory methods for creating specific actions.
    public static <V> BreadthFirstSearchAction<V> initializeA() {
        return new InitializeAction<>();
    }

    public static <V> BreadthFirstSearchAction<V> discoverA() {
        return new DiscoverAction<>();
    }

    public static <V> BreadthFirstSearchAction<V> inKnownA() {
        return new InKnownAction<>();
    }

    public static <V> BreadthFirstSearchAction<V> notInKnownA(V value) {
        return new NotInKnownAction<>(value);
    }

    // Empty variant classes represent specific actions.
    public static class InitializeAction<V> extends BreadthFirstSearchAction<V> {
    }

    public static class DiscoverAction<V> extends BreadthFirstSearchAction<V> {
    }

    public static class InKnownAction<V> extends BreadthFirstSearchAction<V> {
    }

    // Variant with a field for `UnknownA`.
    public static class NotInKnownAction<V> extends BreadthFirstSearchAction<V> {
        public final V vertex;

        public NotInKnownAction(V vertex) {
            this.vertex = vertex;
        }
    }
}