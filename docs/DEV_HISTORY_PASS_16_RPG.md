# PROJECT DARK — DEV HISTORY PASS 16 · RPG / PROGRESSION

Date: 2026-09-10
Role: RPG · Progression

## Source-of-Truth gate executed

Read/checked before implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history including RPG PASS 14 and Combat PASS 15
8. current v4.4 baseline tables relevant to the bottleneck, especially `master/data/Monster_Master.csv` and `master/data/Spawn_Master.csv`

`data/design/PROJECT_DARK_CANONICAL_SEED.md` remains a derived convenience projection and did not override Master.

## Bottleneck audit

The requested next step was to connect a canonical Milles monster to EXP/drop. The current Master does not support that relationship yet:

- `Spawn_Master.csv` contains explicit spawn groups for 포테의숲, 아벨해안던전, 뤼케시온해안던전 and 호러캐슬.
- It contains no Milles hunting-ground spawn row.
- `MillesMasterManifest` positively projects Milles world/location/NPC identity but does not establish a canonical Milles monster spawn.
- Therefore replacing `combat_dummy_01` with a Master monster or borrowing a 포테/아벨/뤼케시온 monster into Milles would invent a region/spawn relationship and violate the Source of Truth.

## Implementation completed

Added `CanonicalMonsterRewardCatalog.java` as an RPG-owned read-only projection of reward facts already supported by `Monster_Master` and `Item_Master`.

Cataloged only positively supported values/relationships:
- `POTE_SPIRIT` EXP `308950` [V]; major-drop identity `IT_RING_THREELINEGOLD` (세줄금반지) mapped by canonical Item_Master identity.
- `ABEL_BERSERKER` EXP `606252` [V]; major-drop identity `IT_RING_SILVERAQUA` (실버아쿠아링) mapped by canonical Item_Master identity.
- `LYK_TYRANT` EXP `780575` [V]; no item drop relation asserted in this projection.

For every drop hint, probability and quantity are explicitly `null`. `hasNoInventedDropEmission()` audits that the catalog cannot silently become a deterministic drop emitter while rates/quantities remain unresolved.

Gameplay commit:
- `cfa8e97788c418b68250a3c9f9150dcb7dc87f7c` — `Add evidence-safe canonical monster reward catalog`

A concurrent Combat pass also added `MonsterDefinitionRegistry`, including the same Master-backed `POTE_SPIRIT` identity/EXP on the Combat ownership side. The RPG catalog does not alter that definition; it projects reward semantics for RPG ownership.

## DESIGN_CONFLICT / PENDING

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
No canonical Milles monster spawn relation exists in the inspected `Spawn_Master`. `combat_dummy_01` remains a prototype fixture and must stay rewardless.

### PENDING: DROP_RATE_AND_QUANTITY
Master major-drop text supports item relationships for some monsters, but exact probability and quantity are not established. World-drop emission remains blocked rather than being made deterministic.

### PENDING: STARTING_NORMAL_EXP
Commoner Lv1 is canonical, but exact starting EXP representation/value remains unresolved. No arbitrary zero initialization was introduced.

### PENDING: RPG_UI_RUNTIME_LINK
`RpgInteractionController` and RPG state contracts exist, but ground-drop/pickup/inventory/equipment presentation remains not fully wired into `GameView`. This remains the next implementation target when no canonical Milles reward source can yet be emitted.

## Result

The project now has an evidence-safe canonical monster reward catalog that can be consumed immediately when a supported monster is placed in the correct canonical region. It preserves the distinction between a verified EXP value, a verified major-drop relationship, and an unresolved drop probability/quantity.

No Monster/Item/Equipment/Skill/EXP/Level/Gold/Character/Job/Quest reward ID or value was invented or overwritten.

## Validation

GitHub Actions run #90 was triggered for the gameplay commit. Final compile/Master/APK result must be checked before this pass is treated as validated.

## Next RPG bottleneck

1. Keep `combat_dummy_01` rewardless until a canonical Milles spawn/reward relation exists.
2. Wire `RpgInteractionController` into visible ground-drop/pickup/inventory/equipment UI without creating a fake drop source.
3. When canonical spawn data becomes available, connect the matching monster ID to `CanonicalMonsterRewardCatalog` and `MONSTER_DEFEATED`.
4. Only after authoritative probability/quantity exists should a major-drop hint emit a world drop.
