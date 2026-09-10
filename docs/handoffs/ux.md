# UX / NPC / Quest handoff

## 2026-09-10 16:14 KST — World integration preparation pass

Branch: `agent/ux/manual-20260910-1614`

### Repository state
- Latest main observed: `6aed787265488db30c50d7413dd36d966d869298`.
- Main now includes the Director-integrated World tap-move, village camera and safe portal contracts.
- `WorldMoveTargetController` and `WorldCameraTransform` are therefore consumable by UX without copying World algorithms.

### Code delta completed
- Added `WorldRuntimeBridge.java`, an UX-owned thin adapter over the World contracts.
- The bridge adapts `RuntimeState` player position and `tryMove(...)` into `WorldMoveTargetController.Walker`.
- Static blocker prefiltering uses the public `RuntimeState.blocked(...)`; actual dynamic occupancy remains authoritative in `RuntimeState.tryMove(...)`.
- Exposed screen→world, world→screen, requestGroundMove, tick, direct-input/action cancel, snapshot and camera snap/follow operations for `GameView` consumption.
- No pathfinding/collision/portal semantics were reimplemented in UX code.

### User-visible/runtime delta carried into this current-main lineage
- Top-level runtime is locked to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` while allowing either landscape orientation.
- Startup onboarding remains `전투: 몬스터 선택 → ATK / SKILL / MAGIC`.
- These previously isolated UX changes now sit on a branch based on the current World-integrated main.

### Immediate next P0
Wire `WorldRuntimeBridge` into `GameView.java`:
1. Instantiate with the gameplay viewport and snap camera on boot/respawn.
2. Project player/NPC/monster world positions via `worldToScreen` while HUD remains screen-space.
3. Reject modal/HUD/action/joystick touches before world hit-testing.
4. Convert actor hit-tests through `screenToWorld`; NPC/monster taps keep priority.
5. Empty-world tap calls `requestGroundMove(screenX, screenY)` and only accepted MOVING/REACHED requests show a tap marker.
6. Tick bridge movement into real WALK animation; never teleport.
7. New ground tap replaces prior target; joystick cancels with `cancelForDirectInput`; attack/skill/magic/NPC intent cancels with action/explicit semantics.
8. Surface REACHED/BLOCKED/CANCELLED as lightweight feedback.

### Boundaries preserved
- No CharacterRenderer internals changed.
- No CombatResolver/MonsterAI/damage logic changed.
- No inventory/reward/EXP/job/save internals changed.
- No canonical values changed.
- No ground-drop/pickup UX introduced.
