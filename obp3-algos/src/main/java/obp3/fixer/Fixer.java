package obp3.fixer;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/// Heavily inspired by:
/// - FRANCOIS POTTIER,<a href="https://gallium.inria.fr/~fpottier/publis/fpottier-fix.pdf"> Lazy Least Fixed Points in ML</a>
/// - <a href="https://gitlab.inria.fr/fpottier/fix">OCAML Code</a>


public class Fixer<S, T> implements Function<S, T>{
    Lattice<T> lattice;
    BiFunction<S, Function<S, T>, T> function;

    /// The permanent table maps variables that have reached a fixed point to properties. It persists forever.
    Map<S, T> mFixed = new IdentityHashMap<>();

    /// The transient table maps variables that have not yet reached a
    /// fixed point to nodes. At the beginning of a run, it is empty.
    /// It fills up during a run.
    /// At the end of a run, it is copied into the permanent table and cleared.
    Map<S, T> mTransient = new IdentityHashMap<>();
    /// Records the observers of a node (The nodes from which reachable)
    /// NC: In the Pottier algorithm this is a graph, I just store the parents here.
    /// NC: Homework: what are the consequences ?
    Map<S, Set<S>> mParents = new IdentityHashMap<>();

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

    public Fixer(BiFunction<S, Function<S, T>, T> function, Lattice<T> lattice) {
        this.function = function;
        this.lattice = lattice;
    }

    /// The flag 'inactive' prevents reentrant calls by the client.
    //    boolean inactive = true;
    int activeCount = 0;

    /// Invocations of 'apply' trigger the fixed point computation.
    @Override
    public T apply(S node) {
        // if the node is fixed already just return it.
        var property = mFixed.get(node);
        if (property != null) { return property; }
        //The `assert inactive` forbids calling apply again from inside your own RHS function during solving
        // assert inactive;
        if (activeCount++ > 0) {
            //Already computing a fixpoint -- instead of assert return the current approximation
            //nested calls return current best known value, I guess the monotonic semantics is maintained -- this should be tested
            ensureTransient(node);
            return mTransient.getOrDefault(node, mFixed.get(node));
        }
        try {
            ensureTransient(node);
            // while we have work to do just do it,
            // - starting either at the node that we just added, if the workset was empty and node was not already transient
            // - or continue with the workset in the defined 'order' (FIFO here).
            while (!mWorkset.isEmpty()) {
                var current = mWorkset.poll();
                solve(current);
            }

            // we are done for this node.
            // copies the transient table into the permanent table, and
            //    empties the transient table.
            //    This allows all nodes to be reclaimed by the garbage collector.
            mFixed.putAll(mTransient);
            mTransient.clear();
        } finally {
            activeCount--;
        }
        return mFixed.get(node);
    }

    void ensureTransient(S node) {
        // if a node is already transient, just return.
        if (mTransient.containsKey(node)) { return; }
        // create a transient node starting it at the bottom of the lattice.
        mTransient.put(node, lattice.bottom());
        mParents.put(node, Collections.newSetFromMap(new IdentityHashMap<>()) );
        mWorkset.add(node);
    }
    void solve(S current) {
        if (mFixed.get(current) != null) return;
        final List<S> currentChildren = new ArrayList<>();

        // The flag [alive] is used to prevent the client from invoking [request] after this interaction phase is over.
        // In theory, this dynamic check seems required in order to argue that [request] behaves like a pure function.
        // In practice, this check is not very useful: only a bizarre client would
        // store a [request] function and invoke it after it has become stale.
        final boolean[] alive = {true};
        Function<S, T> requestFunction = (S node) -> {
            assert alive[0];
            var property = mFixed.get(node);
            if (property != null) { return property; }
            ensureTransient(node);
            currentChildren.add(node);
            return mTransient.get(node);
        };

        var newProperty = function.apply(current, requestFunction);
        alive[0] = false;

        //     If we have gathered no children in the list [children], then this node must have stabilized.
        //     If [new_property] is maximal, then this node must have stabilized.
        //
        //     If this node has stabilized, then it need not observe anymore, so the call to [set_successors] is skipped.
        //     In practice, this seems to be a minor optimization. In the particular case where every node stabilizes at
        //     the very first call to [rhs], this means that no edges are ever built.
        //     This particular case is unlikely, as it means that we are just doing memoization,
        //     not a true fixed point computation.
        //
        //     One could go further and note that, if this node has stabilized, then it
        //     could immediately be taken out of the transient table and copied into the
        //     permanent table. This would have the beneficial effect of allowing the
        //     detection of further nodes that have stabilized. Furthermore, it would
        //     enforce the property that no node in the transient table has a maximal
        //     value, hence the call to [is_maximal] above would become useless.

        var isMaximal = lattice.isMaximal(newProperty);
        if (!isMaximal) {
            for (S child : currentChildren) {
                mParents.computeIfAbsent(child, _ -> Collections.newSetFromMap(new IdentityHashMap<>()) ).add(current);
            }

            // If the updated value differs from the previous value, record
            // the updated value and send a signal to all observers of [node].
            if (!lattice.equality().test(mTransient.get(current), newProperty)) {
                mTransient.put(current, newProperty);
                //signal the parents because the current value changed
                mWorkset.addAll(mParents.get(current));
            }
        } else {
            //current got at the top of the lattice
            mFixed.put(current, newProperty);
            var oldProperty = mTransient.remove(current);
            if (!lattice.equality().test(oldProperty, newProperty)) {
                //signal the parents because the current value changed
                mWorkset.addAll(mParents.get(current));
            }
        }
    }
}
