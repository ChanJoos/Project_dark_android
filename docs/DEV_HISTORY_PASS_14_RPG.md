# PROJECT DARK — DEV HISTORY PASS 14 · RPG / PROGRESSION

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Read before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history including `DEV_HISTORY_PASS_13_WORLD_CHARACTER.md`

`data/design/PROJECT_DARK_CANONICAL_SEED.md` remains derived/fallback only and does not override Master.

## Highest-priority RPG bottleneck selected

The previous RPG/Integrator passes left `PLAYER_PROGRESSION_INITIALIZATION` unresolved. Runtime RPG state had `normalLevel = null`, even though the current canonical scope explicitly fixes creation/progression start as:

`character creation -> COMMONER Lv1 -> five basic jobs -> Lv99 master -> JOB_CHANGE or PURE_JOB -> Lv99 -> FIRST_ADVANCEMENT`

`master/data/Progression_Master.csv` P00 independently anchors the opening progression band as `Lv1~10` in Milles. Exact starting EXP representation and base STR/CON/INT/DEX/WIS values are not established by the inspected evidence, so those values remain PENDING.

## Implementation completed

Updated `RpgProgressionState.java`:

- initialized canonical runtime progression to `ProgressionNode.COMMONER`;
- initialized canonical current job code to `COMMONER`;
- initialized normal level to `1`;
- intentionally retained `normalExp = null` because exact starting EXP storage/value is not evidenced;
- intentionally retained base stat map empty because exact starting STR/CON/INT/DEX/WIS values are not evidenced;
- added an explicit progression-node enum capped at `FIRST_ADVANCEMENT`; no second advancement or later node was introduced;
- existing `IT_GLOVE_LEATHER` level requirement remains unchanged. With runtime level now known as Lv1, equip validation can correctly return `REQUIREMENT_NOT_MET` rather than `REQUIREMENT_PENDING` when that item is actually owned.

Gameplay commit:
- `0a7246e7e25203a6a278c45c3026afc1ca0628f3` — `Initialize canonical Commoner Lv1 progression state`

A concurrent World agent commit followed this change on main, so the RPG commit remains in current main ancestry.

## Validation

GitHub Actions run #79 (`Validate PROJECT DARK Android`) was triggered for commit `0a7246e7e25203a6a278c45c3026afc1ca0628f3` using the compile-only Android validation workflow. Final conclusion must be checked before this pass is treated as compile-verified.

## DESIGN_CONFLICT / PENDING

### PENDING: STARTING_NORMAL_EXP
Canonical sources establish Commoner Lv1 start, but the exact runtime starting EXP value/representation has not been established. `normalExp` remains null; do not assume zero without evidence.

### PENDING: COMMONER_BASE_STATS
Exact starting STR/CON/INT/DEX/WIS values remain unresolved. `baseStats` remains empty; no plausible defaults were fabricated.

### PENDING: COMBAT_DUMMY_REWARD_MAPPING
`combat_dummy_01` remains a prototype fixture. No canonical EXP/Gold/drop is attached to it.

`Monster_Master` contains canonical/verified monsters such as `POTE_SPIRIT` with verified Lv/HP/EXP, but its region is 포테의숲 rather than the current Milles prototype fixture. It must not be substituted into the current world merely to force rewards.

### PENDING: DROP_RATE_AND_QUANTITY
Where Monster_Master lists major drops, exact probability/quantity is not uniformly established. Do not convert a textual major-drop list into deterministic drop emission without an authoritative relationship/rate.

### PENDING: ITEM_STAT_MODIFIERS
`IT_GLOVE_LEATHER` has canonical identity/slot/required level, but no supported stat modifier was introduced. Equip-state transition and stat recomputation remain structurally valid without fabricated stat deltas.

## Result

One previously unresolved canonical progression dependency is now implemented: RPG runtime no longer starts with unknown level/job state. It starts as `COMMONER Lv1` and remains evidence-safe for unknown EXP/base stats.

## Next RPG bottleneck

1. Re-check the newly added Master-backed Milles runtime manifest from concurrent World work and identify whether a positively supported Milles monster -> reward relation now exists.
2. If it exists, map only authoritative monster EXP/drop/Gold relations into `RpgProgressionState` and emit world drops at monster death position.
3. Otherwise keep `combat_dummy_01` rewardless and wire `RpgInteractionController` into visible ground-drop/pickup/inventory/equipment UI without fabricating a drop source.
4. Add regression coverage for Commoner Lv1 requirement behavior and later for defeat -> reward -> drop -> pickup -> inventory -> equip -> stat snapshot once canonical reward data is available.
