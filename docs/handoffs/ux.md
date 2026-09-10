# UX / NPC / Quest handoff

## 2026-09-10 17:02 KST — tap movement discoverability / landscape runtime pass

Branch: `agent/ux/auto-20260910-1702`
Base lineage: current main `c149434fe152cd74f9e7b0418ccdc7aa33d92867` + prior latest-main tap-move implementation from PR #36.

### Repository state
- Current main still exposes the World camera/move-target contracts and camera-correct actor hit testing.
- Generic blank-map tap movement remains implemented on the UX branch lineage, not merged to main yet.
- `WorldMoveTargetController` remains the sole owner of movement target/path semantics; UX only consumes it.
- No stable RPG action metadata DTO (`actionId/iconKey/resourceCost/cooldown/learned/state`) was found on main in this pass, so canonical HUD metadata binding remains blocked.

### User-visible delta completed this pass
- Top-level runtime is now locked to `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, preserving either landscape direction while preventing the 960×540 HUD from collapsing into portrait.
- Startup onboarding now explicitly teaches both major interaction paths: `이동: 빈 맵 터치 · 전투: 몬스터 선택 → ATK / SKILL / MAGIC`.
- This makes the newly implemented tap-to-move flow discoverable on first launch without advertising unavailable AUTO behavior.

### Tap-move P0 carried forward on this branch
- Empty map taps convert through `WorldCameraTransform.screenToWorld(...)` and call `WorldMoveTargetController.requestGroundMove(...)`.
- Movement advances through `WorldMoveTargetController.tick(...)` → `RuntimeState.tryMove(...)`; no teleport path exists.
- New taps replace previous movement targets.
- Joystick cancels with direct-input semantics.
- Combat/NPC/inventory/dialog inputs cancel or supersede movement.
- HUD surfaces are rejected before world movement.
- NPC/monster taps retain priority.
- Accepted targets render a short-lived world-anchored marker; REACHED/BLOCKED/retarget feedback is surfaced.

### Contract request still open
- RPG should expose a stable read-only action presentation DTO for ATTACK/SKILL/MAGIC/AUTO containing at least `actionId`, `label`, `iconKey/visualRef`, `resourceCost`, `cooldownRemaining/cooldownTotal`, `learned/unlocked`, `enabled/disabledReason`, and selected/active state where applicable.
- Once available on main, UX can remove prototype slot labels and render canonical press/cooldown/disabled/selected states without duplicating RPG/combat logic.

### QA focus for Integrator / next pass
1. Continuous retargeting while camera follows.
2. HUD/inventory boundary taps: no accidental ground movement.
3. Blocked tile/obstacle edge taps: BLOCKED without collision bypass.
4. Joystick during tap movement: immediate cancellation.
5. NPC/monster tap during movement: entity interaction wins.
6. Combat input during movement: action wins.
7. Camera clamp at all world edges and tap-marker anchoring.
8. Device rotation: remain in sensor-landscape with HUD geometry unchanged.

### Boundaries preserved
- No World pathfinding/collision/portal algorithms modified or copied.
- No CharacterRenderer internals changed.
- No CombatResolver/MonsterAI/damage logic changed.
- No inventory/reward/EXP/job/save internals changed.
- No canonical values changed.
- No ground-drop/pickup UX introduced.
