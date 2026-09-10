# PROJECT DARK — DEV HISTORY PASS 24 · WORLD

Date: 2026-09-10
Role: World · Map Engine
Base main: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`

## Source-of-Truth gate

Read current `AGENTS.md`, director guide/backlog, Master manifest/reconciliation, design constitution/data contract/source-of-truth, PASS 23 World history, WorldDef/runtime projection and world Master tables. `docs/handoffs/world.md` did not exist on main and is created in this pass.

The newest user-canon P0 supersedes the earlier evidence-mining-only next step: world must expose tap-target movement while preserving collision, bounds, pathfinding, portal authority and NPC priority. Milles geometry remains `[B]`/`PENDING_CROP`; no map value was promoted.

## IMPLEMENTED

Added a stable world-side move-target contract in `com.projectdark.mobile.world`:

- `WorldMoveTargetController`
  - distinct `requestGroundMove` and `requestNpcApproach` APIs;
  - request IDs plus explicit `replacedRequestId` replacement semantics;
  - `MOVING / REACHED / BLOCKED / CANCELLED` outcomes;
  - explicit direct-input/action/explicit cancel reasons;
  - four-neighbour bounded A* with occupancy and map-bound gates;
  - WALK-only incremental movement through a runtime sink;
  - completion tolerance and bounded dynamic replanning.
- `WorldMoveTargetAudit`
  - deterministic obstacle-detour, NPC separation, completion, cancellation and out-of-bounds checks.

This is an integration-ready world delta, not a `GameView.java` implementation. The adapter request is recorded in `docs/handoffs/world.md`.

## Verification status

- PLANNED: complete.
- IMPLEMENTED: complete on the role branch.
- BUILD VERIFIED: partial only. Both new Java sources compile and the deterministic audit passes via `java com.sun.tools.javac.Main`; full Gradle/APK build is not verified because the local image has no `gradle` executable and the current workflow triggers only on main.
- RUNTIME VERIFIED: no. Director/UX wiring and Android execution are still required.

## Canon/evidence safety

- No canonical ID, coordinate, portal or geometry was invented or changed.
- No screenshot was used as a final texture.
- No ground-drop/pickup behavior was introduced.
- The controller does not mutate logical position directly and cannot bypass the runtime's collision/portal step hook.
- Generic ground taps cannot silently masquerade as NPC interaction because the request types and entry points are separate.

## Next world task

After Director integration, add a world-owned camera transform/dead-zone/clamp contract outside `GameView.java`, then resume evidence-backed Milles reconstruction when actual legacy pixels become available.
