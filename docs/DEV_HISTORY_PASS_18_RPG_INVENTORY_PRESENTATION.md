# PROJECT DARK — DEV HISTORY PASS 18 · RPG / INVENTORY PRESENTATION

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Re-read from current GitHub main before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history including RPG PASS 17, Combat PASS 17 and World PASS 15.

Current canonical reward flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory mutation`.
Ground-drop/pickup behavior remains retired and was not restored.

## Highest-priority incomplete RPG items selected

After the auto-loot migration, two dependency-safe gaps remained:
1. reward item IDs already referenced by the canonical monster reward catalog were not all registered in the RPG ItemDefinition registry;
2. inventory/equipment/auto-loot state had no stable read-only presentation projection for HUD/inventory UI consumption.

## Implementation completed

### Canonical reward item registry coverage

Expanded `RpgProgressionState` using existing Master Item_Master identities only:
- `IT_RING_THREELINEGOLD` / 세줄금반지 / 반지 / required level 11 / Evidence O;
- `IT_RING_SILVERAQUA` / 실버아쿠아링 / 반지 / required level 51 / Evidence O.

Existing `IT_GLOVE_LEATHER` remains unchanged.
No stat modifiers were added because authoritative modifier values are not established by the inspected evidence.

Commit:
- `3fa6c92eb22dc7f9f99a77d055daf947aa10f2e5` — `Expand canonical reward item registry`.

### Inventory presentation boundary

Added `RpgInventoryPresentation.java` as a read-only projection layer for Integrator/UI use.
It exposes:
- stable inventory rows by canonical item ID;
- canonical display name, equip slot, quantity, required level and evidence;
- whether an inventory item is currently equipped;
- latest reward notice containing monster ID, reward status, EXP and auto-looted item map;
- a QA invariant ensuring every visible row resolves to an RPG-owned ItemDefinition.

The presentation layer performs no inventory/equipment mutation and contains no reward values of its own.

Commit:
- `242963c3731ba0313a5a04086e8dabec072a372d` — `Add RPG inventory presentation model`.

### Concurrent audit integration

A concurrent main commit added `RewardPipelineAudit.java` and uses the newly expanded item registry to assert that every canonical reward hint item is registered before direct inventory grant can occur. It also checks invalid quantity/item inputs fail closed and that unresolved reward hints do not become deterministic.

Commit observed in current main ancestry:
- `47bfc5db5cea80b212f4bd009c47d75fc4825e4f` — `Add direct auto-loot runtime audit`.

## Validation

GitHub Actions run #107 for `242963c3731ba0313a5a04086e8dabec072a372d` reached all substantive gates successfully:
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

The later `RewardPipelineAudit` commit is in current main ancestry and has its own validation run in progress at pass-record time.

## DESIGN_CONFLICT / PENDING

### RESOLVED: REWARD_ITEM_REGISTRY_COVERAGE
The two canonical item IDs currently referenced by mapped monster reward hints now resolve to RPG ItemDefinition entries instead of failing `INVALID_ITEM` solely because the registry was incomplete.

### PENDING: DROP_RATE_AND_QUANTITY
Reward probability/quantity remains unresolved. Auto-loot still does not grant an item unless emission data is authoritative. No deterministic drop was invented.

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
Current Milles combat still uses `combat_dummy_01`, which has no canonical reward mapping. No original monster reward is attached to the prototype fixture.

### PENDING: ITEM_STAT_MODIFIERS
The registered rings have canonical identity/slot/level requirements, but no unsupported stat modifiers were added.

### PENDING: VISIBLE_INVENTORY_UI_INTEGRATION
A stable RPG presentation boundary now exists, but `GameView`/future inventory screen still must consume it visibly. That is now the next dependency-safe implementation target.

## Result

The direct-auto-loot architecture can now accept the currently mapped canonical reward item IDs without an artificial registry failure, and UI code has a dedicated read-only model for inventory/equipment/reward presentation.

No Monster/Item/Equipment/Skill/EXP/Level/Gold/Character/Job/Quest canonical value or relationship was invented or rewritten.

## Next RPG bottleneck

1. Wire `RpgInventoryPresentation` into visible HUD/inventory UI and expose auto-loot reward feedback without duplicating RPG rules in `GameView`.
2. Keep `combat_dummy_01` rewardless.
3. When canonical spawn and reward emission probability/quantity are resolved, exercise `MONSTER_DEFEATED -> reward resolution -> autoLootResolvedItem -> inventory -> presentation` end to end.
4. Add equipment interaction UI after inventory rows are visible, preserving level/slot requirements and evidence-gated stat modifiers.
