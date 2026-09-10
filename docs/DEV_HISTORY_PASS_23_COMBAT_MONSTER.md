# PROJECT DARK — DEV HISTORY PASS 23 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / canonical spawn readiness boundary

## Source-of-Truth audit

Re-checked the current Pote map/monster relation after PASS 22 and searched the repository for additional authoritative runtime inputs for `MAP_POTE_01` / `MAP_POTE_02`.

Confirmed canonical identity relations remain:
- `MAP_POTE_01` -> `POTE_PURPLE`, `POTE_SILVER`
- `MAP_POTE_02` -> `POTE_WOLFRIDER`, `POTE_ANTGIANT`

No additional repository evidence was found for authoritative spawn coordinates, monster counts, or exact HP/EXP for these four common monsters. Those values remain PENDING and were not invented.

## Bottleneck selected

PASS 22 established canonical map -> Monster_ID membership, but the runtime still lacked an explicit distinction between:
1. a canonical identity-only relation; and
2. a fully playable runtime spawn.

Without that boundary, future work could accidentally treat roster membership as permission to fabricate placement/count/combat values.

## Implementation

Added `MonsterSpawnReadiness.java` with states:
- `IDENTITY_ONLY`
- `PLAYABLE`
- `UNRESOLVED`

For the current four Pote map relations, readiness evaluates to `IDENTITY_ONLY` because identity/map membership is supported while placement/count and combat admission inputs are incomplete.

Added `MonsterSpawnReadinessAudit.java`.

The audit verifies:
- all four current Pote relations remain `IDENTITY_ONLY`;
- no relation is silently promoted to `PLAYABLE`;
- `MAP_POTE_01` / `MAP_POTE_02` common-monster HP/EXP remain unresolved;
- the readiness boundary is evaluated from the existing Pote roster + spawn manifest rather than duplicate hard-coded spawn facts.

Updated `MonsterSpawnAdmissionAudit.java` to enforce `MonsterSpawnReadinessAudit.verify()` at the Combat·Monster runtime boundary.

Commits:
- `8f6a6a3c0b182a0d6bb726024b5324a7f461a652` — `Add canonical monster spawn readiness contract`
- `c2b7135eef806f1eb9cfe0a5dd86a275fd2310b0` — `Add monster spawn readiness audit`
- `81aac480b866b84df9e7a6318f5ec5b3920da493` — `Enforce monster spawn readiness audit`

## Concurrent work handling

Concurrent RPG/World work continued on main. Combat changes were added without overwriting those files. Later commits remain descendants of the Combat work.

## Validation

GitHub Actions Run #150 (`34436878316`) for head `81aac480b866b84df9e7a6318f5ec5b3920da493` completed `success`.

Verified successful steps:
- Android / Gradle setup
- Validate Master DB
- Compile debug sources
- Build debug APK
- Upload debug APK
- workflow completion: SUCCESS

## DESIGN_CONFLICT / PENDING

### PENDING: POTE_SPAWN_COORDINATES
Exact canonical spawn coordinates are not established for the four projected Pote relations. Do not invent them.

### PENDING: POTE_SPAWN_COUNTS
Canonical per-map/per-zone monster counts are not established. Do not invent them.

### PENDING: POTE_COMMON_MONSTER_COMBAT_STATS
`POTE_PURPLE`, `POTE_SILVER`, `POTE_WOLFRIDER`, and `POTE_ANTGIANT` do not currently have authoritative complete runtime HP/EXP tuples in the inspected Master data.

### PENDING: PLAYABLE_CANONICAL_POTE_SPAWN
The map->monster relation is canonical, but none of the four current relations qualifies as a fully playable runtime spawn yet.

### RETIRED: GROUND_LOOT / PICKUP
Monster item rewards remain direct inventory mutation after reward resolution. No world-drop/pickup behavior may be restored.

## Result

Combat now has an explicit evidence gate between canonical spawn identity and playable spawn activation. Map membership alone can no longer be treated as permission to fabricate coordinates, counts, or combat stats.

## Next Combat · Monster bottleneck

1. Inspect remaining Master/source projections for exact Pote common-monster HP or another canonical monster whose map relation + HP are both supported.
2. If exact coordinates remain unavailable, define a clearly `[B]/ADAPTED` placement layer that references canonical monster identity without mislabeling the placement as original data, only if the design constitution permits that prototype bridge.
3. Prefer the first monster with both canonical identity/map relation and authoritative HP for a true playable canonical combat slice.
4. Then connect defeat -> canonical reward -> direct inventory end-to-end without deterministic unresolved drops.
