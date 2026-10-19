# obp3-runtime

Small shared API module. Its job is to define execution, graph, and transition-semantics contracts; algorithm implementations live in `obp3-algos`.

## Build Boundary

- Gradle project: `obp3-runtime`; JPMS name: `obp.sli.runtime`.
- No project dependencies. Java 23 toolchain.
- [`module-info.java`](../obp3-runtime/src/main/java/module-info.java) exports `obp3.runtime` and `obp3.runtime.sli`.
- No Java test sources were present at review time.

## Source Map

- [`IExecutable`](../obp3-runtime/src/main/java/obp3/runtime/IExecutable.java): generic executable state/result contract, termination predicate, and `runAlone()` convenience method.
- [`IRootedGraph`](../obp3-runtime/src/main/java/obp3/runtime/sli/IRootedGraph.java): root and neighbor iterators plus cycle/sharing hints used to choose traversal configuration.
- [`SemanticRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/SemanticRelation.java): nondeterministic transition system with list-valued initial states, actions, and targets; includes iterator and IO adapters.
- [`DeterministicSemanticRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/DeterministicSemanticRelation.java): Optional-valued deterministic relation with side-effect/purity hints and a list-valued adapter.
- [`DependentSemanticRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/DependentSemanticRelation.java): input-dependent transition system.
- [`IOSematicRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/IOSematicRelation.java) and [`DeterministicIOSemanticRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/DeterministicIOSemanticRelation.java): input/output transition contracts and adapters. Preserve the existing `IOSematic` spelling in API references.
- [`Step`](../obp3-runtime/src/main/java/obp3/runtime/sli/Step.java): start/action/end transition value; [`IStepEvaluator`](../obp3-runtime/src/main/java/obp3/runtime/sli/IStepEvaluator.java) evaluates expressions over a step.
- [`IteratorSemanticRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/IteratorSemanticRelation.java) and [`IDeterministicSematicRelation`](../obp3-runtime/src/main/java/obp3/runtime/sli/IDeterministicSematicRelation.java): iterator-based and input-dependent deterministic contracts. Note the existing `Sematic` spelling.

## Extension Notes

- Keep these interfaces domain-neutral. Add algorithm-specific state and behavior in the owning module.
- The deterministic relation defaults assume `actions`/`execute` may mutate configuration; implementations can override purity hints to avoid defensive handling in consumers.
- Returned iterators/lists and whether configurations are mutated are important behavioral contracts. Check callers before changing them.
- Nullability is not consistently specified. In particular, downstream helpers use null sentinels; do not assume null graph vertices or outputs work without tracing the full call path.