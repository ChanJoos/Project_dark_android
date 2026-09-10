# PROJECT DARK — DEV HISTORY PASS 23 · RPG / REWARD READINESS + PRESENTATION

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Re-read current GitHub main before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest RPG PASS 22.

Direct monster reward delivery remains authoritative:
`MONSTER_DEFEATED -> reward resolution -> inventory mutation`.
No ground-drop/pickup behavior was restored.

## Pote canonical reward-path audit

Inspected current `PoteSpawnManifest`, `PoteMonsterRoster`, `MonsterSpawnReadiness` and `CanonicalMonsterRewardCatalog`.

Findings:
- `MAP_POTE_01/02` relations prove canonical map/monster membership only.
- `MonsterSpawnReadiness` intentionally returns `IDENTITY_ONLY` because authoritative coordinates/count/runtime admission remain unresolved.
- `POTE_SPIRIT` has resolved Lv48/HP29201/EXP308950 facts in the roster/reward catalog.
- `POTE_SPIRIT` is not currently present in the authoritative Pote map relation manifest.
- Its mapped major-drop hint `IT_RING_THREELINEGOLD` still has null probability and quantity, so deterministic item emission is forbidden.

Therefore a fully playable canonical `POTE_SPIRIT defeat -> item auto-loot` path was NOT enabled. This is an evidence boundary, not a runtime omission to paper over.

## Implementation completed

### 1. Inventory presentation now exposes RPG-owned requirement/element semantics

Expanded `RpgInventoryPresentation.ItemRow` with:
- `allowedJobCodes`
- `jobRestrictionResolved`
- `attackElement`
- `defenseElement`
- current `RequirementResult`

Added presentation helpers for requirement and element labels. The presentation layer delegates requirement truth to `RpgProgressionState.currentRequirements()` and does not duplicate equip rules.

Commit:
- `c41dc7fa7a4d044cb0dfe3761d92113f2492eee8` — `Expose RPG requirement and element metadata to inventory UI`.

### 2. Canonical reward readiness regression boundary

Added `CanonicalRewardReadinessAudit`.

It verifies:
- POTE_SPIRIT canonical EXP remains 308950;
- unresolved drop probability/quantity cannot become an emittable item reward;
- all current Pote map relations remain `IDENTITY_ONLY` rather than silently becoming playable;
- POTE_SPIRIT is not fabricated into the current map relation set;
- the reward catalog continues to satisfy `hasNoInventedDropEmission()`.

Commit:
- `6bdef76fb95973c79dcc0de4fc5a9290eaca9ce4` — `Add canonical reward readiness audit`.

### 3. Runtime enforcement

`RuntimeState` now fails fast when the canonical reward-readiness boundary is violated.

Commit:
- `e24c33d16526c1fa180661d07e7900e27757af75` — `Enforce canonical reward readiness boundary`.

## Validation

GitHub Actions run #164 (`Validate PROJECT DARK Android`, run id `34437376407`) for gameplay commit `e24c33d16526c1fa180661d07e7900e27757af75` passed all substantive gates:
- Android SDK/build-tools setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

## DESIGN_CONFLICT / PENDING

### RESOLVED: REWARD_READINESS_FALSE_PROMOTION_GUARD
Current canonical map membership and reward metadata cannot silently promote themselves into a playable deterministic loot path.

### RESOLVED: INVENTORY_REQUIREMENT_ELEMENT_PRESENTATION_MODEL
The UI projection can now expose job restrictions, attack/defense element identity, and centralized requirement status without redefining RPG rules.

### PENDING: POTE_SPIRIT_PLAYABLE_MAP_RELATION
POTE_SPIRIT lacks an authoritative current map relation/placement suitable for live runtime admission.

### PENDING: POTE_RUNTIME_PLACEMENT
Current Pote map relations still lack authoritative coordinates/count/runtime placement.

### PENDING: DROP_RATE_AND_QUANTITY
POTE_SPIRIT's major-drop identity relation exists, but probability and quantity remain unresolved. Do not emit deterministic item rewards.

### PENDING: NORMAL_EXP_MUTATION
Although POTE_SPIRIT EXP is resolved as a reward fact, player normal EXP start/threshold/stat-point idempotency remains unresolved; live EXP mutation stays disabled.

## Next RPG bottleneck

1. Wire the new requirement/element presentation fields into the visible inventory detail panel.
2. Continue canonical Pote runtime admission only when map relation + placement become evidence-safe.
3. If authoritative drop probability/quantity is added, connect the existing direct-inventory auto-loot path without creating ground loot.
4. Resolve normal EXP thresholds/start representation before applying resolved monster EXP to player progression.
