# PROJECT DARK — DEV HISTORY PASS 17 · RPG / AUTO-LOOT MIGRATION

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Read/re-checked before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history including RPG PASS 16 and Combat PASS 16

Current DB baseline remains `master/data/*.csv`. Existing Monster/Item IDs and reward values were not rewritten.

## Latest explicit product decision

Monster item rewards no longer use ground-drop entities or manual pickup. The target runtime loop is:

`MONSTER_DEFEATED → reward resolution → inventory mutation`

AUTO and manual combat use the same reward path. Ground item entities, pickup distance, pickup input, pickup pathfinding and pickup animation are retired from the target runtime.

## Design contract migration

Updated canonical design contracts so later agents do not restore the obsolete loot loop:
- `design/DESIGN_CONSTITUTION.md`: AUTO canon now states direct automatic looting and explicitly rejects a ground-item fallback.
- `design/DATA_CONTRACT.md`: previous `world drop → pickup → inventory` contract is RETIRED / SUPERSEDED; canonical monster reward flow is direct inventory mutation.
- `design/SOURCE_OF_TRUTH.md` revision D002 records the latest user decision and preserves the rule that unresolved reward data remains PENDING.

Relevant design commits include:
- `cae17e4199861b8583a9be5f10f94c023d3dec2e` — align loot constitution with auto-loot design.
- `7d8e2f15e3ef3dbfc152de5d4aa12a2450bd0820` / later consolidated contract change `b76966202dd97360ac164b020614b242ae30ccc2` — retire ground-loot contract.
- `5b617097c974a20ce2e6a930a787e1af6218768f` — record auto-loot as latest Source-of-Truth runtime decision.

## Runtime migration completed

`RpgProgressionState` was migrated from:

`reward → WorldDrop → pickup → inventory`

to:

`reward → autoLootResolvedItem() → inventory`.

The old runtime concepts were removed from the RPG target path:
- `WorldDrop`
- `PickupResult`
- `worldDrops`
- pickup radius
- `createWorldDrop()`
- `pickup()`

The new centralized mutation result is `AutoLootResult` with explicit outcomes:
- `LOOTED`
- `INVALID_ITEM`
- `INVALID_QUANTITY`
- `INVENTORY_FULL`

`RewardResolution` now records `autoLootedItems` rather than created ground-drop IDs.

Gameplay commit:
- `94ec4d2174f85de1923d6fae8714bace0d9ce949` — switch monster item rewards to direct auto-loot.

`RpgInteractionController` was also reduced to inventory/equipment interaction only. Ground-drop selection/pickup orchestration is no longer part of the controller contract.

## Reward evidence behavior retained

Automatic looting changes delivery mechanics only; it does not change reward evidence requirements.

Current `CanonicalMonsterRewardCatalog` still separates:
- verified EXP values,
- verified major-drop item relationships,
- unresolved probability/quantity.

If probability or quantity is unresolved, `emissionResolved()` remains false and no item is inserted into inventory. No deterministic reward was invented merely because pickup was removed.

`combat_dummy_01` remains a prototype fixture and still has no canonical reward mapping.

## Validation

GitHub Actions run #99 for gameplay commit `94ec4d2174f85de1923d6fae8714bace0d9ce949` reached all substantive gates successfully:
- `Validate Master DB`: SUCCESS
- `Compile debug sources`: SUCCESS
- `Build debug APK`: SUCCESS
- `Upload debug APK`: SUCCESS

A later main commit also contains this auto-loot change in ancestry, so the migration is preserved in current main.

## DESIGN_CONFLICT / PENDING

### RESOLVED: GROUND_LOOT_VS_AUTO_LOOT
Previous Constitution/Data Contract required ground-world pickup. Latest explicit user decision supersedes that design. Ground-drop/pickup is retired.

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
Current Milles runtime still lacks a positively supported canonical monster spawn→reward relationship. `combat_dummy_01` remains rewardless.

### PENDING: DROP_RATE_AND_QUANTITY
Some Master rows contain major-drop item relationships, but exact probability/quantity are not uniformly established. Auto-loot must not insert those items until the reward emission rule is authoritative.

### PENDING: STARTING_NORMAL_EXP
Commoner Lv1 is canonical, but exact starting EXP representation/value remains unresolved.

### PENDING: ITEM_STAT_MODIFIERS
Known item identities/requirements do not automatically imply unknown stat modifiers. Equipment stat deltas remain evidence-gated.

## Result

The obsolete ground-loot architecture has been removed from the target RPG flow. The project now uses one evidence-safe monster reward path:

`monster death → canonical reward resolution → direct inventory mutation → equipment/stat systems`

The next RPG bottleneck is no longer pickup UI. It is to complete authoritative monster reward emission data and then surface auto-loot results/inventory changes in the HUD/inventory UI.
