# PROJECT DARK — DEV HISTORY PASS 07

## Scope
Autonomous integration pass continuing from PASS 06. No APK packaging/distribution is permitted in this pass; validation is compile-only.

## Repository state read before changes
- Latest gameplay runtime before this pass: `GameView` v0.59.
- PASS 06 priorities inherited: CAST validation/resource ordering, face target before actions, extract `CombatIntent`, replace GameView-local counters with `RuntimeMetrics`.
- Found a process contradiction: `.github/workflows/android.yml` had been changed to `assembleDebug` + APK artifact upload for a prior manual delivery. This conflicts with autonomous-development instructions.

## Changes implemented

### 1. Compile-only CI restored
- Removed `:app:assembleDebug` and APK artifact upload from the active Android workflow.
- Active validation command is now `gradle :app:compileDebugJavaWithJavac --stacktrace` only.
- This is a process correction, not a gameplay change.

### 2. Combat intent/controller extraction
New file: `CombatController.java`.

Responsibilities moved out of `GameView`:
- selected monster target
- deferred combat approach target
- `ATTACK / CAST / SKILL / KICK` intent
- attack-mode selection
- attack/cast/skill/kick cooldown ownership
- prototype action-range lookup

Evidence:
- Auto-approach remains `[ADAPTED]`; it is a mobile usability policy, not claimed as an original pathing behavior.
- Current numeric ranges/cooldowns remain `[B]` through `AttackDef` / `SkillDef`.

### 3. CAST resource-order bug fixed
Previous behavior:
- CAST consumed MP and started cooldown before verifying a usable target/range.

New behavior:
1. reject if no live target;
2. if outside prototype range, start `[ADAPTED]` auto-approach;
3. only after in-range validation, verify MP;
4. face target;
5. consume MP and commit cooldown;
6. trigger CAST animation/effect/damage.

Result: failed target/range checks no longer consume MP or cooldown.

The same target-before-cost consistency was applied to generic SKILL, KICK, and ATTACK prototype execution.

### 4. Target-facing before combat action
Before ATTACK / CAST / SKILL / KICK resolves, the player now updates the existing four-direction facing state toward the selected monster.

- The project still uses the established four screen-diagonal directions.
- No 8-direction movement was introduced.
- Facing implementation remains runtime reconstruction `[B]` pending exact original frame/asset mapping.

### 5. RuntimeMetrics becomes HUD source
Removed GameView-owned defeat/hit counters.
HUD now reads:
- monster defeats
- player hit count
- total prototype damage dealt
from `RuntimeState.metrics()`.

`CombatLedger` remains the event source; `RuntimeMetrics` remains reward-neutral.
No XP, Gold, loot, quest credit or stat-growth rules were invented.

### 6. Skill data model aligned with verified official taxonomy
`SkillDef` now models:
- `ActionClass.TECHNIQUE`
- `ActionClass.MAGIC`
- target policy

Official evidence `[O]`:
- Nexon guide states that techniques do not consume MP while magic consumes MP.
- This pass only adopts that high-level taxonomy as `[O]`.
- Existing prototype MP costs, ranges, cooldowns and damage remain `[B]`; `SKILL_PROTO` is not presented as an authenticated original skill.

The dedicated martial-artist KICK fixture remains `[B]` in timing/range/damage. The concept that the martial artist uses kicks is supported by the official class introduction `[O]`.

## Official evidence reviewed
- `[O]` Basic screen / HUD: https://lod.nexon.com/info/guide/82285
  - HP/MP bar, EXP bar, minimap, unified slot and group UI are documented.
- `[O]` Unified slots: https://lod.nexon.com/info/guide/82284
  - items and skills can be registered and used from unified slots.
- `[O]` Skill guide: https://lod.nexon.com/info/guide/82293
  - techniques vs magic distinction; acquisition/skill categories/s 숙련도 documented.
- `[O]` Game/class introduction: https://lod.nexon.com/info/intro
  - five jobs; martial artist explicitly described as fighting with fists and kicks.

## Asset/evidence safety
- Existing prototype NPC and monster visuals remain `PENDING_CROP` / reconstruction placeholders.
- No generated image is promoted to authenticated original art.
- No unidentified official screenshot crop is asserted to be a specific NPC/monster/sprite.
- Existing background source remains as previously classified in `WorldDef`; this pass does not alter licensing/redistribution status.

## Validation
- `CombatController.java` standalone integration commit triggered compile-only CI.
- `GameView` v0.60 integration triggered compile-only CI.
- `SkillDef` evidence-model change triggered compile-only CI.
- Final compile result must be recorded only after GitHub Actions reports a completed result. No APK validation is performed in this pass.

## Remaining issues / next pass priority
1. Confirm final compile-only CI for PASS 07; fix any compile errors before further feature work.
2. Move NPC auto-approach/navigation intent out of `GameView` into a small interaction controller so view code stops accumulating policy.
3. Split monster chase/attack orchestration out of `GameView` into a runtime AI system while preserving `[B]` labels.
4. Add action-result timing so damage does not always resolve at action start; use `[B]` impact frames until verified.
5. Add evidence-safe slot model (`SlotDef`) so right-bottom controls are data-driven rather than touch-coordinate hardcoding.
6. Keep actual sprite mapping `PENDING_CROP` unless the exact official-hosted visual is clearly identified and licensing status is separately tracked.
