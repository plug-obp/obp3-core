# Design note: ten narrow fixes from the 2026-10-20 audit

Branch: `audit-261020-quick-fixes` (from `main` @ `e8d1476`)
Source: `audit-261020-e8d1476db736adf47e8e5627b350939830e5c8d2` (finding IDs below refer to it)

## Goal and selection rules

Pick the audit findings that can be fixed **today**, each in a few lines, with:

- no change to public signatures, constructors, result types or module exports;
- no new defensive checks or null/argument guards sprinkled through callers;
- a regression test that fails before and passes after;
- no dependence on the unresolved P1 design questions (see "Deliberately out of scope").

Every fix is an independent commit. Order below is the suggested order (cheapest and
most isolated first). Baseline before starting: `./gradlew build` (632 tests pass per audit).

## The ten items

### 1. A8 – GS09 drops its blue `depthBound`
- **Where:** `obp3-algos/.../ndfs/gs09/EmptinessCheckerBuchiGS09.java:101`.
- **Change:** use the overload that takes the bound, as the CDLP05 variant already does
  (`:75`): `new DepthFirstTraversalParameters<>(graph, depthBound, reducer, blueCallbacks)`.
- **Test:** graph `0→1, 1→1`, accepting `{1}`, bound 1. All six variants must stop at the
  same place (GS09 currently finds the cycle at vertex 1, the other five do not). Add a
  parameterized test over the six algorithms in `z2mc/buchi`.
- **Client impact:** only callers that passed a bound and relied on it being ignored.

### 2. A7 – nested-red worklist statistic accumulates itself
- **Where:** the four GS09-family `dfsRed` methods:
  `gs09/EmptinessCheckerBuchiGS09.java:173`, `gs09/separated/...:161`,
  `gs09/cdlp05/EmptinessCheckerBuchiGS09CDLP05.java:149`, `gs09/cdlp05/separated/...:189`.
- **Cause:** `new EmptinessCheckerStatus(0, status.worklistSize)` is evaluated inside the
  callback, so the base is the live, already-accumulated field.
- **Change:** use the already captured `prefixStatus.worklistSize` as the base
  (`new EmptinessCheckerStatus(0, prefixStatus.worklistSize)`), created once before `run`.
  Keep `knownSize` base as is.
- **Test:** `0→1, 1→1`, accepting `{0}`: record worklist sizes seen by the termination
  predicate during red search; assert they never exceed blue-stack size + red-stack size
  (audit observed `3,5,7,8`; and 22 on the four-vertex graph). Same test for all four classes.

### 3. PEEK – `PeekableIterator` loses a peeked `null` (DONE)
- **Where:** `obp3-algos/.../utils/PeekableIterator.java`.
- **Change:** replace `peeked == null` as "empty" with a `boolean hasPeeked` field;
  `equals/hashCode/toString` keep reporting `peeked` and add the flag.
  Do not reject nulls (that would be a client-visible change).
- **Test:** `[null, "next"]`: `peek()`→null, `next()`→null, `next()`→"next", `hasNext()`→false;
  plus the non-null cases to show unchanged behavior.

### 4. LS2 – `LinearScanHashSet.removeAll(this)` throws after partial mutation (DONE)
- **Where:** `LinearScanHashSet.java:347-355`.
- **Change:** when `c == this`, `clear()` and return `!wasEmpty`. Otherwise unchanged.
  (A general "snapshot the argument" copy is not needed; only aliasing breaks.)
- **Test:** empty, single, multiple elements, and a set with collisions/tombstones; assert the
  return value follows the documented `removeAll(S, C) = true ⟺ ∃x ∈ S ∩ C`. Extend
  `LinearScanHashSetTest`.

### 5. S1 – initial-state witness crashes the unified builder
- **Where:** `EmptinessCheckerAnswer.map` (`EmptinessCheckerAnswer.java:44-50`), used by
  `ModelCheckerBuilder.modelChecker()` with `Either::left` / `Either::right`.
- **Cause:** an initial violation has witness `Step(null, empty, root)`; the mapper (and
  therefore `Either`, which rightly rejects null) is applied to the null source.
- **Change:** in `map`, apply the mapper to `start()` only when it is non-null, and keep
  `witness.action()` instead of replacing it with `Optional.empty()`. Do **not** touch
  `Either`'s non-null contract. The null start remains the existing "no predecessor" sentinel
  that the checkers already produce, so no new representation is introduced here.
- **Test:** build through the unified `ModelCheckerBuilder` (safety state predicate true in the
  initial state; Büchi/product with an initially accepting property). Expect `holds=false`,
  a witness whose end maps to the root, no exception. Add also a non-initial violation as a
  control.
- **Client impact:** only turns a thrown NPE into the documented result.

### 6. S4 (part 3) – `Execution` stays `running=true` after a failure
- **Where:** `obp3-algos/.../Execution.java:35-47` (`worker()`).
- **Change:** wrap the body in `try { ... } finally { running.set(false); }` so a thrown
  exception does not leave the execution advertised as running forever. Do not change pause/stop
  semantics (S4 parts 1–2 need a lifecycle design; see out of scope).
- **Test:** an `executableMaker` that throws; after the worker thread ends, `running` is false
  and a subsequent `start()` is accepted (join on the worker thread; no sleeps).

### 7. A5 – a second `run()` mutates and returns the previous answer
- **Where:** `safety/SafetyDepthFirstTraversal.java:28,65-69` and the equivalent `result` field
  in each of the six Büchi checkers.
- **Change (minimal):** create the answer at the start of `run(...)` instead of holding one
  forever, and flip the trace once on that fresh object. The callbacks keep writing to the
  field, which is simply re-assigned per run; no new API and no new locking.
- **Test:** graph `0→1, 1→1`, accepting `{1}`. Run twice on the same instance: both results
  equal `[0,1,1]` and the first result object is not modified by the second run. Safety:
  failing run, then predicate disabled, second run reports `holds=true`.
- **Note:** do this item **after** items 1–2 so the six checkers are touched in one pass of
  review. This item does not reset color maps or weights in separated variants; that is part of
  the broader reuse decision (audit improvement 1) and is not claimed solved.

### 8. A6 – separated red DFS clears the shared blue visited set
- **Where:** `gs09/separated/...:154-157`, `gs09/cdlp05/separated/...:181-184`, with
  `DFTConfigurationSetDeque.initial()` (`:28-30`) clearing `known`.
- **Change:** in the red configuration used by the two separated variants, override
  `initial()` so that it resets only the local stack and leaves the shared `known` set alone
  (no change to `DFTConfigurationSetDeque` for other users).
- **Test:** audit repro `0→1, 1→1`, accepting `{0}`: status snapshots must never show the known
  size dropping (currently 2 → 0 → 1). Plus a differential check that verdicts of the separated
  variants still equal the optimized variants on all directed 3-vertex graphs (the audit
  probe `FocusedAlgorithmAudit` is the model).
- **Risk:** medium of the ten; commit last among code fixes and run the whole `z2mc.buchi` suite.
  If a verdict difference appears, drop this item and carry it to the shared-memory ownership
  work instead of adding patches.

### 9. Document the two semantic conventions the audit flagged (docs only)
- **Where:** a short section in `README.md` or a new `docs/semantics.md`.
- **Content:** (a) which observation (source / action / target) is consumed on each
  transition, and that the initial state is consumed by the first real or stutter step;
  (b) the synthetic model-stutter at deadlock in safety/Büchi products and its interaction with
  the reserved deadlock atom; (c) that `SemanticRelation` implementations may mutate in place
  and products keep references, so clients must supply snapshot-safe (pure) semantics, as
  Soup's `pureSemantics()` does.
- **Why now:** these are audit improvements 2 and 3. Writing down current behavior costs
  nothing and changes nothing; policy changes stay a separate decision.

### 10. Characterization tests for untested, currently-correct code (DONE)
- **Where:** new tests under `obp3-algos/src/test/java/obp3/` (BFS, plus the public builder
  happy path).
- **Change:** port `BfsAndUtilityAudit` as a small exhaustive test: all directed 3-vertex graphs
  × all root subsets, four BFS implementations, reachability checked against a closure computed
  in the test (16,384 runs in the audit, 0 mismatches). Measure runtime first; keep it only if
  it is a few seconds, otherwise restrict to 2-vertex graphs plus a fixed seeded sample.
- **Result:** `z2mc.traversal.bfs.TestBreadthFirstReachability` runs the full 3-vertex enumeration for all four
  BFS variants (4 × 4,096 runs, ~30 ms total), so no reduction was needed. The public builder happy path
  was not added here; it remains with item 5 (S1).
- **DFS extension:** `z2mc.traversal.dft.TestDFTReachability` applies the same enumeration to the three DFS
  algorithms (`WHILE`, `DO`, `RELATIONAL`), unreduced. Besides the known set, it checks that the entry callback
  fires exactly once per reachable vertex and that every reported entry edge exists in the graph.
- **Why:** closes the "no dedicated BFS tests" gap without touching production code and gives
  the later P1 work a safety net.

## Reducer-related work is deferred

All reducer/state-key findings are postponed to a later increment: A1 (nonidentity reduction
unsound in four Büchi variants, ignored in two, reduced Tarjan crash), the state-key
consistency test family, quotient-witness representation, and the "reduced checking" witness
labeling. For this branch:

- No item changes how a `reducer` is consumed or passed. Item 1 only adds `depthBound` to the
  existing call and keeps the `reducer` argument untouched.
- All new tests (items 1–8, 10) run with the identity/unreduced configuration only; none
  asserts behavior under a non-identity reducer, and none adds guards for it.
- If a fix is found to interact with a reducer (e.g. item 8's shared `known` set, which is keyed
  by reduced state), keep the change identity-neutral and record the question for the
  reducer increment instead of handling it here.

## Deliberately out of scope (and why)

These are real, but they need a design decision or change client-visible results, so they must
not be patched around here:

- A1 state-identity under reduction (deferred, see above), A2 completion reason, A3 global visited set, A4 relational
  stop, HC1/HC2 hash-cons ownership: P1 contract work; results/API will change.
- S3 Büchi mode silently becoming reachability: rejecting it breaks clients; needs a decision.
- S4 parts 1–2 (generation sharing, stale permits), LS1 hash policy, U1/SLG internal solvers.
- I1 CVWY92 secondary visited set: needs the algorithm's proof invariant and, because of A6,
  must come after shared-memory ownership is settled.

## Execution plan

1. Run baseline `./gradlew build`; note the 632 passing tests.
2. For each item 1–8: write the failing test, make the change, run
   `./gradlew :obp3-algos:test --tests '<new test class>'`, then the full module tests; one
   commit per item (`audit(<ID>): <summary>`).
3. Items 9–10 as separate commits (docs; tests only).
4. Final `./gradlew build`. Expected: baseline count + new tests, zero failures.
5. Open a PR listing the audit IDs closed (A5, A6, A7, A8, S1, S4-part-3, PEEK, LS2) and the
   out-of-scope list above, so the remaining findings stay visible.

## Done criteria

- Each of items 1–8 has a test that was observed failing before the change.
- No public signature, module export or non-bug behavior changed; no new null/argument
  guards added elsewhere.
- Full build green.
