# obp3-algos

The central implementation module. It depends on `obp3-runtime` and owns graph traversal, semantic composition, SCC and model-checking algorithms, fixed-point evaluation, unification/logic solving, and custom data structures.

## Build Boundary

- Gradle project: `obp3-algos`; JPMS name: `obp.algos`.
- Java 23 toolchain; depends on `project(':obp3-runtime')`; uses JUnit 5.
- [`module-info.java`](../obp3-algos/src/main/java/module-info.java) exports selected packages. A public Java class in an unexported package is not automatically accessible to downstream named modules.

## Package Map

- `obp3.sli.core`: [`RootedGraphFunctional`](../obp3-algos/src/main/java/obp3/sli/core/RootedGraphFunctional.java) and adapters between rooted graphs and semantic relations.
- `obp3.sli.core.operators`: graph rerooting and relation-to-graph conversion; `product/` implements synchronous products, with deterministic variants in `product/deterministic/`.
- `obp3.traversal.dfs`: public [`DepthFirstTraversal`](../obp3-algos/src/main/java/obp3/traversal/dfs/DepthFirstTraversal.java) facade. `semantics/` has WHILE, DO, and relational implementations; `domain/`, `model/`, and `defaults/domain/` hold configuration, callbacks, parameters, and tree/set-backed storage.
- `obp3.traversal.bfs`: imperative and relational breadth-first traversals and their configuration/actions.
- `obp3.scc`: two Tarjan implementations. `tarjan1/` embeds SCC data in the DFS configuration; `tarjan2/` separates algorithm state into [`TarjanMemory`](../obp3-algos/src/main/java/obp3/scc/tarjan2/TarjanMemory.java) and callbacks.
- `obp3.modelchecking`: result/status contracts, safety DFS, Büchi nested-DFS variants (`naive`, `cvwy92`, `gs09`, separated and CDLP05 variants), and builder/model APIs under `tools/`.
- `obp3.fixer`: generic lattice/fixpoint evaluation. [`Fixer`](../obp3-algos/src/main/java/obp3/fixer/Fixer.java) owns transient/fixed tables and dependency edges; [`Lattice`](../obp3-algos/src/main/java/obp3/fixer/Lattice.java) defines bottom/top/equality.
- `obp3.unification`: term unification, answer values, occurs checking, and substitution. Syntax types are in `unification/syntax/`.
- `obp3.uslg.syntax`: substitutions, rules, answer sets, alpha-equivalence, and the tabled SLG solver.
- `obp3.datastructures.collections.linearscan`: linear-probing set/map and shared tombstone/growth logic.
- `obp3.hashcons`: hash-consing abstractions and lambda-term examples, including wrapper/no-wrapper/expando approaches.
- `obp3.utils`: `Either`, `PeekableIterator`, and [`Hashable`](../obp3-algos/src/main/java/obp3/utils/Hashable.java).
- `obp3.things`: sample/generated graphs and a small runnable traversal entry point. `obp3.Execution` supports the experiments UI.

## Useful Traces

- To change DFS behavior, follow `DepthFirstTraversal` -> `IDepthFirstTraversalConfiguration` -> a semantics implementation -> its callback model. The ordinary test families are `z2mc/traversal/dft/TestDFTWhile.java`, `TestDFTDo.java`, `TestDFTRelational.java`, and `TestDFTTree.java`.
- To change a model checker, follow a checker class and its callbacks/configuration, then the `tools/` model/builder that selects it. Tests are under `z2mc/buchi/` and `z2mc/safety/`.
- To change semantic composition, inspect `StepSynchronousProductSemantics` and the relation/graph adapters, then test at the consumer layer.
- To change fixed-point or SLG behavior, start at `Fixer` and follow its `BiFunction`/`request` dependency graph; SLG tests are under `obp3/uslg/syntax/`.

## Tests

All discovered Java tests were under `src/test/java` in this module. Major groups:

- `z2mc/traversal/`, `z2mc/scc/`, `z2mc/buchi/`, and `z2mc/safety/` for graph algorithms and model checking.
- `obp3/datastructures/collections/linearscan/` and `obp3/hashcons/` for collections and hash-consing.
- `obp3/unification/` and `obp3/uslg/syntax/` for terms, answers, substitutions, and logic solving.

Run with `./gradlew :obp3-algos:test` from the project root.

## Static Review Notes

- The GS09 checker accepts a reducer but its `BuchiGS09BlueConfiguration` looks up and stores colors by original `V` vertices rather than reduced `A` values. Add tests with distinct vertices mapping to one reduced value before relying on state reduction in this checker.
- `Hashable.fromComparator` hashes with object hash while equality follows the comparator. `withPositiveHash()` uses `Math.abs`, which leaves `Integer.MIN_VALUE` negative; the linear table later uses `% capacity` as an array index. Existing tests miss this combination (capacity 16 masks the negative remainder for this integer).
- `LinearScanHashMap.keySet()`, `values()`, and `entrySet()` build detached snapshots, and entries are copies. This differs from the backed views required by `Map`; also review `equals`/`hashCode` for the custom map and set.
- `PeekableIterator` uses null as its no-cached-value marker, so a real null value cannot be reliably peeked. Its callers include relational BFS/DFS.
- `Unificator.solve` should have a regression for `X = X`; the candidate may then be rejected by the occurs check. Its fixpoint is stored on the instance, so also review repeated `solveTop` use if that lifecycle becomes public.
- Raw `Map`/`Function` types in `Fixer` and `ModelCheckerBuilder` weaken type safety and produce IDE diagnostics. Prefer fixing these while touching those APIs, with focused tests.