# PROJECT DARK — DEV HISTORY PASS 16 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_15_WORLD_CHARACTER.md`)

Latest user canon was also respected: monster item rewards use direct inventory auto-loot and no ground-drop/pickup path. This World·Character pass did not reintroduce any world loot entity or pickup behavior.

## Backlog selected

PASS 15 left two immediate character-presentation tasks:
1. evidence-aware equipment visual binding,
2. moving remaining player-local action effects out of `GameView` into the renderer `EFFECT` layer.

Current RPG equipment state is not yet directly integrated into `GameView`; forcing a cross-owner equipment bridge from World·Character would duplicate Integrator/RPG orchestration responsibility. Therefore the dependency-free task selected for this pass was full ownership migration of existing player-local prototype effects.

## Implementation completed

### 1. CharacterRenderer now owns prototype player-local effects

Updated `CharacterRenderer.java` with presentation-only `EffectFamily`:
- `NONE`
- `CAST`
- `THROW`
- `PUNCH`
- `KICK`
- `SKILL`
- `HIT`

`Pose` now carries the effect family alongside the existing direction/state/layer contract.

The renderer `EFFECT` layer now owns the existing prototype visual vocabulary for:
- cast rings,
- throw projectile,
- punch/kick/skill arc effects,
- hit feedback.

These visuals remain `[B]` procedural presentation and are not claimed to be authenticated original LOD effects. Exact effect sprites and timings remain `PENDING_CROP` / unverified.

The KICK body pose now keys from the presentation-only `EffectFamily.KICK`, preserving the distinction between generic `SKILL` presentation state and the dedicated martial-artist kick action family.

Commit:
- `0ff25628174b7284cf7cbe7627410f3fe962c13f` — `Move player action effects into CharacterRenderer`

### 2. GameView no longer draws player-local action effects

Updated `GameView.java` to v0.63.

Removed `drawActionFx()` from the render path and removed its implementation.

Added one-way mapping from existing runtime action semantics into renderer effect presentation:
- `CAST -> CAST`
- `THROW -> THROW`
- `PUNCH -> PUNCH`
- `KICK -> KICK`
- `SKILL -> SKILL`
- player hit flash -> `HIT`
- other actions -> `NONE`

`GameView` still owns input/combat action selection and does not move damage, cooldown, targeting, range, resource, or combat resolution into the renderer.

Commit:
- `684f000e84eb1936880d6387b66e89dd46de5a3d` — `Route player effects through CharacterRenderer`

## Validation

GitHub Actions Run #112 for commit `684f000e...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

Only successfully compiling integrated changes are treated as PASS 16.

## DESIGN_CONFLICT / PENDING

No canonical item/monster/skill/NPC/map/progression IDs or values were modified.
No monster ground-drop behavior was introduced.

PENDING retained:
- authenticated original player body/hair/equipment/weapon/effect frames,
- canonical equipment Item ID -> visual asset binding,
- canonical weapon Item ID -> visual asset binding,
- verified original animation/effect frame counts and timings,
- direct RPG/Integrator-owned equipment-state bridge into character presentation,
- renderer regression/audit surface across all four directions and seven presentation states,
- canonical `MAP_MILLES` tile/object/collision/portal trace geometry.

## Result

`GameView` no longer owns player-local action-effect drawing. Character body, hair, equipment placeholder, weapon placeholder, and player-local effects now all pass through the explicit `CharacterRenderer` layer boundary.

This leaves `GameView` focused on runtime orchestration while keeping visual substitution seams ready for later evidence-backed assets.

## Next World · Character bottleneck

1. Add an evidence-aware visual binding object that can accept canonical equipped Item IDs without inventing unresolved sprite refs; actual integration must use the RPG/Integrator-owned equipment state rather than duplicating it.
2. Add renderer regression/audit coverage for `NW/NE/SW/SE × IDLE/WALK/CAST/ATTACK/SKILL/HIT/DEAD`.
3. Continue `MAP_MILLES` transform/trace preparation without guessing Master tile-to-screen geometry.
4. Keep all original sprite/effect replacements `PENDING_CROP` until positively identified and licensing-safe.
