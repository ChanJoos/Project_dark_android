# UX / NPC / Quest handoff

## 2026-09-10 16:10 KST — cumulative manual UX pass

Branch: `agent/ux/manual-20260910-1610`

### Repository state
- Latest `main`: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.
- `GameView.java` on main remains v0.67.
- World branch `agent/world/20260910-1455` exposes `WorldCameraTransform` and `WorldMoveTargetController`, including screen↔world conversion, WALK/path target execution, replacement/cancel semantics, and movement status.
- Those World contracts are still not merged into main, so UX did not copy or reimplement them.
- No stable RPG action metadata DTO (`actionId/iconKey/resourceCost/cooldown/learned/state`) was found on main in this pass.

### User-visible/runtime delta completed
- Continued from the prior UX branch rather than creating another isolated one-off change.
- `MainActivity` keeps the existing startup onboarding toast: `전투: 몬스터 선택 → ATK / SKILL / MAGIC`.
- Added `SCREEN_ORIENTATION_SENSOR_LANDSCAPE` to the same cumulative branch so the 960×540 HUD remains in landscape while allowing either landscape orientation.
- This consolidates two previously separate UX deltas into one lineage for easier Director integration.

### Boundary preservation
- Only UX-owned top-level runtime wiring was changed.
- No world/camera/collision/pathfinding/portal implementation was copied into UX code.
- No CharacterRenderer, CombatResolver, MonsterAI, reward/inventory/EXP/job/save internals were modified.
- No ground-drop/pickup UX was introduced.

### P0 integration once World reaches main
1. Instantiate `WorldCameraTransform` from current WorldDef bounds and gameplay viewport.
2. Snap on boot/respawn/map transition; follow after player movement.
3. Project world actors through `worldToScreen`; keep HUD/input in screen space.
4. Reject HUD/modal touches first; NPC/monster hit-testing retains priority.
5. For empty map taps: `screenToWorld` → `WorldMoveTargetController.requestGroundMove`.
6. Tick the controller into real WALK movement; never teleport.
7. New map taps replace old targets; joystick/action/NPC inputs cancel/supersede movement via the World API.
8. Draw a short-lived tap marker only after an accepted ground-move request.
9. Surface REACHED/BLOCKED/CANCELLED outcomes as lightweight feedback.

### Other blockers
- Quest HUD still needs a stable read-only quest presentation/state DTO.
- AUTO remains disabled/presentation-only until a stable auto-combat contract exists.
- HUD action labels remain prototype-owned until RPG exposes stable action metadata on main.

### Next UX pass
- Re-check main first for World integration. If merged, tap-to-move wiring is immediate P0 and supersedes secondary polish.
- If still unmerged, continue cumulative UX work on this lineage rather than fragmenting equivalent changes across parallel UX branches.
