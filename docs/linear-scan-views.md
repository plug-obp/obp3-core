# LinearScanHashMap / LinearScanHashSet: view and equality semantics

## Problem (confirmed)
- `LinearScanHashMap.keySet()`, `values()` and `entrySet()` returned detached copies
  (`HashSet`/`ArrayList` of `SimpleEntry`), violating the `java.util.Map` view contract:
  removals and `Entry.setValue` never reached the map, and later map changes were not visible.
- `LinearScanHashMap` and `LinearScanHashSet` had no content-based `equals`/`hashCode`
  (identity semantics), so they never equalled `HashMap`/`HashSet` with the same content.
- `LinearScanHashSet.iterator()` had no `remove()`, and its `hasNext()` relied on a counter
  compared with `size`, which would break on removal.

## Reproduction
`LinearScanViewSemanticsTest` (14 of 15 cases failed before the fix).

## Fix
- Views are live: `KeyView`, `ValuesView`, `EntryView` read the table directly.
  Iterators support `remove()` (tombstone the slot); `remove`, `contains`, `clear` delegate
  to the map; `add` is unsupported, as in `HashMap`.
- Stored `MapEntry` now implements `Map.Entry` (write-through `setValue`, contract `equals`/`hashCode`).
- `equals`/`hashCode` follow the `Map`/`Set` contracts (sum of entry hashes `key^value` / element hashes),
  null keys and values included.
- `LinearScanHashSet.iterator()` scans ahead for the next active slot and supports `remove()`.

## Notes
- `equals`/`hashCode` use standard Java `equals`/`hashCode`, not the custom `Hashable`
  supplied to the constructor, so that they interoperate with other `Map`/`Set` implementations.
- Iterators are not fail-fast: structural modification other than via `Iterator.remove()`
  (e.g. `put` triggering a rehash) during iteration gives undefined results.

## Equality and hashing with a custom `Hashable` (follow-up)
- `equals` matches keys/elements with the table's own `Hashable`; map values use `Objects.equals`.
- `hashCode` uses the caller-supplied `Hashable` (`userHashable`, not the internal
  positive-hash wrapper), so `equals` implies equal `hashCode` for any valid `Hashable`.
  With `Hashable.standard()` the results equal those of `HashMap`/`HashSet`.
- Symmetry with foreign `Map`/`Set` implementations only holds when the `Hashable`
  agrees with standard equality (inherent to custom equivalence relations).
- Tests: `setCustomHashable`, `mapCustomHashable` (case-insensitive `Hashable`).

## Fail-fast iterators (follow-up)
**Problem:** iterators (set iterator and map key/value/entry view iterators) were not fail-fast;
a `put`/`add` that rehashed the table during iteration silently produced skipped or repeated elements
(the iterator held an index into the replaced array layout). Also, `put` of an *existing* key and
`add` of an *existing* element could trigger a rehash purely because the table sat at the load threshold.

**Fix:**
- `AbstractLinearProbingTable.modCount` is bumped on insert, removal, `clear` and rehash.
- Iterators snapshot `modCount` and throw `ConcurrentModificationException` from `next()`/`remove()`
  on mismatch; `Iterator.remove()` re-syncs its own expected count.
- `put`/`add` look up the key first; updating an existing key or re-adding an element is not structural
  and no longer rehashes (so value updates during iteration, e.g. `Entry.setValue`, remain legal).
- Like `HashMap`, fail-fast is best effort, not a guarantee.
- `LinearScanHashSet.removeAll(this)` is handled explicitly (clears the set; returns `true` iff it was
  nonempty) since removing while iterating the argument would otherwise trip fail-fast after a partial
  removal. Other arguments, including backed views, are unaffected (tests: `LinearScanHashSetTest`).

Tests: `LinearScanFailFastTest`.
