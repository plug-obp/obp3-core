# Hash-cons arena ownership (audit 2026-10-20, finding 2.7)

## Problem
- `HashConsTable` kept a per-instance tag counter but could be built over another table's map; both
  allocators then issued overlapping tags for different values in the same storage.
- Tags are unique only per table, yet `HashConsed.equalityFunction` and the shipped factories
  (`TermHashConsed`, expando record, nowrapper `Var`/`App`/`Lambda`) compared tags alone, so
  distinct terms from different tables compared equal while their hash codes differed.
- Two uninterned values (tag `-1`) were equal by tag in the generic helper.
- Already-interned foreign values were returned as is, and nowrapper nodes were mutated in place.

## Decision
- New `Arena`: identity token that owns the tag allocator. Each `HashConsTable` owns one arena and
  its storage.
- `HashConsed` gains `arena()`. Tag equality applies only when both values are interned in the
  same non-null arena; otherwise the underlying nodes are compared with ordinary equality.
- `HashConsMaker.create(node, arena, tag, hashKey)` must return a new value, never mutate `node`.
- `hashCons` returns same-arena values unchanged; foreign interned values are re-interned
  (shallow: via `node()`); factories intern children first, so results stay canonical.
- Hard break: the supplied-map constructor no longer adopts the map. It copies and re-interns the
  seed's nodes into fresh storage with a fresh arena. `map()` is a read-only view.
- Hash codes of interned values use `isHashConsed()`, not `hashKey > 0`, and equal ordinary
  hashes are guaranteed across arenas (hashKey is the structural hash at interning time).

## Follow-up: raw vs interned hashing (review 3.1)
`hashCodeFunction` used `hashKey()` for any `HashConsed`, including uninterned values (sentinel `-1`),
so a raw and an interned structurally equal value were equal but hashed differently. The helper now
uses `hashKey()` only when `isHashConsed()`, and the non-same-arena equality fallback additionally
requires equal helper hashes, so equal implies same hash even if a table's custom hashable differs
from `hashCode()`.

## Extending a table without copying
Only safe if the storage has exactly one live owner, the same hashable and maker, and the
allocator moves with it (e.g. consuming the source table so it is never used again). Not provided.

## Plan / status
1. Arena + API change, table, equality fixes, factories — done.
2. Tests in `HashConsArenaTest` — done.
3. Full workspace build — see commit report.
