# UX / NPC / Quest handoff

## 2026-09-10 16:18 KST — map tap movement wiring pass

Branch: `agent/ux/manual-20260910-1614`
Draft PR: #35

### Repository state
- Main now includes the Director-integrated World tap-move, village camera and safe portal contracts.
- `WorldMoveTargetController` and `WorldCameraTransform` are consumed through UX-owned `WorldRuntimeBridge`; no World algorithm is duplicated in `GameView`.

### P0 completed in this pass
- `GameView.java` updated to v0.68 and now consumes `WorldRuntimeBridge`.
- Blank-map taps are converted with the active World camera and routed to `WorldMoveTargetController.requestGroundMove(...)`.
- Ground movement advances through World `tick(...)` and RuntimeState `tryMove(...)`; there is no teleport path.
- New ground taps replace prior moving targets through the World controller's native request semantics.
- Manual joystick input cancels tap movement using `cancelForDirectInput()`.
- ATTACK / MAGIC / SKILL / KICK / attack-mode input cancels or supersedes tap movement with action semantics.
- NPC taps explicitly cancel ground movement and retain priority over generic map taps.
- Monster taps cancel ground movement, select the target, and show `타깃 선택 · <name>` feedback.
- HUD, utility rail, inventory, joystick and action-button regions are rejected before generic map movement.
- Accepted ground targets show a short-lived world-anchored tap marker.
- REACHED and BLOCKED movement outcomes surface as lightweight feedback.

### Camera / projection wiring
- Player, NPCs and monsters are projected through `WorldCameraTransform.worldToScreen(...)`.
- Actor hit tests convert screen input through `screenToWorld(...)` before calling RuntimeState hit tests.
- Camera follows manual, NPC, combat-approach and tap-target movement; respawn snaps camera back to player.
- Background bitmap source cropping now tracks the World camera using logical World bounds so the presented world scrolls with the actor projection.
- HUD remains screen-space.

### Existing UX carried on this branch
- Sensor-landscape runtime orientation.
- Startup onboarding: `전투: 몬스터 선택 → ATK / SKILL / MAGIC`.

### Boundary preservation
- No pathfinding/collision/portal algorithm implemented in GameView or the bridge.
- No CharacterRenderer internal implementation changed; only its Pose coordinates are projected before consumption.
- No CombatResolver/MonsterAI/damage internals changed.
- No inventory/reward/EXP/job/save internals changed.
- No canonical values changed.
- No ground-drop/pickup UX introduced.

### Follow-up QA / next UX priorities
1. Android runtime verify map-image crop alignment against logical World geometry and actor locations.
2. Verify edge-of-map camera clamp, repeated target replacement, blocked-path feedback and joystick/action cancellation on-device.
3. Wire portal presentation lifecycle once destination content is evidence-safe; target-pending south gate must remain non-teleporting.
4. Replace hard-coded quest HUD once a stable read-only quest presentation DTO reaches main.
5. Wire AUTO only after a stable auto-combat API exists.
