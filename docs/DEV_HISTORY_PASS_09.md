# PROJECT DARK — DEV HISTORY PASS 09

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read before coding, in the required precedence order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md`
7. latest `docs/DEV_HISTORY_PASS_08.md`

`data/design/PROJECT_DARK_CANONICAL_SEED.md` was not used to override either Master.

## Backlog selected

PASS 08 explicitly identified renderer-owned NPC approach/dialog orchestration as the next World·Character extraction target. `GameView` v0.60 still owned:
- current approach NPC,
- current dialogue NPC,
- blocked-path timer,
- tap-to-approach state transitions,
- deterministic detour behavior.

This violated the intended ownership boundary because rendering/input code was also defining world interaction policy.

## Implementation completed

### 1. `InteractionController.java`

Added a dedicated world interaction controller that owns the NPC interaction lifecycle:

`IDLE -> REQUEST_NPC -> WALKING -> DIALOG_OPENED`

and the blocked path outcome:

`WALKING -> BLOCKED -> IDLE`

The controller exposes only stable runtime state needed by the renderer: approach target, dialogue target, movement direction, result state, cancellation, and feedback.

### 2. Evidence-safe movement policy

The existing prototype behavior was preserved rather than silently reinterpreted:
- interaction range `56f` remains **[B]**,
- approach speed `92f` remains **[B]**,
- blocked timeout `2f` remains **[B]**,
- deterministic two-side detour remains **[B]**,
- tap-to-approach remains **[ADAPTED]** mobile interaction behavior.

NPC approach still moves exclusively through `RuntimeState.tryMove()`. The controller cannot bypass collision, NPC/monster occupancy, or player alive state.

Four-direction presentation is preserved by emitting only the four screen-diagonal sign combinations. No 8-neighbor movement was introduced.

### 3. `GameView` v0.61 wiring

Removed renderer-owned `approachNpc`, `dialogNpc`, and `approachBlockedClock` state.

`GameView` now delegates:
- NPC tap request -> `InteractionController.request()`
- per-frame NPC approach -> `InteractionController.tick()`
- dialogue open/close -> controller state
- manual joystick override -> `cancelApproach()`
- combat/action override -> controller cancellation
- death/revive transition -> controller reset

The existing dialogue renderer reads the controller's current dialogue NPC without duplicating interaction state.

## Validation

Commits:
- `f45b8f67cff6429c78b2c915aba6a87df054697d` — add evidence-safe NPC interaction controller
- `d6d45b4d811e148c4386c353cf0c513910942007` — wire controller into `GameView` v0.61

GitHub Actions run #59 for the new controller: **PASS**.
GitHub Actions run #60 for the integrated `GameView`: **Compile debug sources only = PASS**.

No APK was packaged in this autonomous development pass.

## DESIGN_CONFLICT / PENDING

No new canonical ID/value conflict was introduced.

Remaining fidelity status:
- NPC identity/name/dialogue in `WorldDef` remains a **[B] interaction fixture**, not original Milles content.
- NPC sprite remains `PENDING_CROP`.
- screenshot-backed world/collision rectangles remain **[B]** and are not canonical tile geometry.
- exact original interaction range, movement speed, approach cancellation timing, and pathfinding policy remain `PENDING`; current values must not be promoted to `[O]` or `[V]`.

## Result

World interaction orchestration is no longer duplicated inside the renderer. This reduces `GameView` ownership to input/render integration and provides a single evidence-aware seam where verified NPC interaction rules can replace prototype values later.

## Next World·Character bottleneck

Highest priority in this domain:
1. split character visual composition into explicit ordered visual layers (body/hair/equipment/weapon/effect) instead of one monolithic procedural draw method;
2. preserve `PENDING_CROP` for unauthenticated sprites and do not generate replacement art as canon;
3. extend `WorldDef` from prototype blockers/spawns toward explicit Tile/Object/Collision/NPC/Spawn/Portal layer contracts without using the whole screenshot as a final map texture;
4. once RPG reward contracts land, expose world-drop entities visually without bypassing the canonical `MONSTER_DEFEATED -> world drop -> pickup` chain.
