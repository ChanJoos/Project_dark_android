# UX / NPC / Quest handoff

## 2026-09-10 19:58 KST — playtest-canon tap/world/HUD integration

Branch: `agent/ux/auto-20260910-1958`
Base main: `d0fb8aa5a7d4093a18387c9ca8b242df651ca955`

### Canonical read-first result
- Re-read `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, `design/PLAYTEST_CANON_20260910_1938.md`, current UX handoff, and relevant World DEV_HISTORY (`PASS_36_WORLD`, `PASS_38_WORLD`) before coding.
- The 19:38 device playtest amendment supersedes earlier completion claims for HUD polish, empty-map tap movement, live map presentation, character scale, and dummy reward verification.
- UX-owned deltas were implemented first. Character scale/head proportion remains Character-owned; dummy reward mutation remains RPG-owned.

### User-visible/runtime delta completed
`GameView` v0.73:
- removes the legacy captured-screenshot runtime map path entirely; no `VISUAL_SOURCE_URL` bitmap is loaded or stretched across world bounds;
- creates and consumes the World-owned `WorldRuntimeAdapter` and `AdaptedMillesMapRenderer` directly;
- draws renderer-built Milles TILE/decor/object content before dynamic NPC/monster/player entities while HUD remains screen-space;
- routes empty-world taps through `WorldRuntimeAdapter.screenToWorld(...)` + `requestGroundScreenTap(...)` and advances movement via the adapter-owned `WorldMoveTargetController`;
- replaces broad merged HUD interception rectangles with exact visible panel bounds and circular control hit regions;
- keeps NPC/monster/modal/control priority above generic world taps;
- aligns joystick/action visual geometry with touch geometry so invisible oversized rectangles no longer consume visible world;
- polishes HUD hierarchy/negative space: lighter panel opacity, tighter party/quest/target/minimap cards, compact chat, aligned status module, consistent circular combat/utility controls;
- keeps the reward banner read-only but now verifies the granted item is actually present in the same RPG inventory projection before showing an auto-loot success banner. Missing inventory state produces a warning instead of a false success message.

### Tap regression audit
Added `UxTapAcceptanceAudit`:
- 10 representative screen anchors covering center/left/right/top/bottom and corner/intermediate safe-world areas;
- rejects coordinates owned by precise HUD regions or NPC/monster hit regions;
- requires `WorldRuntimeAdapter.requestGroundScreenTap(...)` to return `MOVING` or `REACHED` for an occupiable empty target;
- runs the same 10-point acceptance once at initial camera and once after moving/snap-following the camera to a second safe world position;
- pass condition: 10/10 initial + 10/10 moved-camera acceptance.

### Cross-domain contract requests
**Character owner:** playtest canon requests overall player presentation scale `1.60 [ADAPTED]` while reducing head/body visual ratio toward `0.28–0.30`; preserve NW/NE/SW/SE. UX did not modify `CharacterRenderer` internals.

**RPG owner:** playtest canon authorizes a clearly labeled `[B]/[ADAPTED] TEST REWARD` for `combat_dummy_01`, preferably `IT_GLOVE_LEATHER x1`, solely for vertical-slice QA. UX does not fabricate/mutate this reward; once RPG emits/mutates it, v0.73 requires the same inventory row state before presenting auto-loot success.

### Boundaries preserved
- No map/camera/collision/pathfinding/portal algorithm modified.
- No CharacterRenderer internals or asset binding modified.
- No CombatResolver/MonsterAI/damage calculation modified.
- No RPG inventory/reward/EXP/job/save mutation internals modified.
- No canonical values rewritten in code.
- No ground-drop/pickup UX introduced.

### Integrator/device acceptance
1. Run `UxTapAcceptanceAudit`: require 10/10 initial and 10/10 moved camera.
2. Device-test representative empty world points after camera movement: marker + WALK unless genuinely blocked.
3. Confirm renderer-built village content replaces the stretched screenshot and no reachable black canvas gaps appear from the removed screenshot path.
4. Confirm modal/control/NPC/monster taps never leak to world movement.
5. Once RPG dummy fixture lands: kill -> reward mutation -> banner -> BAG row quantity increases exactly once.
6. Character owner change remains required before playtest P0-1 can be called complete.

## 2026-09-10 19:02 KST — inventory modal input-safety continuation

Branch: `agent/ux/auto-20260910-1902`
Parent UX head: `d24f20842d9c3be3c7aa07e70b39d832cbdbfefb` (PR #63)
Latest main verified before work: `2b66df9780142e3d84606f4ac0250dcabfa2d2b7`

### Canonical read-first result
- Re-read `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, current UX handoff, and current DEV_HISTORY lineage before implementation.
- No newer canonical rule supersedes the mobile shell, tap-to-move, diagonal character, or direct-inventory reward contracts.
- Direct inventory auto-loot remains canonical. No ground-drop/pickup behavior was introduced.
- PR #63 remains open/draft/mergeable and is the immediate UX continuity baseline; this run continues from its head rather than rebuilding the HUD from main.

### User-visible delta completed
`GameView` v0.72 fixes the inventory modal so it behaves like a real mobile-game overlay instead of a visual panel over still-active gameplay controls:
- adds a visible circular `×` close affordance to the inventory header;
- tapping the close affordance closes inventory and consumes the touch;
- tapping outside the inventory card now closes inventory and consumes the touch;
- while inventory is open, the same touch can no longer fall through to joystick, combat buttons, NPC/monster selection, or generic map tap-to-move;
- existing row selection/equip interactions remain intact.

### Remaining continuity
1. Device/integrator QA of the full HUD touch geometry and readability.
2. Stable player action/AUTO presentation/orchestration DTO remains the blocker for real player AUTO and canonical icon/learned/disabled-reason metadata.
3. Do not invent AUTO behavior or canonical action metadata while the stable contract is absent.
