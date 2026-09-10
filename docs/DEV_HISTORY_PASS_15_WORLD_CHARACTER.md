# PROJECT DARK — DEV HISTORY PASS 15 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_14_WORLD_CHARACTER.md`)

`data/design/PROJECT_DARK_CANONICAL_SEED.md` was not used to override Master data.

## Backlog selected

PASS 14 identified the next dependency-free World·Character bottleneck as extracting procedural player presentation from `GameView` into an explicit renderer contract with:
- four directions: `NW / NE / SW / SE`
- minimum common presentation states: `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`
- visual layers: `BODY / HAIR / EQUIPMENT / WEAPON / EFFECT`

Authenticated original sprite frames remain `PENDING_CROP`; no frame count, sprite mapping, or original timing was invented.

## Implementation completed

### 1. Added layered CharacterRenderer

Added `app/src/main/java/com/projectdark/mobile/CharacterRenderer.java`.

The renderer defines:
- `Direction { NW, NE, SW, SE }`
- `State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }`
- `Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }`
- explicit draw order `BODY → HAIR → EQUIPMENT → WEAPON → EFFECT`
- immutable `Pose` input so gameplay/runtime state remains outside rendering code
- `hasRequiredStateContract()` audit hook

The current drawing remains a `[B]` procedural placeholder. `equipmentVisualRef`, `weaponVisualRef`, and `effectVisualRef` are present as seams for later evidence-backed assets but are intentionally unresolved as `PENDING_CROP`.

Commit:
- `d9dfb21efdd06ea258147c3346b5325f77a455db` — `Add layered character renderer contract`

GitHub Actions Run #87 completed SUCCESS before integration continued.

### 2. Delegated GameView player presentation to CharacterRenderer

Updated `GameView.java` to v0.62 and removed the large procedural player-body implementation from its `drawCharacter()` responsibility.

`GameView` now translates existing runtime state into a `CharacterRenderer.Pose`:
- existing direction integer maps to the four explicit renderer directions without changing movement semantics;
- `WALK` maps to renderer `WALK`;
- `CAST` maps to `CAST`;
- `SWING / THRUST / THROW / PUNCH` map to renderer `ATTACK`;
- `SKILL / KICK` map to renderer `SKILL`;
- player `hitFlash` maps to renderer `HIT`;
- dead player maps to renderer `DEAD`;
- otherwise maps to `IDLE`.

Combat validation, cooldown, movement, targeting, and damage logic were not moved into the renderer and were not semantically changed.

Existing prototype effect drawing remains in `GameView` for specialized throw/punch/kick/skill effects; this is retained temporarily to avoid changing combat presentation behavior in the same extraction pass. Further effect ownership consolidation is a follow-up refactor, not a canon decision.

Commit:
- `72e49df26b13096897512123c452e93f77f51dcf` — `Delegate player rendering to CharacterRenderer`

## Validation

GitHub Actions Run #101 for commit `72e49df...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

Only successfully compiling changes were retained on `main`.

## DESIGN_CONFLICT / PENDING

No canonical ID/value/relationship was changed in this pass.

PENDING retained:
- authenticated original player body frames
- authenticated hair frames / creation appearance mapping
- equipment visual mapping by canonical Equipment/Item ID
- weapon visual mapping by canonical item/action family
- effect sprite mapping
- verified original animation frame counts and timing
- final ownership migration of all player-local effects from `GameView` to the renderer

The procedural renderer remains `[B]`, not original art. Original assets remain `PENDING_CROP` until positively identified and licensing-safe.

## Result

Character presentation is no longer structurally embedded in `GameView`. The runtime now has an explicit four-direction, seven-state, five-layer rendering boundary that can later consume evidence-backed character/equipment/weapon/effect assets without coupling those mappings to combat/input logic.

## Next World · Character bottleneck

1. Introduce evidence-aware visual-slot bindings from canonical equipment/item IDs into `CharacterRenderer` without inventing missing sprite references.
2. Separate specialized player-local action effects from `GameView` into the `EFFECT` layer while preserving existing `[B]` presentation behavior.
3. Add a renderer regression/audit surface for all four directions across `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`.
4. Continue `MAP_MILLES` trace/transform preparation without guessing canonical tile-to-screen geometry.
