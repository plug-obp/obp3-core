# Fixer implementation comparison

Standalone JMH 1.37 comparison of the same fixtures compiled against two committed
`Fixer`/`Lattice` versions. This does not alter the project's dependencies or use
the experiments module's JavaFX/toolchain setup.

The recorded comparison uses the hardened, map-preserving implementation at
`f713d572d8d8cdafa07cfd9c50247943e6c5a45a` and the Node/Run refactor at
`7f7dab9a3c184803760764d1e7247346ad8b6af8`. Both include lifecycle fixes absent
from the original baseline.

## Reproduce

Requires Python 3, Git, and a JDK supporting the compared Java sources (the
recorded run uses GraalVM Community JDK 23.0.2). The driver defaults to jars in the
local Gradle cache. Alternatively, obtain the four dependencies independently of
the project build:

```sh
mkdir -p /tmp/fixer-jmh-deps
curl --fail --location https://repo.maven.apache.org/maven2/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37.jar -o /tmp/fixer-jmh-deps/jmh-core-1.37.jar
curl --fail --location https://repo.maven.apache.org/maven2/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar -o /tmp/fixer-jmh-deps/jmh-generator-annprocess-1.37.jar
curl --fail --location https://repo.maven.apache.org/maven2/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar -o /tmp/fixer-jmh-deps/jopt-simple-5.0.4.jar
curl --fail --location https://repo.maven.apache.org/maven2/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar -o /tmp/fixer-jmh-deps/commons-math3-3.6.1.jar
python3 benchmarks/fixer/run.py \
  --java-home "$JAVA_HOME" \
  --dependency-dir /tmp/fixer-jmh-deps \
  --minimal-ref f713d572d8d8cdafa07cfd9c50247943e6c5a45a \
  --refactor-ref 7f7dab9a3c184803760764d1e7247346ad8b6af8 \
  --output /tmp/fixer-results
python3 benchmarks/fixer/summarize.py /tmp/fixer-results
```

Use a new output directory for each run. `--smoke` verifies the machinery quickly;
its un-warmed timings must not be used as performance results. Compilation occurs
in temporary directories that are removed when the driver exits. The script
needs local JVM fork/socket permissions for JMH; it does not contact a server.

## Workloads and units

Every fixture is verified before measurement, including every cached value.
Timed functions return their result so JMH consumes it. Inputs, equations and key
objects are created outside timed regions; fresh solver construction is included
in `coldSolve`.

| Workload | Equations and operation |
| --- | --- |
| `chain` | 2,000 variables, each reads its successor; final node is true. One fresh solver and root query. |
| `booleanCycle` | 2,000-node ring, one true seed, Boolean domain encoded as immutable `Long` 0/1. No top optimization. One fresh solver and root query. |
| `powersetRing` | 64-node ring, each unions its own bit with its successor's result. Immutable 64-bit `Long` powerset domain, no top optimization. All 64 bits must reach every node. One fresh solver and root query. |
| `independent` | 256 independent constant equations. One fresh solver and 256 public calls; a result is per whole session. |
| `topFanout` | 512 variables. Root requests 511 leaves then returns top; every leaf returns top. Tests deferred dependency recording versus immediate recording followed by detachment. One fresh solver and root query. |
| `cachedQuery` | Pre-solved 256 independent variables. Batches of 256 cached public calls, normalized with `@OperationsPerInvocation(256)` to one lookup. |

Each case runs with identity maps and structural maps. Both reuse one preallocated
request object per variable. This compares `IdentityHashMap` and `HashMap` with
simple record keys; it does not benchmark repeated equal-but-distinct cache hits
or expensive user equality/hash functions. The solver's qualification tests
separately cover structurally equal, distinct keys.
The harness measures cheap equations to expose solver overhead. It does not model
VarioHyve/AAM state growth, expensive RHS computations, cancellation/failure
traffic, adversarial hash collisions or concurrent contention.

Each case uses one thread, three independent JVM forks, three 500 ms warmup
iterations and five 500 ms measurement iterations per fork. Both use
`-Xms256m -Xmx256m -XX:+UseG1GC`. The driver alternates which implementation runs
first for each paired workload/key-policy case. In the primary run this means
maps first for identity cases and refactor first for structural cases. CPU affinity and other operating
system activity are uncontrolled. These are short microbenchmarks on one machine;
small differences and percentages need caution, and another JVM may behave
differently.

## What memory measurements mean

`gc.alloc.rate.norm` is JMH's GC-profiler estimate of allocated **bytes per
operation**, including allocations later collected. It is not retained memory.
Tiny nonzero values in an allocation-free cached loop are profiler noise.

The separate instrumentation agent sums `Instrumentation.getObjectSize` over
objects reachable from a completed solver, excluding the graph reachable from
the shared fixture (keys, equation, lattice and factory). It visits both arrays
and reference fields and fails on inaccessible fields. Three fresh JVM probes
must agree. This exposes persistent maps, their backing arrays and cached result
objects after transient cleanup, and also reports an empty solver's footprint.

This is a **solver-reachable object-graph footprint**, not a heap-dump dominator
analysis, exclusive retained heap, peak heap, RSS, or native/JIT/GC memory. Shared
boxed constants not already in the fixture can be counted. JVM object layout and
the selected heap size affect the result. No forced-GC heap-delta estimate is used.

## Recorded evidence

[The recorded run](results/2026-10-12-graal23/) includes source/harness/dependency
hashes, exact commands and JVM/machine metadata; complete JMH JSON and text logs
for each paired case; combined JSON; correctness output; and three footprint CSVs
per variant. `summary.csv` is generated from the combined JSON, with JMH's mean,
99.9% error interval and the range of per-fork means. The [assessment](../../docs/fixer-refactor-evaluation.md)
explains the findings and why they do not make refactoring a correctness prerequisite.

JSON trailing blank lines and summary CSV line endings are normalized for version
control; measurement values are unchanged.

The [reversed-order record](results/2026-10-12-graal23-reverse/) checks two large,
contrasting effects using the unchanged Java fixtures and the same fork/warmup
settings. Add these options to `run.py`, with a new output directory:

```sh
--case coldSolve:identity:chain --case coldSolve:structural:topFanout --reverse-order
```

The primary driver did not yet record timestamps or dependency digests during
execution. Its metadata explicitly labels the later dependency-hash enrichment
and the filesystem timestamp of the final raw output; it does not infer a start
time. The reversed-order run records start/end UTC timestamps directly.
