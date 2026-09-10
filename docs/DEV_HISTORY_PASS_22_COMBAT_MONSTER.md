# PROJECT DARK — DEV HISTORY PASS 22 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / Pote map-to-monster relation projection

## Source-of-Truth audit

Re-checked latest main after PASS 21, including:
- `master/data/Map_Instance_Master.csv`
- `master/data/Monster_Master.csv`
- current Pote roster projection
- current runtime spawn admission guard

Master-backed findings:
- `MAP_POTE_01` (`포테의숲 1~2존`) has Spawn/Room Rule `퍼플/실버팜팻 중심` with map-row evidence `V+B`.
- Stable monster IDs exist for the exact-name matches `POTE_PURPLE` and `POTE_SILVER`.
- `MAP_POTE_02` (`포테의숲 3~4존`) has Spawn/Room Rule `울프라이더/앤트자이언트/강력몹` with map-row evidence `V+B`.
- Stable monster IDs exist for exact-name matches `POTE_WOLFRIDER` and `POTE_ANTGIANT`.
- The generic token `강력몹` does not identify which strong-variant Monster_ID(s) should spawn, so no concrete strong monster relation is projected from that token.
- `포테의정령 조건` appears in the clear/exit context for `MAP_POTE_02`, not as one of the exact spawn-rule monster names used by this pass. `POTE_SPIRIT` is therefore not promoted to a map spawn relation here.

## Bottleneck selected

PASS 21 established stable Pote monster identities but no map-to-monster relation substrate. The next safe step is to project only the exact map/monster relations that the Master map table actually supports, while continuing to leave coordinates, counts, HP, AI timings, and reward emission unresolved.

## Implementation

Added `PoteSpawnManifest.java`.

Projected exact relations:
- `MAP_POTE_01` -> `POTE_PURPLE`
- `MAP_POTE_01` -> `POTE_SILVER`
- `MAP_POTE_02` -> `POTE_WOLFRIDER`
- `MAP_POTE_02` -> `POTE_ANTGIANT`

Each relation preserves:
- map ID;
- stable Monster_ID;
- raw Spawn/Room Rule text;
- map-row evidence `V+B`.

The manifest intentionally does NOT include:
- screen/tile coordinates;
- spawn count/density;
- respawn timing;
- unresolved HP/EXP;
- attack/detect/chase numbers;
- deterministic item drop probability/quantity.

Added `PoteSpawnManifestAudit.java`.

Audit guarantees:
1. exactly two projected Pote map IDs;
2. `MAP_POTE_01` contains exactly `POTE_PURPLE` + `POTE_SILVER`;
3. `MAP_POTE_02` contains exactly `POTE_WOLFRIDER` + `POTE_ANTGIANT`;
4. every projected Monster_ID exists in the Pote canonical identity roster and remains in `포테의숲`;
5. evidence remains `V+B`;
6. ambiguous `강력몹` is not silently converted into any `POTE_STRONG_*` ID;
7. `POTE_SPIRIT` is not silently promoted from condition/clear text into these spawn-rule relations.

Updated `MonsterSpawnAdmissionAudit.java` so the Pote map/monster manifest audit is enforced at the Combat·Monster runtime boundary.

Commits:
- `99cab9b8c816117f6a3b74380b2a49079aae52ec` — `Add canonical Pote map monster manifest`
- `b7491507fe56bd9d0ef0089c4009d79baf19f7e5` — `Add Pote spawn manifest audit`
- `de0fffeca51cec542017bc1e9fdc1eb8165dac69` — `Validate Pote map monster manifest`

## Validation

GitHub Actions Run #143 (`34436673666`) for head `de0fffeca51cec542017bc1e9fdc1eb8165dac69` completed with conclusion `success`.

Verified successful:
- Android / Gradle setup
- Validate Master DB
- Compile debug sources
- Build debug APK
- Upload debug APK
- workflow conclusion: SUCCESS

## DESIGN_CONFLICT / PENDING

### RESOLVED PARTIALLY: POTE_PLAYABLE_WORLD_SPAWN
Map-to-monster identity relations now exist for exact Master-backed Pote spawn-rule names. This resolves the roster membership part only.

### PENDING: POTE_SPAWN_PLACEMENT
No authoritative tile/screen coordinates or spawn counts were found in the inspected source. Do not fabricate physical placement.

### PENDING: POTE_COMMON_MONSTER_STATS
`POTE_PURPLE`, `POTE_SILVER`, `POTE_WOLFRIDER`, and `POTE_ANTGIANT` still lack authoritative complete Lv/HP/EXP tuples in the current Master data. Do not turn them into damageable runtime monsters using invented HP.

### PENDING: POTE_STRONG_VARIANT_MAPPING
`MAP_POTE_02` says `강력몹`, but it does not identify which strong-variant Monster_ID(s). Keep this unresolved.

### PENDING: CANONICAL_MONSTER_AI_PROFILE
Narrative/partial behavior evidence does not establish exact detect radius, chase speed, attack range, damage, windup, cooldown, or respawn timing.

### RETIRED: GROUND_LOOT / PICKUP
Monster item rewards remain direct-to-inventory after reward resolution. Do not restore world item drops or pickup navigation.

## Result

Combat now has a verified `map ID -> canonical Monster_ID` substrate for the first two Pote field groups without inventing coordinates or stats. The transition from `combat_dummy_01` toward real Pote encounters is now blocked specifically by physical spawn placement and unresolved runtime combat stats, rather than by monster identity or map membership.

## Next Combat · Monster bottleneck

1. Search Master/source data for authoritative Pote spawn coordinates/count/density or a tile/object relation that can place these four exact monsters.
2. Search for authoritative HP/runtime-combat stats for `POTE_PURPLE`, `POTE_SILVER`, `POTE_WOLFRIDER`, or `POTE_ANTGIANT`.
3. Activate a real canonical encounter only when both placement and required combat stats are supportable; otherwise keep the manifest identity-only.
4. Once one real Pote monster is playable, connect defeat -> reward -> direct inventory end-to-end and retire the dummy from that slice.
