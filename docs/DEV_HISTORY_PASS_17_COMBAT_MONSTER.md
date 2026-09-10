# PROJECT DARK — DEV HISTORY PASS 17 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster / reward-chain integration

## Source-of-Truth gate executed

Read/re-read before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history including `DEV_HISTORY_PASS_16_COMBAT_MONSTER.md`

During this pass, the central design update landed on main and superseded the old ground-drop/pickup contract. Current canonical flow is now:

`MONSTER_DEFEATED -> reward resolution -> direct inventory mutation`

No monster ground-drop entity or pickup stage is allowed in target runtime.

## Highest-priority bottleneck selected

Runtime still contained the retired chain in `RpgProgressionState` and `RpgInteractionController`:
- `WorldDrop` runtime entity;
- `worldDrops` collection;
- `createWorldDrop(...)`;
- pickup radius and `pickup(...)` validation;
- selected ground-drop state and nearest-drop interaction in `RpgInteractionController`.

This contradicted the newly accepted direct auto-loot design and would allow old behavior to re-enter through later integration.

## Implementation completed

### RpgProgressionState

Changed the reward pipeline to `reward -> auto-loot -> inventory`:
- removed `WorldDrop` entity;
- removed world-drop collection/state and drop IDs;
- removed pickup radius and pickup result contract;
- removed `createWorldDrop(...)` and `pickup(...)`;
- introduced explicit `AutoLootResult` outcomes: `LOOTED / INVALID_ITEM / INVALID_QUANTITY / INVENTORY_FULL`;
- introduced `autoLootResolvedItem(itemId, quantity)` as the single direct-inventory mutation endpoint for resolved monster item rewards;
- `RewardResolution` now records `autoLootedItems` instead of ground-drop IDs;
- connected `CanonicalMonsterRewardCatalog` lookup to monster defeat processing;
- unresolved probability/quantity is still skipped, not converted to deterministic loot;
- unknown/prototype monster reward mappings still return `PENDING_NO_CANONICAL_MONSTER_REWARD` and mutate nothing.

Gameplay commit:
- `94ec4d2174f85de1923d6fae8714bace0d9ce949` — `Switch monster item rewards to direct auto-loot`

### RpgInteractionController

Removed all monster-ground-loot orchestration:
- removed selected drop ID;
- removed nearest-drop selection;
- removed pickup interaction/result states;
- retained only inventory item selection, equipment interaction and stat QA helpers.

Gameplay commit:
- `ce79350fa28afc603a145ebe55b1e93a5a5e5809` — `Remove ground-drop pickup interaction path`

### Canon / concurrent integration

A concurrent central design commit `b76966202dd97360ac164b020614b242ae30ccc2` (`Retire ground loot contract`) updated `design/DATA_CONTRACT.md`. `design/DESIGN_CONSTITUTION.md` also now explicitly states the supersession rule: old monster ground-drop/pickup behavior has zero design authority and must not be restored.

The auto-loot runtime commits remain present in current main ancestry after concurrent World/Character integration.

## Validation

GitHub Actions run #99 for `94ec4d2174f85de1923d6fae8714bace0d9ce949`:
- `Validate Master DB`: SUCCESS
- `Compile debug sources`: SUCCESS
- `Build debug APK`: SUCCESS
- `Upload debug APK`: SUCCESS
- complete job: SUCCESS

A later ancestry build, run #101 for main commit `72e49df26b13096897512123c452e93f77f51dcf`, also completed all substantive and final steps successfully, proving the direct auto-loot changes remained compile/build compatible after the concurrent design and World/Character commits.

No compilation correction was required.

## DESIGN_CONFLICT / PENDING

### RESOLVED: GROUND_DROP_VS_AUTO_LOOT
Old Constitution/Data Contract required ground-world loot. Latest explicit user decision and newly updated canonical design supersede it. Target runtime is direct auto-loot only.

### PENDING: DROP_RATE_AND_QUANTITY
Existing canonical reward hints such as POTE_SPIRIT -> IT_RING_THREELINEGOLD do not yet have authoritative probability and quantity in the inspected catalog. Therefore they are not automatically granted merely because direct-inventory delivery exists.

### PENDING: COMBAT_DUMMY_REWARD_MAPPING
`combat_dummy_01` remains a prototype fixture with no canonical EXP/Gold/item relationship and continues to grant nothing.

### PENDING: ITEM_REGISTRY_COVERAGE
Only item definitions registered in the RPG runtime can be accepted by `autoLootResolvedItem`. Future canonical reward item IDs must first exist in the canonical item projection; unknown IDs fail as `INVALID_ITEM` rather than silently entering inventory.

## Result

The retired world-drop subsystem is disconnected from the active monster reward pipeline. A valid future resolved monster item reward now has one target path only:

`MONSTER_DEFEATED -> canonical reward resolution -> autoLootResolvedItem -> inventory`

No proximity check, floor object, pickup input, pickup pathfinding or pickup animation remains in the target monster reward contract.

## Next Combat · Monster bottleneck

1. Establish a supported playable-world canonical monster ID/spawn relation instead of `combat_dummy_01`.
2. Expand canonical ItemDefinition projection for item IDs referenced by positively supported monster reward relations.
3. Resolve authoritative probability/quantity where evidence exists; only then exercise a real `MONSTER_DEFEATED -> direct inventory` item grant end-to-end.
4. Add regression assertions that one defeat cannot double-grant and that no monster reward creates a ground entity or pickup request.
