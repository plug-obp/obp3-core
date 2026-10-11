# Fixer refactor: necessity and performance evaluation

Date: 2026-10-12. Status: measured comparison; refactor deferred.

## Decision

Keep the original map-based implementation with the targeted correctness fixes.
The `Node`/`Run` refactor is not necessary for those fixes. Preserve it separately
and consider adopting it only if a repeatable benefit on representative workloads
justifies the larger implementation change.

In the recorded microbenchmarks, the refactor reduced fresh-solve time in nine
of ten cases and reduced the completed solver's reachable footprint in all ten.
It also made the structural-key top-fanout case about 61% slower and increased
allocation in two cases. These are useful workload-specific findings, not a
general claim that the refactor is faster or uses less memory.

Both implementations passed the same 595-test build, including the 40 added
contract, lifecycle and finite-domain qualification tests described in
[the solver contract](fixer.md). This supports comparing their representations;
it is not a proof for arbitrary equations or a future VarioHyve abstract domain.

## Compared implementations

| Implementation | Source and scope |
|---|---|
| Hardened maps | [`f713d572d8d8cdafa07cfd9c50247943e6c5a45a`](https://github.com/plug-obp/obp3-core/blob/f713d572d8d8cdafa07cfd9c50247943e6c5a45a/obp3-algos/src/main/java/obp3/fixer/Fixer.java). Retains `mFixed`, `mTransient`, `mParents`, `mChildren`, `mWorkset` and the original `apply`/`ensureTransient`/`solve` organization. |
| Node/Run refactor | Measured prototype [`7f7dab9a3c184803760764d1e7247346ad8b6af8`](https://github.com/plug-obp/obp3-core/blob/7f7dab9a3c184803760764d1e7247346ad8b6af8/obp3-algos/src/main/java/obp3/fixer/Fixer.java). Holds each variable's transient data in a canonical `Node`, with a separate `Run` for one solve. Further proposal work is kept separately on `codex/fixer-refactor`; these measurements remain pinned to the prototype, regardless of that branch's tip. |

The original baseline before lifecycle fixes at
[`ebce326e00b7be217ae0c58f41881ece29dc4690`](https://github.com/plug-obp/obp3-core/blob/ebce326e00b7be217ae0c58f41881ece29dc4690/obp3-algos/src/main/java/obp3/fixer/Fixer.java)
is historical evidence. Comparing its timings with the refactor would mix the
cost of lifecycle fixes with the choice of representation. The comparison here
uses the two hardened implementations instead.

## Why the refactor is optional

The required guarantees are independent of the representation. The map-based
implementation rejects public reentrancy, expires request callbacks, preserves
the chosen key equality, queues an observer only once, and clears its previous
dependencies before reevaluation. It keeps top values transient until successful
publication, discards failed or cancelled runs, and preserves earlier completed
cache entries. Small helpers and `finally` cleanup express these guarantees
without replacing the original tables with `Node` and `Run` classes.

The refactor groups related state and makes the lifetime of transient storage
explicit. Those are architectural tradeoffs to review separately. The same
dependency and least-fixed-point obligations apply to both implementations.

## Static cost differences

These are source-level differences, not measurements of Java object sizes or
proofs of better performance.

| Area | Hardened maps | Node/Run refactor |
|---|---|---|
| Variable state | Separate approximation and adjacency maps keyed by variables. | One variable-to-node map; fields hold a node's value and adjacency sets. |
| Dependency access | Repeated variable-key map/set operations. | A request finds a canonical node; subsequent graph operations use node references. |
| Adjacency representation | Two sets per variable, backed by maps from the chosen factory. | Two `LinkedHashSet` instances per node. Their entries include insertion-order links. |
| Queue | `LinkedList` plus a map-backed queued-membership set. | `ArrayDeque` plus a Boolean flag in each node. |
| RHS dependency collection | Collects requested variables in a temporary list; registers edges only for a non-top result. | Registers edges during requests; removes them if the result reaches top. |
| Transient storage after completion | Clears the existing tables, whose backing capacity can remain retained. | Discards the run table and nodes; their storage can become collectible. |

Both store a reciprocal dependency graph with `O(V + E)` logical storage for
`V` demanded variables and `E` current dependency edges. That bound does not say
how many bytes they use. Map choice, graph density, property size, allocation
patterns and JVM layout affect the constants.

Canonical nodes can avoid repeated client-key hashing or equality checks, but
they also allocate wrapper objects. `LinkedHashSet` entries differ from both
identity-map and ordinary hash-map backing storage. Cleared table capacity can
reduce allocation on later solves while retaining the largest capacity reached.
Top-heavy equations also exercise different edge-registration paths. A measured
difference therefore belongs to the whole compared implementation; it cannot be
attributed to `Node`/`Run` alone without another controlled experiment.

## Measurement record

The [standalone harness](../benchmarks/fixer/run.py) compiles the same fixtures
against each pinned solver revision. It does not change production dependencies.
The [recorded metadata](../benchmarks/fixer/results/2026-10-12-graal23/metadata.json)
contains source and harness SHA-256 hashes, resolved commands and machine details.

The run uses JMH 1.37, GraalVM CE Java 23.0.2, one benchmark thread, a 256 MiB
initial/maximum heap and G1 GC on an Apple M5 with 32 GiB RAM, macOS arm64. Each
implementation/case has three separate JVM forks, three 500 ms warmup iterations
and five 500 ms measured iterations per fork. Paired cases alternate which
implementation runs first. The GC profiler measures allocated bytes per
operation. Fixture construction and its correctness checks happen outside the
timed operation; solver construction belongs to each fresh-solve operation.

| Case | One timed operation |
|---|---|
| Chain | Construct a solver and solve a 2,000-variable dependency chain. |
| Boolean cycle | Construct a solver and solve a 2,000-variable cycle with one seed. No top optimization. |
| Powerset ring | Construct a solver and propagate 64 facts around a 64-variable ring, represented by immutable `Long` bit masks. No top optimization; this does not measure Java `Set` property allocation. |
| Independent queries | Construct one solver and issue 256 independent public queries. |
| Top fanout | Construct a solver whose root requests 511 children and returns top; finish the whole 512-variable run. |
| Cached lookup | One lookup in a completed cache, normalized from a batch of 256 public calls using JMH's `OperationsPerInvocation`. |

Every case runs with `IdentityHashMap` and `HashMap`. Both insert and look up
the same preallocated `requests[]` key objects. Although the structural fixture
also allocates an equal-but-distinct `keys[]` array, that array is not used for
solver queries. Thus this compares map implementations with simple `Key(int)`
records; it does not measure distinct-equal lookup cost. The qualification tests
cover distinct-equal key semantics independently. These fixtures also exclude
expensive client hashing, newly allocated query keys and real abstract
interpreter properties.

The record distinguishes elapsed time per operation, allocated bytes per
operation and the shallow-size sum reachable from the solver after completion,
excluding the shared fixture graph. The latter is a reproducible reachable
footprint, not a dominator-tree measurement of exclusively retained heap.
Allocation volume is not live heap, and neither measurement establishes peak
heap or process RSS. Cached queries are reported separately from fresh solves.

The [footprint probe](../benchmarks/fixer/src/obp3/bench/fixer/Footprint.java)
walks object references and sums `Instrumentation.getObjectSize`, excluding the
entire graph reachable from the input fixture. It measures both a newly created
solver and the solver after all fixture queries have completed. Three fresh JVM
runs are recorded for each implementation. This isolates reachable solver
storage under this JVM layout; it does not establish a peak-memory bound. Shared
boxed constants not reachable from the excluded fixture may still be counted.

### Fresh-solve time

Mean microseconds per operation, with JMH's 99.9% error half-width. Lower is
better. The independent case is a complete 256-query session; the others are
one fresh solver and root query. Values come from the
[generated summary](../benchmarks/fixer/results/2026-10-12-graal23/summary.csv),
with full fork data in the
[map-based JSON](../benchmarks/fixer/results/2026-10-12-graal23/minimal-jmh.json)
and [refactor JSON](../benchmarks/fixer/results/2026-10-12-graal23/refactor-jmh.json).

| Case | Map policy | Hardened maps, µs/op | Refactor, µs/op |
|---|---|---:|---:|
| Chain | identity | 692.1 ± 6.7 | 404.4 ± 17.2 |
| Chain | structural | 477.6 ± 3.1 | 340.8 ± 10.4 |
| Boolean cycle | identity | 709.7 ± 19.0 | 399.4 ± 14.2 |
| Boolean cycle | structural | 496.1 ± 55.6 | 336.3 ± 3.7 |
| Powerset ring | identity | 403.9 ± 22.4 | 138.8 ± 4.7 |
| Powerset ring | structural | 316.3 ± 3.9 | 131.9 ± 1.3 |
| Independent queries | identity | 76.5 ± 2.8 | 24.3 ± 0.5 |
| Independent queries | structural | 33.0 ± 0.9 | 18.5 ± 0.7 |
| Top fanout | identity | 96.8 ± 2.0 | 76.6 ± 5.7 |
| Top fanout | structural | 43.2 ± 1.5 | 69.4 ± 1.6 |

The largest improvements here occur on repeated powerset propagation and the
identity-map independent-query session. The structural top-fanout regression is
also substantial. The source offers hypotheses, including deferred versus eager
edge registration, but this benchmark does not isolate the cause.

Cached lookup means were 2.726 ± 0.104 versus 2.360 ± 0.045 ns for identity maps,
and 2.559 ± 0.038 versus 2.296 ± 0.020 ns for structural maps. Allocation was
effectively zero; tiny nonzero profiler values are noise. The absolute timing
differences are below half a nanosecond per lookup and are not an application
performance justification for refactoring.

### Allocation and completed footprint

All values below are KiB, where 1 KiB = 1,024 bytes. Each pair is
**hardened maps / refactor**. Allocation is per fresh-solve operation. Footprint
is measured after completion and all cached-value checks, not during solving.
The [map-based footprint CSV](../benchmarks/fixer/results/2026-10-12-graal23/minimal-footprint-1.csv)
and [refactor footprint CSV](../benchmarks/fixer/results/2026-10-12-graal23/refactor-footprint-1.csv)
contain exact byte counts; all three fresh-JVM repetitions agreed for each.

| Case | Map policy | Allocated KiB/op, maps / refactor | Reachable KiB, maps / refactor |
|---|---|---:|---:|
| Chain | identity | 2,271.3 / 1,502.7 | 128.74 / 32.14 |
| Chain | structural | 1,987.2 / 1,564.1 | 127.09 / 78.65 |
| Boolean cycle | identity | 2,240.2 / 1,456.2 | 128.74 / 32.14 |
| Boolean cycle | structural | 1,987.8 / 1,517.6 | 127.09 / 78.65 |
| Powerset ring | identity | 678.7 / 551.9 | 4.99 / 1.14 |
| Powerset ring | structural | 781.9 / 554.3 | 4.78 / 2.65 |
| Independent queries | identity | 209.3 / 175.9 | 5.49 / 4.14 |
| Independent queries | structural | 94.9 / 158.1 | 10.78 / 10.15 |
| Top fanout | identity | 494.7 / 282.1 | 40.47 / 8.12 |
| Top fanout | structural | 263.2 / 298.6 | 36.51 / 20.12 |

The refactor has a smaller completed footprint in every recorded fixture,
consistent with releasing transient containers while the map-based version
retains cleared table capacity. Empty-solver footprints were 1,712 / 376 bytes
with identity maps and 392 / 112 bytes with structural maps.

Allocation is a different result: the refactor allocated about 67% more in the
structural independent-query session despite completing it faster, and about
13% more in structural top fanout while also completing it slower. Therefore
neither the lower completed footprint nor the latency gains establish lower
allocation or peak memory for every client.

### Limits and reproducibility

This is one machine, one JVM and short measurement windows. CPU affinity and
other OS activity were not controlled. Although variant order alternates across
paired cases, the primary ordering correlates with map policy: maps run first
for identity cases and the refactor first for structural cases. A separate
[reversed-order check](../benchmarks/fixer/results/2026-10-12-graal23-reverse/summary.csv)
repeated two substantial effects with the same sources, fixture hashes, JVM
flags, forks and iteration settings:

| Reversed-order case | Hardened maps, µs/op | Refactor, µs/op |
|---|---:|---:|
| Identity chain, refactor first | 740.7 ± 60.7 | 412.0 ± 8.2 |
| Structural top fanout, maps first | 44.2 ± 2.1 | 69.3 ± 1.2 |

Both effects retained their direction. This checks those two cases rather than
eliminating scheduling or thermal effects across all workloads. The harness
measures inexpensive equations and excludes cancellation/failure traffic,
concurrent contention and adversarial hash collisions.

Reproduce from the repository root with a JDK 23 installation and the dependencies
already present in the Gradle module cache: JMH core/annotation processor 1.37,
jopt-simple 5.0.4 and commons-math3 3.6.1. Supply an unused output directory:

```sh
python3 benchmarks/fixer/run.py \
  --java-home /path/to/jdk-23 \
  --minimal-ref f713d572d8d8cdafa07cfd9c50247943e6c5a45a \
  --refactor-ref 7f7dab9a3c184803760764d1e7247346ad8b6af8 \
  --output /tmp/fixer-comparison-reproduction
```

`--gradle-cache` selects another dependency cache; the
[benchmark README](../benchmarks/fixer/README.md) also describes using standalone
dependency jars. `--smoke` checks the harness with one short, unwarmed fork; its
timings are not performance evidence.

## Adoption criteria

Use the measurements to identify a concrete benefit, then confirm it on the
intended clients. A faster synthetic solver does not by itself establish faster
editor diagnostics or lower memory for VarioHyve's abstract state. The proposed
first AAM experiment still starts with whole-state OBP3 reachability; `Fixer`
remains a later option for demand queries or joined-state equations.

Before proposing the refactor again:

1. Retain the same observable solver contract and qualification results.
2. Reproduce any useful speed or memory improvement, including regressions on
   cached and small-query workloads.
3. Identify which representation change produces the benefit. Compare narrower
   alternatives where appropriate, such as queue storage or release of cleared
   transient table capacity.
4. Show that the benefit matters for a representative workload and merits the
   extra code movement. Preserve both source histories and benchmark evidence.

Until then, keep the targeted fixes on the original implementation and retain
the broader refactor as a separate, reviewable proposal.
