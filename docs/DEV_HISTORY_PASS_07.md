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
- Auto-approach remains `[ADAPTED]`; it is a mobile usability policy, not claimed as original pathing behavior.
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
Before ATTACK / CAST / SKILL / KICK resolves, the player updates the existing four-direction facing state toward the selected monster.
- Existing four screen-diagonal directions are preserved.
- No 8-direction movement was introduced.
- Facing implementation remains `[B]` pending exact original frame/asset mapping.

### 5. RuntimeMetrics becomes HUD source
Removed GameView-owned defeat/hit counters.
HUD now reads monster defeats, player hit count, and total prototype damage dealt from `RuntimeState.metrics()`.
`CombatLedger` remains the event source; `RuntimeMetrics` remains reward-neutral.
No XP, Gold, loot, quest credit or stat-growth rules were invented.

### 6. Skill model aligned with verified official taxonomy
`SkillDef` now models:
- `ActionClass.TECHNIQUE`
- `ActionClass.MAGIC`
- target policy

Official evidence `[O]`:
- Nexon guide states that techniques do not consume MP while magic consumes MP.
- Initial implementation accidentally left `SKILL_PROTO` classified as `TECHNIQUE` with `mpCost=5`. This was detected as a design contradiction during the same pass.
- Correction: `SKILL_PROTO.mpCost` is now `0`, consistent with the verified high-level technique/magic taxonomy `[O]`.
- CAST prototype MP cost value, all cooldowns, ranges and damage remain `[B]`; they are not authenticated original server numbers.

The dedicated martial-artist KICK fixture remains `[B]` in timing/range/damage. The concept that the martial artist uses kicks is supported by the official class introduction `[O]`.

## Official evidence reviewed
- `[O]` Basic screen / HUD: https://lod.nexon.com/info/guide/82285
- `[O]` Unified slots: https://lod.nexon.com/info/guide/82284
- `[O]` Skill guide: https://lod.nexon.com/info/guide/82293
- `[O]` Game/class introduction: https://lod.nexon.com/info/intro

## Asset/evidence safety
- Existing prototype NPC and monster visuals remain `PENDING_CROP` / reconstruction placeholders.
- No generated image is promoted to authenticated original art.
- No unidentified official screenshot crop is asserted to be a specific NPC/monster/sprite.
- Existing background source remains as previously classified in `WorldDef`; this pass does not alter licensing/redistribution status.

## Validation
- `CombatController.java` creation: compile-only CI succeeded.
- `GameView` v0.60 + `CombatController` integration: GitHub Actions run #46, `Compile debug sources only` succeeded.
- Final `SkillDef` taxonomy correction was also locally syntax-checked with `javac` successfully; its GitHub compile-only run was still in environment setup when this history entry was finalized.
- No APK build, assembly, signing, artifact upload or distribution was performed during this autonomous pass.

## Remaining issues / next pass priority
1. Confirm the final `SkillDef` compile-only run; if it fails, fix before feature expansion.
2. Move NPC auto-approach/navigation intent out of `GameView` into a small interaction controller.
3. Split monster chase/attack orchestration out of `GameView` into a runtime AI system while preserving `[B]` labels.
4. Add action-result timing so damage does not always resolve at action start; use `[B]` impact frames until verified.
5. Add evidence-safe slot model (`SlotDef`) so right-bottom controls are data-driven rather than touch-coordinate hardcoding.
6. Keep actual sprite mapping `PENDING_CROP` unless the exact official-hosted visual is clearly identified and licensing status is separately tracked.
