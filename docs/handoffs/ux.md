# UX / NPC / Quest handoff

## 2026-09-10 15:59 KST — automation pass

Branch: `agent/ux/auto-20260910-1558`
Draft PR: #29 `UX: lock runtime to landscape HUD orientation`
Base main: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`

### User-visible delta completed
- `MainActivity` now requests `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` before constructing `GameView`.
- The existing 960×540 landscape HUD therefore remains in landscape during physical device rotation instead of being recomputed into portrait geometry.
- Both landscape orientations remain available; immersive fullscreen behavior is unchanged.

### World tap-to-move P0 status
The required World-owned contracts now exist, but only on unmerged World branches/PRs:
- `WorldMoveTargetController` (World PR #11 / continued by #17): `requestGroundMove`, separate NPC approach request, WALK-only tick, replacement/cancel/status semantics.
- `WorldCameraTransform` (World PR #17): camera follow, `worldToScreen`, `screenToWorld`, dead-zone and clamp.

They are not present on current `main`, so UX must not copy those algorithms into `GameView` or create a cross-branch compile dependency.

### Wiring sequence immediately after World integration to main
1. Construct `WorldCameraTransform` from `WorldDef` bounds and the world viewport; `snapTo(player.x, player.y)` on boot/respawn/transition.
2. Construct `WorldMoveTargetController` with thin adapters over RuntimeState occupancy and incremental WALK movement.
3. Call camera `follow` after logical player movement and project world actors through `worldToScreen`; HUD remains screen-space.
4. Touch routing order: modal/HUD -> NPC -> monster -> joystick/action controls -> blank world.
5. Blank world: `screenToWorld` -> `requestGroundMove`. Only accepted/moving/reached requests receive visible tap-marker feedback.
6. Tick move-target while active and present player as WALK. A new blank tap replaces the old target.
7. Joystick manual input calls direct-input cancellation; combat/action calls action cancellation; NPC selection supersedes generic ground movement.
8. Never activate the adapted south portal until World exposes a verified target map/spawn contract.

### Other pending contracts
- Quest HUD still needs a stable read-only quest presentation/state DTO; do not infer quest progression from combat counters in GameView.
- RPG PR #5 exposes canonical action/reward-feed contracts but is not on main. Once integrated, consume those stable DTOs rather than duplicating RPG state.
- AUTO remains disabled until an owning domain exposes a stable auto-combat execution contract.

### Boundaries
- No map/camera/collision/pathfinding/portal algorithm was implemented here.
- No CharacterRenderer, CombatResolver/MonsterAI, RPG/reward/EXP/save internals or canonical values changed.
- Ground drop/pickup remains absent.
- Main was not merged/pushed; APK packaging was not performed.
