# PROJECT DARK — DEV HISTORY PASS 17 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_16_WORLD_CHARACTER.md`)

Current user canon was preserved: monster item rewards use direct inventory auto-loot; no ground-drop/pickup path was added or restored.

## Concurrent main audit

Before integration, current `GameView` had advanced to v0.64 with Integrator/RPG presentation work including `RpgInventoryPresentation`, inventory UI, and the existing `RuntimeState.rpg()` bridge.

This pass did not create a second RPG state or duplicate equip rules. Character presentation reads the existing RPG-owned state only.

## Backlog selected

PASS 16 identified the next dependency-free character bottleneck as an evidence-aware equipped Item ID -> character visual binding that:
- preserves canonical Item IDs verbatim,
- distinguishes weapon slot from other equipment slots,
- never invents sprite asset references,
- consumes the existing RPG-owned equipment state rather than duplicating it.

## Master evidence checked

`master/data/Item_Master.csv` confirms the canonical `슬롯` field and includes `무기` as the weapon slot for item IDs such as:
- `IT_WAR_WPN_TOMAHAWK`
- `IT_WAR_WPN_AXE`
- `IT_ROG_WPN_1`
- `IT_WIZ_WPN_STAFF`
- `IT_PRI_WPN_WAND`

Other visible equipment slot examples include `갑옷`, `투구`, `장갑`, `각반`, `신발`, `방패`, `목걸이`, `벨트`, `귀걸이`, `반지`.

No sprite mapping was inferred from these rows.

## Implementation completed

### 1. Added `CharacterVisualBinding`

Created:
`app/src/main/java/com/projectdark/mobile/CharacterVisualBinding.java`

Responsibilities:
- builds a read-only projection from `RpgProgressionState.equipment()`;
- validates each equipped Item ID against `RpgProgressionState.itemDefinitions()` and its declared `equipSlot`;
- preserves equipped slot -> Item ID pairs verbatim;
- treats canonical slot `무기` as the weapon presentation slot;
- builds presentation refs that retain the Item IDs while leaving asset state `PENDING_CROP`;
- exposes `isDefinitionConsistent()` for audit;
- explicitly reports `hasResolvedSpriteAssets() == false` until authenticated assets exist.

No equip eligibility, stat mutation, inventory mutation, or reward behavior was added to the World/Character layer.

Commit:
- `cdf4c07d8c96a80c751df329fd204a07c4d30bee` — `Add evidence-safe character equipment visual binding`

### 2. Wired existing RPG equipment state into `GameView` character presentation

Updated `GameView.java` to v0.65.

`drawCharacter()` now:
1. reads the existing `state.rpg()` object;
2. constructs `CharacterVisualBinding` from the RPG-owned equipment map;
3. sends `equipmentVisualRef()` and `weaponVisualRef()` into `CharacterRenderer.Pose`;
4. leaves effect asset state `PENDING_CROP` and preserves all existing action/effect behavior.

This is a one-way presentation bridge only. Combat, inventory, reward, equip, stat, and progression ownership remain outside `CharacterRenderer`.

Commit:
- `c82b9f62eb9a46c619afa2e9350fc2c38665cbd4` — `Bind equipped item IDs into character presentation`

## Validation

GitHub Actions Run #121 for commit `c82b9f62...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

Only successfully compiling integrated changes are treated as PASS 17.

## DESIGN_CONFLICT / PENDING

No canonical Item ID/value/relationship was changed.
No sprite asset was fabricated.
No monster ground-drop/pickup behavior was introduced.

PENDING retained:
- authenticated original equipment sprite frames,
- authenticated original weapon sprite frames,
- item-specific equipment/weapon visual asset refs,
- layering/occlusion rules per actual equipped item,
- canonical left/right hand or dual-slot semantics where not already evidenced,
- renderer regression/audit coverage across all four directions and seven presentation states,
- canonical `MAP_MILLES` tile/object/collision/portal trace geometry.

Current equipment and weapon visual refs preserve Item IDs for future mapping but remain `PENDING_CROP`; this is not a claim that procedural placeholder pixels match those items.

## Result

Character presentation now consumes the same RPG-owned equipped Item IDs used by inventory/equipment state. The renderer has a stable evidence-safe seam for later item-specific equipment and weapon sprites without duplicating RPG rules or inventing asset mappings.

## Next World · Character bottleneck

1. Add renderer regression/audit coverage for `NW/NE/SW/SE × IDLE/WALK/CAST/ATTACK/SKILL/HIT/DEAD` and verify the five-layer contract under each presentation state.
2. Add evidence-safe visual mapping registry entries only when authenticated item sprite references become available; unresolved entries remain `PENDING_CROP`.
3. Continue `MAP_MILLES` transform/trace preparation without guessing canonical tile-to-screen geometry.
4. Preserve the direct-inventory monster reward canon and do not reintroduce world loot entities.
