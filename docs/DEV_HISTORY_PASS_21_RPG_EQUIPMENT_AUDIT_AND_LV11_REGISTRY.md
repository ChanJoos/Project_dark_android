# PROJECT DARK — DEV HISTORY PASS 21 · RPG / EQUIPMENT AUDIT + LV11 REGISTRY

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
7. latest RPG PASS 20.

Direct auto-loot remains authoritative:
`MONSTER_DEFEATED -> reward resolution -> inventory mutation`.
No ground-drop/pickup behavior was restored.

## Highest-priority incomplete RPG items selected

1. Add isolated regression coverage for inventory selection/equipment requirements.
2. Expand evidence-backed ItemDefinition projection for the early equipment band without inventing unsupported job restriction semantics.
3. Re-check whether normal EXP mutation can be safely implemented from current evidence.

## Implementation completed

### 1. Evidence-backed Lv11 common equipment registry expansion

Expanded `RpgProgressionState` from the previous small reward-focused subset to include canonical common Lv11 equipment whose current ItemDefinition can represent identity, slot and required level without losing a job restriction.

Added:
- `IT_LEGGING_LEATHER` — 가죽각반 / 각반 / Lv11
- `IT_SHOES` — 신발 / 신발 / Lv11
- `IT_RING_REDJADE` — 홍옥반지 / 반지 / Lv11
- `IT_NECK_WATER_PEARL` — 바다의진주목걸이 / 목걸이 / Lv11
- `IT_BELT_WATER_LEATHER` — 바다의가죽벨트 / 벨트 / Lv11
- `IT_NECK_EARTH_PEARL` — 대지의진주목걸이 / 목걸이 / Lv11
- `IT_BELT_EARTH_LEATHER` — 대지의가죽벨트 / 벨트 / Lv11
- `IT_NECK_WIND_PEARL` — 바람의진주목걸이 / 목걸이 / Lv11
- `IT_BELT_WIND_LEATHER` — 바람의가죽벨트 / 벨트 / Lv11
- `IT_NECK_FIRE_PEARL` — 화염의진주목걸이 / 목걸이 / Lv11
- `IT_BELT_FIRE_LEATHER` — 화염의가죽벨트 / 벨트 / Lv11

Previously registered canonical items were retained, including 가죽장갑, 세줄금반지 and 실버아쿠아링.

No stat modifiers were invented. Element semantics are not yet promoted into the simplified ItemDefinition because the current model lacks dedicated attack/defense element fields.

Commit:
- `5c6cda3491c8acc3f6091d329f7463047648f8d1` — `Expand canonical Lv11 common equipment registry`.

### 2. Isolated equipment interaction regression audit

Added `RpgEquipmentInteractionAudit`.

The audit creates a fresh RPG state and verifies without touching live player state:
- COMMONER starts at Lv1;
- selecting an unowned canonical item fails and clears selection;
- direct resolved-item inventory mutation can add a known canonical item;
- selecting an owned item preserves canonical item ID;
- Lv11 equipment at Lv1 returns `REQUIREMENT_NOT_MET`;
- a blocked equip does not mutate equipment;
- equipment ownership invariant still holds;
- stat snapshot remains unchanged when equip is blocked;
- unknown item auto-loot fails closed;
- zero quantity auto-loot fails closed;
- selecting an unknown/unowned item clears selection.

Commit:
- `6d2cdf38795088258938ceddbe2d2c482611443b` — `Add RPG equipment interaction regression audit`.

### 3. Runtime enforcement

`RuntimeState` now fails fast if `RpgEquipmentInteractionAudit.verify()` fails, alongside existing reward/idempotency/spawn admission audits.

An integration typo in `hitMonster()` was detected during local review immediately after the first RuntimeState write and corrected before validation completion.

Commits:
- `e21a9f7085660f32f4ac5aca244a0e15be203df6` — `Enforce RPG equipment interaction audit`
- `8e6166e03d20009dca91bcc743527fa4774c81a6` — `Fix monster hit iteration after RPG audit integration`

## EXP progression decision

Normal EXP live mutation remains PENDING.

Current data contains canonical monster EXP facts, but the runtime still lacks sufficiently resolved starting EXP representation and the complete normal-level threshold semantics needed to guarantee correct level advancement and two-stat-point award idempotency. Therefore this pass did not mutate `normalExp` or `normalLevel` from monster defeat.

## Deliberately excluded from registry expansion

Job-restricted equipment such as job-specific earrings/rings and shields was not added merely because its item row is known. Current `ItemDefinition` does not yet model job restriction, so registering such items as if level were the only requirement would weaken canonical requirements.

## Validation

GitHub Actions run #137 (`Validate PROJECT DARK Android`) for final gameplay commit `8e6166e03d20009dca91bcc743527fa4774c81a6` reached all substantive gates successfully:
- Android SDK/build-tools setup: SUCCESS
- Validate Master DB: SUCCESS
- Compile debug sources: SUCCESS
- Build debug APK: SUCCESS
- Upload debug APK: SUCCESS

## DESIGN_CONFLICT / PENDING

### RESOLVED: EQUIPMENT_INTERACTION_REGRESSION_GATE
Inventory selection and level-blocked equipment behavior now has an isolated fail-fast audit.

### PARTIAL: EARLY_ITEM_DEFINITION_COVERAGE
The canonical common Lv11 set representable by the current simplified model has been expanded. Job-restricted items remain excluded until restriction semantics exist.

### PENDING: ITEM_JOB_RESTRICTIONS
`ItemDefinition` needs explicit job restriction semantics before job-specific equipment and shields can be safely admitted.

### PENDING: EQUIPMENT_ELEMENT_SEMANTICS
Canonical necklaces/belts carry attack/defense element meaning, but the current ItemDefinition has no dedicated element fields. IDs/names/slots/required levels are registered; element behavior remains unimplemented rather than collapsed into stat modifiers.

### PENDING: NORMAL_EXP_MUTATION
Do not mutate live normal EXP/level until threshold/start representation and level-up stat-point idempotency are resolved.

### PENDING: CANONICAL_MILLES_MONSTER_REWARD_EMISSION
The Milles runtime prototype remains without a supported canonical monster spawn→reward emission path. Do not fabricate drop probability or quantity.

## Next RPG bottleneck

1. Extend ItemDefinition with evidence-safe job restriction and equipment element fields, then admit the currently excluded job-restricted early equipment.
2. Add requirement-aware equipment audit cases for job restrictions once represented.
3. Resolve normal EXP threshold/start semantics and stat-point award idempotency before enabling live EXP mutation.
4. Preserve direct auto-loot only; no ground-drop/pickup subsystem may return.
