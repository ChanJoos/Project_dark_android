# PROJECT DARK — DEV HISTORY PASS 15 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster

## Source-of-Truth gate executed

Read before and during implementation:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest available PASS history. PASS 13 was latest at initial gate; concurrent RPG PASS 14 landed during this cycle and was re-read before closing the pass.

`data/design/PROJECT_DARK_CANONICAL_SEED.md` remains derived/fallback only and did not override Master.

Source-of-Truth revision observed during this pass also establishes `master/data/*.csv` as the current DB baseline above historical convenience projections. No Master data row was modified.

## Highest-priority Combat · Monster bottleneck selected

The previously documented Combat bottleneck was still present in current `GameView`: monster distance detection, chase movement, attack wind-up cancellation/readiness and prototype damage/cooldown resolution were directly orchestrated by the renderer/view layer.

This violated the ownership boundary in `design/DATA_CONTRACT.md`, where Combat·Monster owns monster runtime AI and cross-domain behavior should not be duplicated in `GameView`.

## Implementation completed

Created `app/src/main/java/com/projectdark/mobile/MonsterAIController.java` and delegated the per-frame monster AI call from `GameView` to it.

The controller now owns the existing runtime sequence:
- live-player gate;
- live-monster iteration;
- player distance calculation;
- existing chase transition/movement request;
- existing attack wind-up cancellation when the target leaves range;
- existing attack readiness check;
- existing monster attack resolution call;
- return to IDLE when chase is no longer active.

The following pre-existing prototype values were moved without semantic change and remain explicitly `[B]`:
- chase radius 180f;
- attack begin range 42f;
- attack cancel range 48f;
- chase speed 28f;
- prototype monster damage 4;
- prototype monster cooldown 1.2f.

No Monster/Skill/Magic/Item/Equipment/Map/Progression ID, canonical stat, acquisition condition, effect, drop, EXP value, spawn relation or region relation was created or changed.

`WANDER` and `DETECT` remain declared runtime contract states only. No unsupported behavior was invented for them.

Gameplay commits:
- `27b74c166e2c4bde6c9152efea67d074f9acfafa` — create `MonsterAIController`.
- `af2ff78af7c2fcc9968e4e14d1a57f8272291781` — remove GameView-owned monster orchestration and delegate to the controller.

A concurrent RPG PASS 14 commit was preserved in main ancestry; current Combat integration did not overwrite its Commoner Lv1 progression changes.

## Validation

GitHub Actions run #83 (`Validate PROJECT DARK Android`) for commit `af2ff78af7c2fcc9968e4e14d1a57f8272291781` reached the substantive gates successfully:
- Android SDK/build-tools setup: SUCCESS
- `Validate Master DB`: SUCCESS
- `Compile debug sources`: SUCCESS
- `Build debug APK`: SUCCESS
- `Upload debug APK`: SUCCESS

No compilation correction was required after the integrated GameView commit.

## DESIGN_CONFLICT / PENDING

### PENDING: MONSTER_WANDER_DETECT_POLICY
`WANDER` and `DETECT` exist in the state contract but verified original behavior/timing is not established in the inspected evidence. They remain unimplemented rather than receiving plausible AI behavior.

### PENDING: PROTOTYPE_AI_NUMBERS
The existing chase/attack ranges, movement speed, damage and cooldown are `[B]` prototype values. This pass only relocated them to the correct ownership module and did not promote them to original facts.

### PENDING: COMBAT_DUMMY_REWARD_MAPPING
RPG PASS 14 confirms `combat_dummy_01` remains a prototype fixture with no canonical EXP/Gold/drop mapping. Combat does not fabricate a reward.

### PENDING: CANONICAL_MILLES_MONSTER_RUNTIME_LINK
Canonical monster data exists in Master, but a positively supported Milles monster -> runtime spawn -> reward chain must be established before replacing the current combat dummy. Region mismatches must not be bridged for convenience.

## Result

The PLAYABLE SLICE combat chain is structurally cleaner: renderer/input remains in `GameView`, while monster combat orchestration now resides in the Combat·Monster-owned controller. Existing behavior and prototype numbers are preserved, and Master-backed content remains untouched.

## Next Combat · Monster bottleneck

1. Introduce an evidence-safe MonsterDefinition/runtime projection loader from the current Master-backed data baseline instead of relying on hard-coded prototype monster definition fields.
2. Keep `combat_dummy_01` rewardless until an authoritative Milles monster relation is confirmed.
3. Once a supported runtime monster is available, connect its canonical monster ID to the existing `MONSTER_DEFEATED -> RpgProgressionState` contract without inventing EXP/drop rates.
4. Move remaining monster presentation-only telegraph assumptions out of gameplay rules where needed, while preserving HIT/DEAD/RESPAWN lifecycle separation.
