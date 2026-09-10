# PROJECT DARK — DEV HISTORY PASS 20 · RPG / EQUIPMENT INTERACTION

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
7. latest RPG PASS 19.

Canonical monster reward delivery remains direct auto-loot only:
`MONSTER_DEFEATED -> reward resolution -> inventory mutation`.
No ground-drop/pickup path was restored.

## Highest-priority incomplete RPG item selected

PASS 19 exposed inventory rows in live `GameView`, but those rows were read-only. The next dependency-safe gap was:
`visible inventory row -> canonical item selection -> equipment request -> RPG requirement result -> equipment/stat presentation`.

## Implementation completed

### Detailed equipment interaction result boundary

Updated `RpgInteractionController` so UI can surface the exact RPG-owned equipment outcome without reimplementing level/slot/ownership rules.

Added:
- `lastEquipResult()`;
- `equipSelectedDetailed(...)` returning `RpgProgressionState.EquipResult`;
- selection clears stale prior result;
- legacy coarse `equipSelected(...)` remains and delegates to the detailed path.

All equipment eligibility still lives in `RpgProgressionState.equip(...)`.

Commit:
- `39d2096c0a26bd49accf73db6f8d7cc2d24d5fe9` — `Expose detailed RPG equip outcomes`.

### Live inventory selection/equipment UI

Updated current `GameView` baseline to v0.66 while preserving concurrent World/Character `CharacterVisualBinding` integration.

Added:
- `RpgInteractionController` as the interaction boundary;
- tap selection of visible inventory rows by canonical item ID;
- selected-row highlight;
- compact `장착` button;
- inventory-panel touch capture so UI input does not fall through to NPC/monster/world controls;
- exact equipment outcome presentation for:
  - `EQUIPPED`;
  - `REQUIREMENT_NOT_MET`;
  - `REQUIREMENT_PENDING`;
  - `NOT_EQUIPPABLE`;
  - `UNKNOWN_ITEM`;
  - `ITEM_NOT_OWNED`;
- read-only equipment stat snapshot presentation (`장비 보정 항목 N개`).

The already integrated `CharacterVisualBinding.from(state.rpg())` means a successful future equipment mutation is immediately projected into character equipment/weapon visual references while unresolved sprite assets remain `PENDING_CROP`.

Gameplay commit:
- `36496e25fb260c1213d4b28ea12a214fbd30f006` — `Add inventory selection and equip interaction`.

## Validation

GitHub Actions run #127 (`Validate PROJECT DARK Android`) for gameplay commit `36496e25fb260c1213d4b28ea12a214fbd30f006` completed all substantive gates successfully:
- Android SDK/build-tools setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

No compile correction was required.

## DESIGN_CONFLICT / PENDING

### RESOLVED: INVENTORY_EQUIP_INPUT
Visible inventory rows now select canonical IDs and route equipment requests through the RPG interaction/state contracts instead of duplicating equip rules in `GameView`.

### PENDING: LIVE_SUCCESSFUL_EQUIP_CONTENT
The current player starts as COMMONER Lv1. The registered canonical equipment examples currently exposed by the reward/item projection require Lv11 or Lv51, so if those items are present at Lv1 the correct result is `REQUIREMENT_NOT_MET`. No hidden level override or fabricated starter equipment was added merely to force a successful equipment demo.

### PENDING: ITEM_STAT_MODIFIERS
Known item identity/slot/required-level data is preserved, but unsupported stat modifier values remain absent. Therefore the equipment stat snapshot can legitimately show zero modifier entries.

### PENDING: CANONICAL_MILLES_MONSTER_REWARD_EMISSION
`combat_dummy_01` remains prototype/rewardless. Canonical monster reward probability/quantity and a supported playable Milles spawn→reward relation are still required before a real defeat can auto-loot these items.

### PENDING: NORMAL_EXP_MUTATION
Known catalog EXP facts are not yet sufficient to safely implement complete normal EXP/level threshold semantics. No EXP progression was fabricated in this pass.

## Result

The runtime now has an end-to-end equipment interaction boundary:
`inventory presentation -> canonical item selection -> RpgInteractionController -> RpgProgressionState.equip -> exact result -> equipment/stat/character presentation`.

This preserves direct auto-loot architecture and existing concurrent World/Character visual binding work.

## Next RPG bottleneck

1. Add an isolated regression audit for inventory selection/equipment requirements without mutating live player state.
2. Expand evidence-backed ItemDefinition projection beyond the current small reward-related subset as needed for the starting-region vertical slice.
3. Resolve canonical normal EXP thresholds/progression mutation before changing live level state.
4. When authoritative playable monster reward emission data exists, exercise the real chain `defeat -> direct auto-loot -> inventory -> selection -> equipment` without QA-only reward injection.
