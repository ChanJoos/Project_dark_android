# PROJECT DARK — DEV HISTORY PASS 19 · RPG / VISIBLE INVENTORY

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
7. latest RPG PASS 18.

The direct auto-loot supersession remains authoritative:
`MONSTER_DEFEATED -> reward resolution -> direct inventory mutation`.
No ground-drop/pickup behavior was restored.

## Highest-priority incomplete item selected

RPG PASS 18 introduced `RpgInventoryPresentation`, but it was not visible or reachable from the live GameView. This left inventory and reward state structurally present but opaque to the player and QA.

## Implementation completed

Updated `GameView.java` from the concurrently integrated v0.63 baseline to v0.64 while preserving the latest CharacterRenderer/effect delegation changes.

Added:
- a single `RpgInventoryPresentation` presentation dependency;
- compact inventory-open UI state;
- utility-rail `▣` toggle interaction;
- compact inventory overlay rather than a permanently expanded desktop-sized inventory;
- current canonical progression label (`평민 · Lv1` from RPG state);
- inventory rows sourced exclusively through `RpgInventoryPresentation`;
- canonical item display name, quantity, required level, item ID and equipped marker;
- latest monster reward resolution status (`처리됨` or `PENDING`);
- auto-looted item-kind count when a resolved grant exists.

The overlay is read-only in this pass. It does not duplicate inventory/equipment mutation rules in GameView.

Gameplay commit:
- `642438894245f9b5d91a152614d980e7dbad499f` — `Wire RPG inventory presentation into GameView`.

## Validation

GitHub Actions run #115 (`Validate PROJECT DARK Android`) for gameplay commit `642438894245f9b5d91a152614d980e7dbad499f` reached all substantive gates successfully:
- Android SDK/build-tools setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

No compile correction was required.

## DESIGN_CONFLICT / PENDING

### RESOLVED: VISIBLE_INVENTORY_UI_INTEGRATION
The live GameView now has a compact, toggleable inventory view backed by RPG presentation state rather than hard-coded inventory rows.

### PENDING: INVENTORY_EQUIP_INPUT
Rows are visible but are not yet selectable/tappable for equipment interaction. The next pass should route row selection and equip requests through `RpgInteractionController`; GameView must not implement level/slot rules itself.

### PENDING: DROP_RATE_AND_QUANTITY
Current canonical monster reward hints still do not have authoritative probability/quantity. Auto-loot does not invent deterministic item grants.

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
Current Milles combat fixture `combat_dummy_01` remains prototype-only and rewardless.

### PENDING: ITEM_STAT_MODIFIERS
Known canonical item identity/slot/level requirements are visible, but unsupported equipment stat modifiers remain absent.

### PENDING: NORMAL_EXP_MUTATION
Some canonical monster reward EXP values are known in the reward catalog, but current normal EXP initialization/level threshold semantics are not fully resolved for safe live mutation. The UI does not claim EXP was applied merely because a reward entry contains an EXP fact.

## Result

The direct-auto-loot architecture is now visible at the live UI boundary without reintroducing a world-drop stage. When authoritative monster item emission data becomes available, the resulting direct inventory mutation will appear through the same inventory presentation path without additional pickup plumbing.

## Next RPG bottleneck

1. Add inventory-row touch selection and equipment request through `RpgInteractionController`.
2. Surface explicit equip outcomes: equipped / level requirement blocked / pending requirement / invalid item, without moving those rules into GameView.
3. Reflect equipment state through the existing presentation row and stat snapshot.
4. Keep `combat_dummy_01` rewardless and keep unresolved item probability/quantity PENDING.
