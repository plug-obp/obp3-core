package obp3.fixer;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Demand-driven least solutions of a fixed system of monotone equations.
 * Inspired by Francois Pottier's <em>Lazy Least Fixed Points in ML</em>.
 *
 * <p>An equation receives its variable and a synchronous {@code request} function
 * for reading approximations of other variables. It must use that function, not
 * recursively call {@link #apply(Object)}. Equations must be pure, monotone and
 * stable for this solver's lifetime, demand finitely many variables, and use a
 * property order with finite ascending chains. Properties, including bottom,
 * must be immutable (or treated as immutable). The solver does not supply a join,
 * check monotonicity or widen values.
 *
 * <p>Completed solutions are memoized for this equation system. A new program,
 * graph or model requires a new solver. Failed or cancelled equation evaluation
 * discards the entire current run, including newly discovered maximal values;
 * earlier completed solutions remain cached. External effects of client code
 * cannot be rolled back.
 *
 * <p>Only one public call may be active. Reentrant and concurrent calls fail
 * immediately, including cached reads. A request is valid only on its originating
 * thread during its own equation invocation. These checks do not depend on Java
 * assertions. Sequential use from different threads is allowed.
 *
 * @param <S> non-null variables with stable identity/equality under the chosen map
 * @param <T> non-null immutable properties
 */
public class Fixer<S, T> implements Function<S, T> {
    private final Lattice<T> lattice;
    private final BiFunction<S, Function<S, T>, T> function;
    private final Supplier<? extends Map<?, ?>> mapSupplier;
    private final Map<S, T> fixed;
    private final AtomicBoolean active = new AtomicBoolean();

    /** Uses reference identity for variables; use {@code HashMap::new} for value equality. */
    public Fixer(BiFunction<S, Function<S, T>, T> function, Lattice<T> lattice) {
        this(function, lattice, IdentityHashMap::new);
    }

    /**
     * Uses the supplied map semantics for variable equality throughout the solver.
     * The factory must return a fresh, empty, mutable, strongly retaining map on
     * every call, with the same key equivalence and no value-type restrictions.
     * Maps must not be retained or mutated by the caller; keys and map operations
     * must obey their ordinary stable, non-throwing contracts.
     */
    @SuppressWarnings("rawtypes") // Preserve source compatibility with legacy Supplier<Map> callers.
    public Fixer(BiFunction<S, Function<S, T>, T> function, Lattice<T> lattice,
                 Supplier<? extends Map> mapSupplier) {
        this.function = Objects.requireNonNull(function, "function");
        this.lattice = Objects.requireNonNull(lattice, "lattice");
        var checkedSupplier = Objects.requireNonNull(mapSupplier, "mapSupplier");
        this.mapSupplier = checkedSupplier::get;
        this.fixed = newMap();
    }

    @SuppressWarnings("unchecked")
    private <V> Map<S, V> newMap() {
        // The factory is value-polymorphic by contract; keep the single cast here.
        Map<?, ?> map = Objects.requireNonNull(mapSupplier.get(), "mapSupplier result");
        if (!map.isEmpty()) throw new IllegalArgumentException("mapSupplier must return empty maps");
        return (Map<S, V>) map;
    }

    /** Computes or retrieves a completed solution; no cancellation is requested. */
    @Override
    public T apply(S variable) {
        return apply(variable, () -> false);
    }

    /**
     * Computes a solution with cooperative cancellation. Cancellation is checked
     * before cache access, between equations, at requests and before publication.
     * It throws {@link CancellationException}, never returns a partial solution,
     * and leaves the solver reusable. Once observed, cancellation is latched for
     * the run even if an equation catches the exception. A blocking equation
     * which does not request anything cannot be preempted by this mechanism.
     *
     * @param variable variable to solve
     * @param cancellationRequested side-effect-free cancellation predicate
     * @return the least solution under the class contract
     */
    public T apply(S variable, BooleanSupplier cancellationRequested) {
        Objects.requireNonNull(variable, "variable");
        Objects.requireNonNull(cancellationRequested, "cancellationRequested");
        if (!active.compareAndSet(false, true))
            throw new IllegalStateException("Fixer.apply is already active; use the equation's request callback");
        Run run = null;
        try {
            if (cancellationRequested.getAsBoolean()) throw new CancellationException("Fixer cancelled");
            T cached = fixed.get(variable);
            if (cached != null) return cached;
            run = new Run(cancellationRequested);
            Node root = run.node(variable);
            while (!run.work.isEmpty()) {
                run.checkCancellation();
                Node current = run.work.removeFirst();
                current.queued = false;
                run.solve(current);
            }
            run.checkCancellation();
            // No callback, equality check or cancellation predicate runs during
            // publication. Only a fully quiescent run enters the permanent cache.
            for (Node node : run.nodes.values()) fixed.put(node.variable, node.value);
            return root.value;
        } finally {
            if (run != null) run.discard();
            active.set(false);
        }
    }

    /** Canonical per-run node identity also governs dependency sets and the queue. */
    private final class Node {
        final S variable;
        final Set<Node> parents = new LinkedHashSet<>();
        final Set<Node> children = new LinkedHashSet<>();
        T value = lattice.bottom();
        boolean queued;
        boolean maximal;

        Node(S variable) { this.variable = variable; }

        void detachChildren() {
            for (Node child : children) child.parents.remove(this);
            children.clear();
        }
    }

    private final class Run {
        final Map<S, Node> nodes = newMap();
        final ArrayDeque<Node> work = new ArrayDeque<>();
        final Thread owner = Thread.currentThread();
        final BooleanSupplier cancellationRequested;
        boolean cancelled;

        Run(BooleanSupplier cancellationRequested) {
            if ((Object) nodes == fixed)
                throw new IllegalArgumentException("mapSupplier must return fresh maps");
            this.cancellationRequested = cancellationRequested;
        }

        void checkCancellation() {
            if (cancelled || cancellationRequested.getAsBoolean()) {
                cancelled = true;
                throw new CancellationException("Fixer cancelled");
            }
        }

        Node node(S variable) {
            Node node = nodes.get(variable);
            if (node == null) {
                node = new Node(variable);
                nodes.put(variable, node);
                enqueue(node);
            }
            return node;
        }

        void enqueue(Node node) {
            if (node.queued || node.maximal) return;
            // A queued equation observes nothing until its next evaluation.
            node.detachChildren();
            node.queued = true;
            work.addLast(node);
        }

        void solve(Node current) {
            Request request = new Request(current);
            T next;
            try {
                next = Objects.requireNonNull(function.apply(current.variable, request), "equation result");
            } finally {
                request.close();
            }
            checkCancellation();
            boolean changed = !lattice.equality().test(current.value, next);
            current.maximal = lattice.isMaximal(next);
            current.value = next;
            if (current.maximal) current.detachChildren();
            if (changed) {
                // Enqueuing an observer removes it from this parents set.
                for (Node parent : List.copyOf(current.parents)) enqueue(parent);
            }
        }

        void discard() {
            // Also release the graph if a client retains an expired request.
            for (Node node : nodes.values()) {
                node.parents.clear();
                node.children.clear();
            }
            nodes.clear();
            work.clear();
        }

        private final class Request implements Function<S, T> {
            private Node observer;

            Request(Node observer) { this.observer = observer; }

            @Override
            public T apply(S variable) {
                if (Thread.currentThread() != owner)
                    throw new IllegalStateException("Equation requests must stay on their originating thread");
                if (observer == null)
                    throw new IllegalStateException("Equation request has expired");
                Objects.requireNonNull(variable, "variable");
                checkCancellation();
                T cached = fixed.get(variable);
                if (cached != null) return cached;
                Node dependency = node(variable);
                if (!dependency.maximal) {
                    observer.children.add(dependency);
                    dependency.parents.add(observer);
                }
                return dependency.value;
            }

            void close() { observer = null; }
        }
    }
}
