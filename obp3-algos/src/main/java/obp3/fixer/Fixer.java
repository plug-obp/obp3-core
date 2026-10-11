package obp3.fixer;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/// Heavily inspired by:
/// - FRANCOIS POTTIER,<a href="https://gallium.inria.fr/~fpottier/publis/fpottier-fix.pdf"> Lazy Least Fixed Points in ML</a>
/// - <a href="https://gitlab.inria.fr/fpottier/fix">OCAML Code</a>
///
/// Computes least solutions of fixed, pure monotone equations with finite demand
/// and finite ascending chains. Properties must be non-null and immutable.
/// Dependency reads use the supplied synchronous request, never public apply.
/// Completed solutions are cached: changing equations requires a new Fixer.
/// Failed/cancelled runs discard new results and preserve earlier completed ones.
/// Only one public call may be active; callbacks expire after their own RHS and
/// must stay on its thread. These guards do not depend on Java assertions.


public class Fixer<S, T> implements Function<S, T>{
    Lattice<T> lattice;
    BiFunction<S, Function<S, T>, T> function;
    Supplier<? extends Map> mapSupplier;

    /// The permanent table maps variables that have reached a fixed point to properties. It persists forever.
    Map<S, T> mFixed;

    /// The transient table maps variables that have not yet reached a
    /// fixed point to nodes. At the beginning of a run, it is empty.
    /// It fills up during a run.
    /// At the end of a run, it is copied into the permanent table and cleared.
    Map<S, T> mTransient;
    /// Records the observers of a node (The nodes from which reachable)
    /// The two adjacency tables record the dependency graph in both directions.
    Map<S, Set<S>> mParents;
    Map<S, Set<S>> mChildren;

    /// The workset is based on a Queue, but it could just as well be based on a
    ///    Stack. A textual replacement is possible. It could also be based on a
    ///    priority queue, provided a sensible way of assigning priorities could
    ///    be found.
    ///
    /// A node in the workset has no successors.
    /// NC: The idea is that when a node gets in the workset, it is like newly discovered - so we forget its successors
    ///    (It can have predecessors.)
    ///    In other words, a predecessor (an observer) of some node is never in the workset.
    ///    Furthermore, a node never appears twice in the workset.
    ///
    /// When a variable broadcasts a signal, all of its predecessors (observers) receive the signal.
    ///    Any variable that receives the signal loses all of its
    ///    successors (that is, it ceases to observe anything) and is inserted into
    ///    the workset. This preserves the above invariant.
    Queue<S> mWorkset = new LinkedList<>();
    Set<S> mQueued;

    public Fixer(BiFunction<S, Function<S, T>, T> function, Lattice<T> lattice) {
        this(function, lattice, IdentityHashMap::new);
    }

    /// The default constructor uses identity keys; use HashMap::new for structural keys.
    /// The factory must supply fresh, empty, mutable, strongly retaining maps with
    /// consistent key equivalence, unrestricted value types and nonthrowing operations.
    public Fixer(BiFunction<S, Function<S, T>, T> function, Lattice<T> lattice, Supplier<? extends Map> mapSupplier) {
        this.function = Objects.requireNonNull(function, "function");
        this.lattice = Objects.requireNonNull(lattice, "lattice");
        this.mapSupplier = Objects.requireNonNull(mapSupplier, "mapSupplier");
        this.mFixed = newMap();
        this.mTransient = newMap();
        this.mParents = newMap();
        this.mChildren = newMap();
        this.mQueued = Collections.newSetFromMap(newMap());
    }

    @SuppressWarnings("unchecked")
    private <V> Map<S, V> newMap() {
        Map<?, ?> map = Objects.requireNonNull(mapSupplier.get(), "mapSupplier result");
        if (!map.isEmpty()) throw new IllegalArgumentException("mapSupplier must return empty maps");
        if (map == mFixed || map == mTransient || map == mParents || map == mChildren)
            throw new IllegalArgumentException("mapSupplier must return fresh maps");
        return (Map<S, V>) map;
    }

    /// The flag 'inactive' prevents reentrant calls by the client.
    private final AtomicBoolean inactive = new AtomicBoolean(true);
    private BooleanSupplier cancellationRequested;
    private boolean cancelled;

    /// Invocations of 'apply' trigger the fixed point computation.
    @Override
    public T apply(S node) {
        return apply(node, () -> false);
    }

    /// Cooperative cancellation never returns a partial result. Once observed it
    /// stays latched for this run, even if an RHS catches CancellationException.
    /// Blocking client code cannot be preempted; cancellation needs a checkpoint.
    public T apply(S node, BooleanSupplier cancellationRequested) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(cancellationRequested, "cancellationRequested");
        if (!inactive.compareAndSet(true, false))
            throw new IllegalStateException("Fixer.apply is already active; use the equation's request callback");
        boolean solving = false;
        try {
            this.cancellationRequested = cancellationRequested;
            checkCancellation();
            // if the node is fixed already just return it.
            var property = mFixed.get(node);
            if (property != null) { return property; }
            solving = true;
            ensureTransient(node);
            // while we have work to do just do it,
            // - starting either at the node that we just added, if the workset was empty and node was not already transient
            // - or continue with the workset in the defined 'order' (FIFO here).
            while (!mWorkset.isEmpty()) {
                checkCancellation();
                var current = mWorkset.poll();
                mQueued.remove(current);
                solve(current);
            }

            // we are done for this node.
            // copies the transient table into the permanent table, and
            //    empties the transient table.
            //    Dependency edges can then be reclaimed; completed values stay cached.
            checkCancellation();
            mFixed.putAll(mTransient);
            return mFixed.get(node);
        } finally {
            if (solving) {
                mTransient.clear();
                mParents.values().forEach(Set::clear);
                mChildren.values().forEach(Set::clear);
                mParents.clear();
                mChildren.clear();
                mWorkset.clear();
                mQueued.clear();
            }
            this.cancellationRequested = null;
            cancelled = false;
            inactive.set(true);
        }
    }

    private void checkCancellation() {
        if (cancelled || cancellationRequested.getAsBoolean()) {
            cancelled = true;
            throw new CancellationException("Fixer cancelled");
        }
    }

    void ensureTransient(S node) {
        // if a node is already transient, just return.
        if (mTransient.containsKey(node)) { return; }
        // create a transient node starting it at the bottom of the lattice.
        mTransient.put(node, lattice.bottom());
        mParents.put(node, Collections.newSetFromMap(newMap()) );
        mChildren.put(node, Collections.newSetFromMap(newMap()) );
        enqueue(node);
    }

    void enqueue(S node) {
        if (!mQueued.add(node)) return;
        // Queued nodes cease observing their old dependencies until reevaluation.
        Set<S> oldChildren = mChildren.get(node);
        for (S oldChild : oldChildren) {
            mParents.get(oldChild).remove(node);
        }
        oldChildren.clear();
        mWorkset.add(node);
    }
    void solve(S current) {
        if (mFixed.get(current) != null) return;
        final List<S> currentChildren = new ArrayList<>();

        try {
            // The flag [alive] is used to prevent the client from invoking [request] after this interaction phase is over.
            final boolean[] alive = {true};
            final Thread owner = Thread.currentThread();
            Function<S, T> requestFunction = (S node) -> {
                if (Thread.currentThread() != owner || !alive[0])
                    throw new IllegalStateException("Equation request is expired or used on another thread");
                Objects.requireNonNull(node, "node");
                checkCancellation();
                var property = mFixed.get(node);
                if (property != null) { return property; }
                ensureTransient(node);
                currentChildren.add(node);
                return mTransient.get(node);
            };

            T newProperty;
            try {
                newProperty = Objects.requireNonNull(function.apply(current, requestFunction), "equation result");
            } finally {
                alive[0] = false;
            }
            checkCancellation();

            //     If we have gathered no children in the list [children], then this node must have stabilized.
            //     If [new_property] is maximal, then this node must have stabilized.
            //
            //     If this node has stabilized, then it need not observe anymore, so the call to [set_successors] is skipped.
            //     In practice, this seems to be a minor optimization. In the particular case where every node stabilizes at
            //     the very first call to [rhs], this means that no edges are ever built.
            //     This particular case is unlikely, as it means that we are just doing memoization,
            //     not a true fixed point computation.
            //
            //     Even maximal results stay transient until the whole run succeeds,
            //     so a later exception or cancellation cannot publish partial results.

            var isMaximal = lattice.isMaximal(newProperty);
            if (!isMaximal) {
                //register new parents/children
                for (S child : currentChildren) {
                    mParents.get(child).add(current);
                    mChildren.get(current).add(child);
                }

                // If the updated value differs from the previous value, record
                // the updated value and send a signal to all observers of [node].
                if (!lattice.equality().test(mTransient.get(current), newProperty)) {
                    mTransient.put(current, newProperty);
                    //signal the parents because the current value changed
                    for (S parent : new ArrayList<>(mParents.get(current))) enqueue(parent);
                }
            } else {
                //current got at the top of the lattice
                var oldProperty = mTransient.put(current, newProperty);
                if (!lattice.equality().test(oldProperty, newProperty)) {
                    //signal the parents because the current value changed
                    for (S parent : new ArrayList<>(mParents.get(current))) enqueue(parent);
                }
            }
        } finally {
            // A retained, expired callback must not retain its collected dependencies.
            currentChildren.clear();
        }
    }
}
