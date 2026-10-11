# Fixer: contract, algorithm and qualification

Reviewed 2026-10-12. This document describes the hardened Java solver in
[`obp3.fixer`](../obp3-algos/src/main/java/obp3/fixer/Fixer.java), its client
obligations, failure/cancellation behavior and qualification evidence.
The baseline audited was `ebce326e00b7be217ae0c58f41881ece29dc4690`; its
[original source remains preserved](https://github.com/plug-obp/obp3-core/blob/ebce326e00b7be217ae0c58f41881ece29dc4690/obp3-algos/src/main/java/obp3/fixer/Fixer.java).
The current fixes retain that implementation's map-based structure. The broader
`Node`/`Run` prototype was measured at
[`7f7dab9a3c184803760764d1e7247346ad8b6af8`](https://github.com/plug-obp/obp3-core/commit/7f7dab9a3c184803760764d1e7247346ad8b6af8);
further refactor work is kept separately on `codex/fixer-refactor`. The
[refactor assessment](fixer-refactor-evaluation.md) records its necessity,
measured tradeoffs and limits. The pinned measurement does not depend on the
proposal branch's current tip.

## What the solver computes

For a fixed system of equations `value(x) = equation(x, value)`, `Fixer` computes
the least solution for a requested variable and discovers its dependencies on
demand. Recursive and mutually recursive equations are allowed. Completed
solutions are cached for subsequent requests in the **same equation system**.

The design follows Pottier's
[Lazy Least Fixed Points in ML](https://cambium.inria.fr/~fpottier/publis/fpottier-fix.pdf)
and the [Fix library contract](https://gallium.inria.fr/~fpottier/fix/doc/fix/Fix/index.html).
It requires monotone equations, a least property, finite ascending chains and
finite demand. Bottom and equality suffice for this algorithm; it does not
require a join/order API. The Java lifecycle guarantees below are additional
engineering contracts, not a proof transferred from the original implementation.

```java
Map<String, List<String>> graph = Map.of(
        "a", List.of("b"),
        "b", List.of("a", "c"),
        "c", List.of());

Fixer<String, Set<String>> reachable = new Fixer<>((node, request) -> {
    var result = new HashSet<String>();
    result.add(node);
    for (String successor : graph.get(node)) {
        result.addAll(request.apply(successor));
    }
    return Set.copyOf(result);
}, new Lattice<>(Set.of(), Objects::equals), HashMap::new);

reachable.apply("a"); // {a, b, c}
reachable.apply("c"); // {c}; already solved and cached
```

Imports are `obp3.fixer.*` and `java.util.*`. The mutable temporary `result` is
local to one evaluation; returned and requested property values are immutable.
Graph traversal is one example, not a special case in the solver.

## Client obligations

| Concern | Contract |
|---|---|
| Equations | Pure, deterministic for a given valuation, monotone, and unchanged throughout this solver's lifetime. Dynamic dependency discovery is supported; changing the equations is not. |
| Domain | A partial order with a least element and finite ascending chains for termination. Equality must reflect equality in that order, and equations must respect it. Optional top must be greatest. |
| Properties | Non-null and immutable, or treated as immutable by all participants. Never mutate bottom, a requested approximation or a returned cached value. The solver cannot copy arbitrary client data. |
| Variables | Non-null, with stable key equivalence and hashing/comparison. Finite property height alone does not ensure termination if equations keep requesting new variables. |
| Dependency reads | Use only the provided synchronous `request` callback. No recursive public `apply`, even for a cached variable. Do not save a callback for another equation invocation or use it on another thread. |
| Maps | The factory returns fresh, empty, mutable, strongly retaining maps with consistent key equivalence and no restrictions on the internal value type. Do not retain/mutate supplied maps. Map/key operations must have stable, non-throwing ordinary behavior. Weak or evicting maps are unsuitable. |
| Sessions | Construct a new solver when the program, graph, rules or other equation inputs change. This API does not invalidate cached solutions across edits. |

The two-argument constructor deliberately preserves **identity-key** behavior
using `IdentityHashMap`. Two equal but distinct objects can denote different
variables. Use `HashMap::new` for structural keys, as the SLG solver does. Fresh
equivalent keys on recursive requests require structural equality or explicit
canonicalization; identity maps do not merge them automatically.

`Lattice` has a required bottom, optional top (`null` means absent), and a required
equality predicate. `isMaximal` uses this predicate, not Java reference identity
or an unrelated `Object.equals`. The solver does not infer order, compute joins,
validate monotonicity or apply widening. For powersets, the equation computes its
own unions/intersections. Do not silently add a join to mask invalid equations.

## Calls, cancellation and failure

`apply(variable)` computes a completed solution. The overload
`apply(variable, cancellationRequested)` adds cooperative cancellation:

```java
AtomicBoolean cancelled = new AtomicBoolean();
try {
    Set<String> answer = reachable.apply("a", cancelled::get);
    // answer is completed, never a partial approximation
} catch (CancellationException cancelledRun) {
    // No newly computed result from this run was published.
}
```

The additional imports are `java.util.concurrent.CancellationException` and
`java.util.concurrent.atomic.AtomicBoolean`. Another thread may set the flag;
the cancellation predicate runs on the solving thread and should only observe
the flag. Cancellation is checked before cache access, between equation
evaluations, at each request, after an equation returns and before publication.
Once observed, it remains latched for that run, even if client code catches the
exception and resets its flag. An arbitrary blocking equation cannot be
preempted; cancellation requires reaching a solver checkpoint. A flag changed
after the final checkpoint can race with successful completion.

An uncaught equation, lattice or cancellation-predicate exception propagates.
Its run's approximations, including newly discovered top values, are discarded;
previously completed cached results remain available. Retry begins with fresh
transient state. Request callbacks expire in `finally`, including after an RHS
throws. This does not undo external side effects or promise recovery from
malicious/broken map operations or process-level resource failure. Conditions
required by the fixed-equation contract still apply after a retry.

Only one public call may be active. Concurrent/reentrant calls throw
`IllegalStateException` immediately, including cache hits, without blocking the
active solve. A callback also throws `IllegalStateException` if expired or used
on a different thread. These guards run with assertions disabled. Sequential
calls from different threads are supported, provided the client also respects
the property/equation contracts. The solver is not a parallel fixed-point engine.

The internal cache retains requested completed solutions for the solver's
lifetime. Discard the solver to release that cache. Dependency graphs and queues
belong only to one run and are cleared on completion, failure and cancellation.
Retaining an expired callback grants no access to a later run.

## Algorithm and invariants

The solver retains the original tables and queue, with variables represented
directly as keys and set elements:

| Field | Role |
|---|---|
| `mFixed` | Permanent solutions published only by completed runs. |
| `mTransient` | Current approximations, including newly discovered top values, until the run completes. |
| `mParents` | For each variable, the equations observing its approximation. |
| `mChildren` | For each equation, the variables it currently observes. |
| `mWorkset` | FIFO queue of variables awaiting equation evaluation. |

The supplied map equivalence also governs dependency sets and queued membership.
There are no canonical node wrappers or separate run objects. Small helpers
maintain scheduling and cleanup around the existing `apply`, `ensureTransient`
and `solve` flow.

1. Discovering a new variable gives it bottom and queues it once.
2. A queued equation has no outgoing dependency edges. Dequeuing it begins a
   new evaluation, during which requests record the values it currently observes.
3. Requests return existing approximations; they do not recursively evaluate
   dependencies on the Java stack. New dependencies are queued.
4. On a property change, the enqueue helper detaches each observer from its old
   children and adds it to the queue only if it is not already queued. A later
   evaluation establishes its current dependency set. Copying the observer set
   before notification permits this detachment safely.
5. A top-valued evaluation skips registering its collected dependencies, leaving
   the equation with no outgoing edges. Its value cannot grow further under the
   domain/monotonicity contract, but it remains private to the run in `mTransient`
   until successful completion.
6. Queue exhaustion means all demanded equations are stable. After the final
   cancellation check, publish `mTransient` into `mFixed`. The `finally` cleanup
   clears the transient table, every parent/child set and its backing map, the
   graph tables, and workset/membership bookkeeping. Earlier uncaught failure or
   cancellation reaches the same cleanup without publishing those results.

Self-dependencies participate in the same rules. A changed self-dependent node
is requeued; an unchanged bottom cycle stabilizes at bottom. A top-valued node
does not repeatedly notify itself. Queued membership prevents duplicate work when
several children change before their shared parent is reevaluated.

Under stable monotone equations, increasing dependency valuations cannot make a
reevaluation decrease. Starting from bottom therefore stays below the least
solution. Finite demand, finite ascending chains and notification of changed
dependencies lead to quiescence. This is the algorithm's justification under
its stated hypotheses, not a mechanized Java correctness proof. Neither finite
tests nor a fixed point of an abstract model proves soundness of a client's
abstraction or termination of the program being analyzed.

## Findings and corrections

| Baseline finding | Correction and regression |
|---|---|
| Nested public `apply` bypassed dependency tracking and incremented `activeCount` without balancing it. The earlier probe returned `false` for equations whose least solution was `true`, including a subsequent query. | Reject all nested public calls before accessing cache/state; use the proper callback. Test cached/uncached nesting and recovery. |
| An RHS exception left a polled transient node unscheduled. A retry could publish bottom without reevaluating it; partial top results could already have entered the permanent table. | Keep all new values in `mTransient` until successful publication, and clear transient state in `finally`. Test exceptions, `Error`, lattice failure, retry and preservation of earlier completed cache. |
| Request expiry used only an assertion, and an exception bypassed expiry entirely. | Runtime owner-thread/lifetime checks and `finally` closure. Test successful/failed RHS, later equations, wrong threads and a separate `-da` JVM. |
| Work notifications could enqueue duplicate observers without detaching old dependencies. Dependency tables survived completed runs. | Preserve variable-keyed maps/sets; add unique queue membership, detach-on-enqueue and `finally` cleanup of graph sets and tables. Test shared/removed dependencies and supplied-map cleanup. This was an invariant/retention problem; no separate valid-callback wrong-result case was established from duplicate scheduling alone. |
| Top recognition used reference identity. | Use domain equality; test fresh equivalent top values, including a custom equality different from `Object.equals`. |
| Null values were confused with absent cache entries. | Reject null properties/bottom/results and document non-null keys. Optional missing top remains supported. |
| There was no cooperative cancellation or protection against concurrent public entry. | Add cancellation overload and fail-fast active-call guard; test cancellation before cache access, during iteration, after RHS, before publication, caught cancellation and subsequent reuse. |
| `Unificator.solveTop` replaced constraints while retaining the same Fixer. Old returned valuations could read new equations or stale cached answers. | Each returned valuation now captures its own decomposed constraint snapshot and solver; test cached and previously unqueried variables across sessions. The unification algorithm itself is unchanged. |

The normal exception-free request-callback protocol passed the finite oracle
qualification on the baseline as well as on the hardened implementation. The
audit does not claim a general wrong-result defect in proper callback solving.
It found invalid lifecycle paths, missing enforcement and implementation
invariants that matter for dependable clients.

### Compatibility

Existing constructors and `Function<S,T>` use remain available, with the default
identity semantics unchanged. Existing proper-callback SLG and unification tests
must continue to pass. The map-factory parameter continues to accept existing
`Supplier<Map>` variables. A compile-time regression exercises this legacy call
form. The factory's value-polymorphic contract applies to property tables,
dependency tables and backing maps for variable sets.

Intentional tightening: null variables/properties are rejected; all public
reentrancy, including cached nested reads, is rejected; stale/cross-thread
callbacks fail regardless of `-ea`. The original Pottier interface permitted
cached nested reads; this Java API deliberately requires one dependency-read
protocol. Clients depending on these formerly permissive paths must migrate.

No cache-reset API is supplied: a fresh solver makes equation/model boundaries
explicit and avoids invalidating functions already returned to clients. No
thread interruption policy or automatic timeout is imposed by default.

## Qualification suite

Run from the repository root with the configured JDK 23 toolchain:

```sh
./gradlew :obp3-algos:test --tests 'obp3.fixer.*'
./gradlew test
./gradlew build
```

The build includes the normal module descriptors and existing clients. The
focused suite is intentionally independent of implementation-private fields.
On 2026-10-12, `./gradlew build` passed with **595 tests, zero failures, errors or
skips**, including 40 new tests across the four classes below. The original
baseline also passed `./gradlew test`; the added qualification distinguishes
normal solving from the lifecycle defects that its old tests did not cover.
Test locations:

* [FixerContractTest](../obp3-algos/src/test/java/obp3/fixer/FixerContractTest.java):
  lifecycle, cancellation, dependency changes, cache and map contracts.
* [LatticeTest](../obp3-algos/src/test/java/obp3/fixer/LatticeTest.java): bottom/top,
  required operations and semantic equality.
* [FixerQualificationTest](../obp3-algos/src/test/java/obp3/fixer/FixerQualificationTest.java):
  independent reference solvers, exhaustive finite systems and depth/cycle probes.
* [UnificatorSessionTest](../obp3-algos/src/test/java/obp3/unification/UnificatorSessionTest.java):
  equation-session isolation, alongside existing unification/SLG tests.

| Qualification | Coverage and independent oracle |
|---|---|
| Exhaustive Boolean systems | All 8,039 monotone systems with one through three variables; every query permutation; with/without top; 96,150 solver sessions and 576,588 queries. RHS uses short-circuit minimal DNF; oracle synchronously iterates truth tables from bottom. |
| Seeded powerset reachability | 250 directed graphs with cycles; three query orders; with/without top; 16,680 queries checked against explicit graph traversal. |
| General powerset equations | 300 six-variable systems over five facts using unions/intersections; three query orders; with/without top; 21,600 queries checked against synchronous bitset iteration. |
| Conditional recursive demand | Growing dependencies reveal a recursive component across all 24 query orders, with/without top; 384 queries. |
| Key policies | Identity-distinct equal variables and structural recursive requests using freshly allocated equal keys. |
| Stack/resource sanity | A 20,001-node dependency chain and a 10,000-node recursive component; dependency solving does not recurse on the Java stack. These are bounded checks, not performance guarantees. |

Seeds and coverage counts are assertions in the suite. Oracle iteration includes
independent bounds appropriate to the finite domains. Qualification covers the
stated equations, key policies and lifecycle scenarios; it does not establish
correctness of arbitrary non-monotone equations, mutable properties, foreign
effects, unbounded demand, custom broken maps or a future VarioHyve abstract
semantics. For editor analysis, use a new solver per immutable snapshot/model and
report cancellation separately from successful analysis completion.
