# UX / NPC / Quest handoff

## 2026-09-10 16:26 KST — latest-main tap movement pass

Branch: `agent/ux/manual-20260910-1626`
Base main: `b7aaff72817a0ed204b53d6b38f9be478fbd7e4f`

### Repository state
- Latest main already includes Director-integrated camera/world contracts and camera-correct actor hit testing.
- Main did not yet include generic blank-map tap-to-move runtime wiring.
- Prior UX PR #35 contains an earlier tap-move implementation but its base moved and it is not merged; this pass reapplies the P0 directly on the latest main lineage instead of stacking on the stale base.

### P0 completed on latest main
- `GameView.java` now consumes `WorldMoveTargetController` directly as a thin adapter.
- Empty map taps convert through the existing `WorldCameraTransform.screenToWorld(...)` and call `requestGroundMove(...)`.
- Movement advances only through `WorldMoveTargetController.tick(...)` → `RuntimeState.tryMove(...)`; no teleport path was introduced.
- New map taps replace previous movement targets using World-owned replacement semantics.
- Joystick cancels ground movement with `cancelForDirectInput()`.
- ATTACK/MAGIC/SKILL/KICK/mode/inventory/dialog/NPC/monster interactions cancel or supersede ground movement before executing.
- HUD surfaces are rejected before actor/world hit testing so HUD taps never become movement commands.
- NPC/monster taps retain priority over generic empty-ground movement.
- Accepted movement requests render a short-lived world-anchored tap marker.
- REACHED/BLOCKED outcomes surface `이동 완료` / `이동 불가` feedback; replacement surfaces `이동 목표 변경`.
- Respawn cancels movement and snaps the camera back to the player.

### QA focus for Integrator / next pass
1. Continuous retargeting while the camera is moving.
2. Tap near HUD boundaries and inventory overlay: verify no accidental ground movement.
3. Tap blocked tiles/obstacle edges: verify BLOCKED without teleport or collision bypass.
4. Joystick during tap movement: verify immediate direct-input cancellation.
5. NPC/monster tap during movement: verify entity interaction wins.
6. Attack/skill/magic during movement: verify movement cancellation and combat intent priority.
7. Camera clamp at all world edges and tap-marker visual anchoring while camera follows.

### Boundaries preserved
- No World pathfinding/collision/portal algorithms were copied or modified.
- No CharacterRenderer internals changed.
- No CombatResolver/MonsterAI/damage logic changed.
- No inventory/reward/EXP/job/save internals changed.
- No canonical values changed.
- No ground-drop/pickup UX introduced.
