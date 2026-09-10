# PROJECT DARK — DEV HISTORY PASS 20 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / evidence-safe spawn admission

## Current-main audit

PASS 19 remained the latest Combat pass when this cycle started. Concurrent World/RPG work had added character equipment binding and detailed inventory/equip presentation, but no new evidence-safe Milles monster stable-ID/spawn/reward relation was present.

The current world remains `milles_runtime_proto` with only `combat_dummy_01` as its MONSTER_SPAWN fixture. `POTE_SPIRIT` remains canonical and region-bound to 포테의숲; it must not be injected into Milles simply to exercise reward flow.

## Bottleneck selected

Because canonical Milles monster evidence is still unresolved, this pass hardens the transition boundary so future agents cannot accidentally mix unresolved or wrong-region canonical monsters into the prototype world.

## Implementation

Added `MonsterSpawnAdmissionAudit.java`.

The audit verifies every current `WorldDef.MonsterSpawn` against `MonsterDefinitionRegistry` and fails closed when:
1. a spawn ID resolves to `UNRESOLVED`;
2. the current `milles_runtime_proto` contains anything other than explicit `PROTOTYPE_PENDING` monster fixtures;
3. a prototype fixture incorrectly exposes canonical reward facts;
4. a prototype fixture is not marked `[B]` evidence.

Integrated the audit into `RuntimeState` construction before runtime monster instances are created.

Commits:
- `c49941f4b65f40b9c1b910087ecd773166035911` — `Add monster spawn admission audit`
- `aba23459c7e23d148375cd11e01a55dde47103a4` — `Enforce monster spawn admission audit`

## Concurrent work handling

Concurrent RPG commit `39d2096c...` and World history commit `5cf3d35b...` landed during/after this pass. Combat-owned files were not overwritten and the newer main ancestry was preserved.

## Validation

GitHub Actions Run #124 (`34436026581`) for `aba23459c7e23d148375cd11e01a55dde47103a4` completed `success`.

Verified gates:
- Android/Gradle setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS
- workflow conclusion: SUCCESS

## DESIGN_CONFLICT / PENDING

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
No evidence-safe stable Monster_ID + spawn relation is yet available for the current Milles playable combat fixture.

### PENDING: DROP_RATE_AND_QUANTITY
Major-drop identity does not establish deterministic probability or quantity. Auto-loot remains delivery-only and must not invent reward emission.

### PENDING: CANONICAL_MONSTER_AI_PROFILE
Canonical monsters must not inherit the current prototype chase radius/range/speed/damage/cooldown values without evidence.

### RETIRED: GROUND_LOOT / PICKUP
Ground loot entities, pickup input/range, loot navigation, and AUTO pickup movement remain retired.

## Result

The runtime now protects both sides of future canonical monster integration: reward consumption is idempotent and direct-to-inventory, while world spawn admission rejects unresolved or prematurely canonicalized monster fixtures in the current Milles prototype.

## Next Combat · Monster bottleneck

1. Resolve a Master-backed starting-region stable Monster_ID + spawn relation.
2. Introduce an evidence-backed canonical monster AI/action profile only when supported values/behavior exist.
3. When both spawn and reward emission are authoritative, replace the prototype fixture and exercise true runtime `spawn -> combat -> MONSTER_DEFEATED -> reward -> auto-loot -> inventory` end-to-end.
4. Keep `combat_dummy_01` rewardless and all ground-loot code retired until that point.
