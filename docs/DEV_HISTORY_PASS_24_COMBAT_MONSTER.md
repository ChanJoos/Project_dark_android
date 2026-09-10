# PROJECT DARK — DEV HISTORY PASS 24 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / canonical identity vs prototype combat tuning

## Source-of-Truth audit

Re-checked latest main and repository evidence for `MAP_POTE_01/02` and the currently mapped common monsters:
- `POTE_PURPLE`
- `POTE_SILVER`
- `POTE_WOLFRIDER`
- `POTE_ANTGIANT`

No additional authoritative coordinate/count/HP/EXP evidence was found for those common-monster runtime spawns. The canonical roster and map membership remain valid, but exact runtime combat values remain unresolved.

`DESIGN_CONSTITUTION.md` explicitly permits unknown original HP/damage/timing/etc. to remain null/PENDING or to be isolated as prototype `[B]` values, provided they are not promoted to canon.

## Bottleneck selected

The next integration risk was conflating canonical identity/map membership with prototype runtime tuning. A playable slice may need temporary combat values before original values are recovered, but those values must never overwrite or masquerade as canonical monster data.

## Implementation

Added `PotePrototypeCombatProfile.java`.

The first explicit prototype candidate is:
- canonical map identity: `MAP_POTE_01`
- canonical monster identity: `POTE_PURPLE`
- runtime tuning evidence: `[B]`
- placement: unresolved / not present

To avoid inventing a second set of arbitrary balance values, the profile reuses the existing `combat_dummy_01` prototype baseline exactly:
- runtime HP: 60
- chase radius: 180
- attack begin range: 42
- attack cancel range: 48
- chase speed: 28
- attack damage: 4
- attack cooldown: 1.2s

These values are explicitly non-canonical and do not modify `PoteMonsterRoster`, whose POTE_PURPLE Lv/HP/EXP remain null.

Added `PotePrototypeCombatProfileAudit.java`.

The audit verifies:
1. the profile remains `[B]` only;
2. no placement is claimed;
3. `POTE_PURPLE` is a stable canonical Pote identity;
4. `MAP_POTE_01 -> POTE_PURPLE` exists in the canonical map/monster manifest;
5. canonical POTE_PURPLE Lv/HP/EXP remain unresolved;
6. the prototype combat values exactly reuse the established dummy baseline and do not introduce new balance numbers.

Updated `MonsterSpawnAdmissionAudit` to enforce this boundary audit during runtime initialization.

Commits:
- `e015381a1c815985a83b4dccaa2cdbb9b2ea033a` — `Add explicit Pote prototype combat profile`
- `8dd8a2165a2962348bf2c502ee4b4c10e6f9481e` — `Audit explicit Pote prototype combat profile`
- `48ea05f5465095fdf2474b8cdb37f35e3a71980d` — `Validate Pote prototype combat boundary`

## Validation

GitHub Actions Run #163 (`34437356332`) for head `48ea05f5465095fdf2474b8cdb37f35e3a71980d` completed successfully.

Verified successful steps:
- Android / Gradle setup
- Validate Master DB
- Compile debug sources
- Build debug APK
- Upload debug APK
- complete job: SUCCESS

## DESIGN_CONFLICT / PENDING

### PENDING: POTE_SPAWN_PLACEMENT
No authoritative coordinates or count are currently available for POTE common-monster placement. The prototype profile intentionally has no placement and therefore is not yet activated as a world spawn.

### PENDING: POTE_COMMON_MONSTER_STATS
POTE_PURPLE and the other common Pote monsters retain null canonical Lv/HP/EXP. The `[B]` profile is a runtime prototype only.

### PENDING: CANONICAL_MONSTER_AI_PROFILE
Exact Pote detect/chase/attack/cooldown/respawn values remain unresolved. The current profile reuses the old dummy behavior solely as `[B]` vertical-slice tuning.

### RETIRED: GROUND LOOT / PICKUP
Direct inventory reward delivery remains canonical. No ground loot/pickup path was restored.

## Result

Combat now has an explicit boundary that allows a canonical monster identity to receive isolated prototype combat tuning without contaminating Master-backed data. `POTE_PURPLE` is the first such candidate, but remains non-spawned because placement is still unresolved.

## Next Combat · Monster bottleneck

1. Define an explicitly `[ADAPTED]/[B]` prototype placement contract for `MAP_POTE_01` without claiming original coordinates.
2. If that contract is accepted by the existing constitution/runtime architecture, activate one `POTE_PURPLE` vertical-slice spawn in a Pote-only prototype world, not Milles.
3. Keep canonical roster HP/EXP null until evidence is recovered.
4. Once activated, wire defeat -> reward resolution -> direct inventory regression while preventing unresolved item drops from becoming deterministic.
