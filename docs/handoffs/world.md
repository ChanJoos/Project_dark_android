# World handoff — PASS 24

- Base `main`: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`
- Master: M001 / current 92-CSV baseline (no Master values changed)
- Branch: `agent/world/20260910-1415`
- Draft PR: pending creation after this commit

## Implemented

- Added `world/WorldMoveTargetController.java`.
- Ground tap and NPC approach are separate entry points.
- New target replaces the active target using a monotonically increasing request ID and reports the superseded request ID.
- Direct input, action input and explicit cancellation have distinct reasons.
- Four-neighbour A* produces WALK waypoints and calls the runtime-provided `Walker.tryWalkStep`; it never writes player coordinates or teleports.
- World bounds and `NavigationWorld.canPlayerOccupy` gate every path node.
- Target completion tolerance, unreachable/out-of-bounds `BLOCKED`, bounded search and bounded dynamic replanning are explicit.
- Added deterministic `WorldMoveTargetAudit` for collision detour, NPC-contract separation, reach, direct-input cancellation and invalid target failure.

## Integration request (Director / UX owner)

Do not duplicate path logic in `GameView.java`.

1. Adapt `RuntimeState` to `NavigationWorld` and `Walker`; `canPlayerOccupy` should expose the existing player collision/occupancy result without changing ownership of collision.
2. Convert eligible screen taps using the current camera transform, then call `requestGroundMove(worldX, worldY)`.
3. For an NPC hit, call `requestNpcApproach(npcId, npc.x, npc.y, interactionRadius)` instead of the ground API.
4. Drive `tick(dt)` only while player action state permits WALK; direct joystick calls `cancelForDirectInput()`, combat/action calls `cancelForAction()`.
5. Render tap feedback from `Snapshot`; UX owns the marker, world owns command status.

No `GameView.java` or director-owned integration file was changed.

## Validation

- Source/static review: completed.
- Isolated Java compile: PASS using the JDK compiler module:
  `java com.sun.tools.javac.Main -d /tmp/dark-world-audit-pass24 WorldMoveTargetController.java WorldMoveTargetAudit.java`
- Deterministic audit execution: PASS:
  `java -cp /tmp/dark-world-audit-pass24 com.projectdark.mobile.world.WorldMoveTargetAudit`
- Full Gradle compile: not run locally because this execution image has no `gradle` executable.
- GitHub Actions: pending; repository workflow currently triggers only on pushes to `main`, while workers are prohibited from pushing/merging main.
- APK/runtime: not verified. Director integration is required before the feature is user-operable.

## Remaining

- Director wiring listed above.
- Camera/world transform and target marker remain UX-owned.
- Portal transition callback remains a future world API extension after a portal definition exists; movement already delegates every physical step to the runtime so portal checks can remain authoritative.
- Verified legacy Milles pixel calibration remains blocked as recorded in PASS 23; this implementation does not fabricate geometry.
