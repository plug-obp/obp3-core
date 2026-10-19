# obp3-experiments

Application and research sandbox. It combines a JavaFX execution list, trace-monitoring prototypes, examples, JMH benchmarks, and GraalVM native-image configuration. Keep production algorithms and contracts in `obp3-algos`/`obp3-runtime` instead.

## Build Boundary

- Gradle project: `obp3-experiments`; JPMS name: `obp.experiments`.
- Java 23; depends on `obp3-runtime` and `obp3-algos`.
- Build plugins/configuration include JavaFX 23, Ikonli, JMH, and GraalVM Native Image.
- [`module-info.java`](../obp3-experiments/src/main/java/module-info.java) exports `obp3.fx`; monitoring packages are not exported to downstream named modules.
- No Java test sources were present at review time.

## Source Map

- `obp3.fx/`: [`JFXExecutableListApp`](../obp3-experiments/src/main/java/obp3/fx/JFXExecutableListApp.java) assembles DFS tasks over generated graphs; [`ExecutionView`](../obp3-experiments/src/main/java/obp3/fx/ExecutionView.java) displays controls/results for `obp3.Execution`.
- `obp3.monitoring.trace/`: [`TraceSemantics`](../obp3-experiments/src/main/java/obp3/monitoring/trace/TraceSemantics.java) turns successive input measurements into timed `Step` values; `TraceConfiguration`, `TraceAction`, and `TraceTiming` hold state/action/timing data.
- `obp3.monitoring.inclusion/`: [`TraceInclusionOperator`](../obp3-experiments/src/main/java/obp3/monitoring/inclusion/TraceInclusionOperator.java) advances a left-hand trace producer and a set of right-hand specification states; `TraceInclusionAction` wraps a left action.
- [`Fibonacci`](../obp3-experiments/src/main/java/obp3/experiments/Fibonacci.java): compares recursive, memoized, iterative, and graph-traversal encodings.
- `src/jmh/java/obp3/bench/`: `FibonacciBench`, `DFSBench`, `DFSBenchCallbacks`, and `BFSBench` benchmark traversal/recursion approaches.

## Validation And Caveats

- There are no tests in this module at review time. Run `./gradlew :obp3-experiments:test` after adding focused coverage.
- `TraceInclusionOperator` has `lhs` and `rhs` fields but no constructor/setter or in-repository wiring was found. Its default instance cannot run `initial()` successfully until those dependencies are supplied; treat it as an unfinished prototype.
- The JMH configuration sets `includes = ["FibonacciBench"]`, so DFS/BFS benchmarks may be excluded from the configured JMH run. `BFSBench.doBFS` and `doFlatBFS` both instantiate `BreadthFirstTraversalDoFlat`; check benchmark intent before using the results.
- The JavaFX app and Native Image settings declare different main classes. Check `application.mainClass` versus `graalvmNative.binaries.main.mainClass` before changing launch/build behavior.
- `DESSemantics` and `obp3-algos` findings are summarized in their respective module guides; experiments that benchmark or display those APIs inherit their behavior.