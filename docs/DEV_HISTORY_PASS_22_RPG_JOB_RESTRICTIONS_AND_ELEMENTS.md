# PROJECT DARK — DEV HISTORY PASS 22 · RPG / JOB RESTRICTIONS + EQUIPMENT ELEMENTS

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
7. latest RPG PASS 21.

Direct monster reward delivery remains authoritative:
`MONSTER_DEFEATED -> reward resolution -> inventory mutation`.
No ground-drop/pickup behavior was restored.

## Highest-priority RPG gap selected

PASS 21 deliberately excluded job-restricted equipment and element semantics because the simplified `ItemDefinition` could not represent them safely. This pass closes that model gap without inventing shield eligibility or elemental combat formulas.

## Implementation completed

### ItemDefinition job restriction model

Added first-class fields:
- `allowedJobCodes`
- `jobRestrictionResolved`

Added pure requirement evaluation:
- `RequirementResult.MET`
- `PENDING`
- `LEVEL_NOT_MET`
- `JOB_NOT_MET`
- `UNKNOWN_ITEM`

`equip()` now delegates level/job requirement interpretation to this centralized evaluator. UI/integration code therefore does not need to duplicate job rules.

Canonical job-restricted early items admitted where the Master row is explicit:
- `IT_EARRING_DOUBLE_SILVER` — 쌍은귀걸이 / Lv11 / `WARRIOR, ROGUE, MARTIAL_ARTIST`
- `IT_RING_GORU` — 고루반지 / Lv11 / `MAGE, CLERIC`

`IT_SHIELD_LEATHER` was intentionally not admitted: its Master restriction is only `방패 가능 직업`, while the exact allowed-job set is not resolved in the current evidence. No guess was made.

### Equipment element model

Added first-class `attackElement` and `defenseElement` fields to `ItemDefinition`.

Lv11 common elemental equipment now preserves Master semantics:
- 바다/대지/바람/화염 진주목걸이 -> attack element
- 바다/대지/바람/화염 가죽벨트 -> defense element

No 1.3x or other elemental matchup multiplier was implemented. Exact combat interaction remains PENDING.

### Regression audit expansion

Expanded `RpgEquipmentInteractionAudit` to verify:
- exact physical-job set for 쌍은귀걸이;
- exact magic-job set for 고루반지;
- allowed-job evaluation returns `MET`;
- wrong-job evaluation returns `JOB_NOT_MET`;
- level failure remains distinct from job failure;
- water necklace exposes attack element only;
- water belt exposes defense element only;
- unknown item requirement lookup fails closed;
- previous inventory/selection/auto-loot guard cases remain intact.

Gameplay commits:
- `9d415e5045c6d2b6c01ed76d5cc14e00961dafd4` — `Model item job restrictions and equipment elements`
- `a1599da73083d082e48d107e59fb696975c18556` — `Centralize equipment requirement evaluation`
- `80dc223d8624d266469696c549c184aff71b08fa` — `Audit job restrictions and equipment elements`

## Validation

GitHub Actions run #154 (`Validate PROJECT DARK Android`, run id `34436997469`) for `80dc223d8624d266469696c549c184aff71b08fa` completed successfully.

Successful substantive gates:
- Android SDK/build-tools setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

## DESIGN_CONFLICT / PENDING

### RESOLVED: ITEM_JOB_RESTRICTION_MODEL
Explicit Master job groups can now be represented and centrally validated rather than silently discarded.

### RESOLVED: EQUIPMENT_ELEMENT_METADATA
Attack/defense element identity is now preserved as equipment metadata instead of being collapsed into generic stat modifiers.

### PENDING: SHIELD_ALLOWED_JOB_SET
`방패 가능 직업` is not yet an explicit canonical job-code set. Do not register shield eligibility until evidence resolves it.

### PENDING: ELEMENT_COMBAT_FORMULA
Element identity is modeled, but exact matchup table/multiplier/cost behavior is not sufficiently resolved. Do not restore the old free-toggle/1.3x prototype as canon.

### PENDING: ITEM_STAT_MODIFIERS
Unsupported equipment stat values remain absent.

### PENDING: NORMAL_EXP_MUTATION
Normal EXP starting representation, complete thresholds and level-up stat-point idempotency remain unresolved; live EXP mutation stays disabled.

### PARTIAL: CANONICAL_PLAYABLE_MONSTER_REWARD_PATH
Concurrent Combat/World passes have advanced Pote monster/spawn readiness. RPG must inspect those latest manifests against the canonical reward catalog before enabling any real defeat->reward exercise. Drop probability/quantity must remain PENDING unless explicitly resolved.

## Next RPG bottleneck

1. Extend inventory/detail presentation to show centralized requirement reason and equipment element metadata without duplicating rules in `GameView`.
2. Inspect latest Pote spawn/monster manifests and reward catalog for the first evidence-safe canonical playable defeat->reward path.
3. Resolve shield allowed-job mapping from canonical evidence before admitting shields.
4. Investigate normal EXP threshold/start semantics and two-stat-point level-up idempotency before enabling live progression.
