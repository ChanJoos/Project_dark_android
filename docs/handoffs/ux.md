# UX / NPC / Quest handoff

## 2026-09-10 20:27 KST — original-inspired HUD/action-grid adaptation

Branch: `agent/ux/auto-20260910-1958`

### User-directed visual delta
- Applied the user-approved reference direction directly to runtime HUD rather than generating another detached mockup.
- `GameView` advanced to v0.74.
- Left/top information architecture now follows the supplied original-game references more closely: compact top-left player HP/MP + level/buff block, left quest panel, top-right utility group + minimap, translucent bottom-center chat, bottom-left joystick.
- Rebuilt the bottom-right combat shell around a 2x5 square quick-slot grid plus a large dedicated attack button, mode control and AUTO control.
- Removed the previous text-heavy circular SKILL/MAG/KICK arrangement as the primary presentation.

### Icon implementation
- Added `ClassicHudIconAtlas.java`.
- It reconstructs the supplied reference vocabulary as crisp procedural/vector icons: slash, wave, claw, burst, aura, flame, palm, spiral, body, comet, mode, auto and attack/sword motifs.
- No whole screenshot or cropped screenshot texture is shipped in the runtime. The user screenshots are reference material; the runtime icons are `[ADAPTED]` reproductions so scaling/pressed/disabled states remain controllable in code.
- Existing live actions are wired into the first three quick slots: SKILL, MAGIC and KICK. Remaining slots are visible quick-slot placeholders until stable learned-action metadata is available.
- Large sword button invokes the existing ATTACK path. Existing combat calculation is not duplicated or modified.

### Input/tap safety
- HUD hit testing was updated to match the new visible panels, 2x5 quick-slot cells, attack button, mode/AUTO controls and utility icons.
- Invisible broad interception rectangles were not reintroduced.
- Generic empty-world taps still route to `WorldRuntimeAdapter.requestGroundScreenTap(...)` after modal/control/NPC/monster priority.
- Existing `UxTapAcceptanceAudit` remains the regression gate and must still pass 10/10 on initial and moved camera.

### Ownership boundaries
- No World algorithms changed.
- No CharacterRenderer internals changed.
- No Combat resolver/math/MonsterAI changes.
- No RPG mutation/reward/save internals changed.
- AUTO remains presentation-only/locked until a stable player AUTO orchestration contract exists.

### Device acceptance for this delta
1. Visually confirm the lower-right 2x5 grid reads as one original-inspired skill deck, not isolated debug circles.
2. SKILL/MAGIC/KICK slots and the large sword ATTACK button must invoke the same existing runtime action paths.
3. Disabled/pressed/selected states must remain visually legible.
4. Empty world around and between visible HUD regions must still accept tap-to-move with marker + WALK.
5. Re-run `UxTapAcceptanceAudit` after integration.

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
