# PROJECT DARK — DEV HISTORY PASS 25 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / isolated Pote prototype world

## Source-of-Truth audit

Re-checked current main after PASS 24.

Findings:
- `MAP_POTE_01 -> POTE_PURPLE` remains an evidence-backed canonical identity/map relation.
- Exact original POTE_PURPLE HP/EXP/placement/count remain unresolved in the current repository.
- Existing PASS 24 prototype combat values are explicitly `[B]` and must not become original POTE_PURPLE facts.
- Current `MonsterDefinitionRegistry` still registers only `POTE_SPIRIT` as a canonical runtime definition; directly inserting `POTE_PURPLE` into the existing Milles `WorldDef` would therefore violate the current admission boundary.

## Bottleneck selected

Create a separate Pote prototype world shell that can instantiate the real canonical `POTE_PURPLE` Monster_ID as the same runtime monster type used by the combat engine, while keeping placement/count/HP explicitly non-canonical and isolated from the Milles world.

## Implementation

Added `PotePrototypeWorldDef.java`.

It preserves canonical facts:
- map ID: `MAP_POTE_01`;
- monster ID: `POTE_PURPLE`;
- map/monster membership remains sourced from `PoteSpawnManifest`.

It explicitly tags prototype placement as `B/ADAPTED`:
- player screen-space prototype position: (480, 320);
- monster screen-space prototype position: (480, 220);
- monster count: 1;
- runtime HP: reuses `PotePrototypeCombatProfile.RUNTIME_HP_B` (60).

These numbers are runtime reconstruction values only, not original map coordinates or original monster stats.

`PotePrototypeWorldDef.Spawn.instantiateRuntimeMonster()` now creates an actual `RuntimeState.Monster` with ID `POTE_PURPLE`, using the isolated prototype placement and HP.

Added `PotePrototypeWorldAudit.java`.

The audit verifies:
1. the prototype world targets `MAP_POTE_01`;
2. the spawn uses `POTE_PURPLE`;
3. placement evidence remains exactly `B/ADAPTED`;
4. runtime HP comes only from the explicit PASS 24 `[B]` combat profile;
5. the independent canonical `MAP_POTE_01 -> POTE_PURPLE` relation still exists;
6. canonical POTE_PURPLE HP/EXP remain null in `PoteMonsterRoster`;
7. the spawn can instantiate the same `RuntimeState.Monster` type used by the current combat engine;
8. the instantiated monster begins alive/IDLE with expected prototype position and HP;
9. prototype count remains exactly one.

Updated `MonsterSpawnAdmissionAudit.verify()` to fail closed unless `PotePrototypeWorldAudit.verify()` passes.

Commits:
- `d3b664d67aabebe087fad6230c414d9e67d7c2ca` — `Add Pote prototype world placement contract`
- `a28f3981b3b25aa3462e4d94dc8a7e67aa290885` — `Add Pote prototype world runtime audit`
- `89a225d92b2e8cb791554d204566411d507a1172` — `Validate isolated Pote prototype world`

## Validation

GitHub Actions Run #179 (`34438271547`) for head `89a225d92b2e8cb791554d204566411d507a1172` completed successfully.

Verified successful steps:
- Android / Gradle setup
- Validate Master DB
- Compile debug sources
- Build debug APK
- Upload debug APK
- complete job: SUCCESS

## DESIGN_CONFLICT / PENDING

### PENDING: POTE_RUNTIME_ACTIVATION
The isolated Pote prototype can now instantiate a runtime-compatible `POTE_PURPLE`, but the current GameView/RuntimeState still boots the Milles `WorldDef`. Do not silently replace Milles or mix Pote into it. A deliberate world-selection/runtime-world abstraction is the next integration step.

### PENDING: POTE_PURPLE_CANONICAL_STATS
Original HP/EXP/damage/timing remain unresolved. The current HP/AI profile is `[B]` only.

### PENDING: POTE_CANONICAL_PLACEMENT
Current Pote screen-space position/count are `B/ADAPTED`; original tile coordinates and population remain unresolved.

### PENDING: CANONICAL_MONSTER_AI_PROFILE
POTE_PURPLE still must not inherit prototype AI as an original fact. Any playable use must keep the profile explicitly prototype-labelled until evidence exists.

### RETIRED: GROUND_LOOT / PICKUP
Direct-to-inventory reward policy remains authoritative. Do not restore ground loot.

## Result

The project now has the first isolated Pote world shell that can instantiate a real canonical Monster_ID (`POTE_PURPLE`) as the combat engine's runtime monster type without contaminating canonical stats or the Milles prototype world.

## Next Combat · Monster bottleneck

1. Introduce a small runtime-world abstraction / selectable world source so `RuntimeState` can boot either Milles prototype or Pote prototype without mixing them.
2. Route the isolated Pote spawn through that path.
3. Extend `MonsterAIController` so the explicit Pote `[B]` combat profile can drive only the Pote prototype slice, never canonical/general monsters.
4. Add an end-to-end regression: Pote prototype boot -> POTE_PURPLE spawn -> combat -> MONSTER_DEFEATED -> reward resolution. If canonical reward remains unresolved, verify no fabricated reward emission.
