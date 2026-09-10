# World handoff — PASS 40 LIVE VILLAGE / ARBITRARY TAP

- Branch: `agent/world/20260910-2014`
- Draft PR: `#76`
- Latest main base: `200edd0aa1a3fcbd3eacbe32c8056bc8357b2137`
- Supersedes the World-owned integration surface in unmerged Draft PR `#71` on current main.
- Geometry/visual status: `ADAPTED/B`; unverified original art remains `PENDING_CROP`.

## Canon response

`design/PLAYTEST_CANON_20260910_1938.md` remains authoritative. A stretched gameplay screenshot is not a live map. Empty-map taps must reach World only after precise visible HUD/modal rejection. Runtime acceptance still requires a new Director APK/device pass.

## Implemented World delta

- Added `WorldLiveMapLayer`, the single stable live TILE/OBJECT integration surface.
- It intentionally exposes no screenshot texture API.
- `AdaptedMillesMapRenderer` now gives every visible material a distinct authored treatment:
  - grass tufts on ground
  - stone seams/cobbles on roads
  - inset paving on plazas
  - threshold bands at the south gate
- Existing buildings, entrances, decorations and collision-aligned footprints remain intact.
- Corrected `east_outer_lane` from a building footprint to reachable road ground.

## Tap/navigation correction

- A* expansion limit now derives from the active map cell count (bounded to 65,536) instead of a fixed 4,096 that could fail on the expanded village.
- A successful WALK step resets the transient replan streak; separated temporary blockers no longer accumulate into a false permanent `BLOCKED`.
- Persistent consecutive blockers still terminate deterministically.
- `WorldRuntimeAdapter.canPlayerOccupy` now rejects player-radius overlap with map boundaries, matching runtime walking semantics.
- `WorldArbitraryTapAcceptanceAudit` covers 14 non-grid-aligned targets across central village, east market and south gate camera regions, plus bounds, replacement, direct-input cancel and transient/persistent blockers.

## Director / UX integration required

`GameView.java` remains outside World ownership and was not modified.

1. Remove/disable the stretched `WorldDef.VISUAL_SOURCE_URL` live draw path.
2. Instantiate one `WorldLiveMapLayer` for the Milles runtime.
3. Call `liveMap.draw(canvas)` before NPC/monster/player rendering; do not apply a second camera translation.
4. Call `liveMap.tick(dt)` once per navigation frame and use its transform for all world entities.
5. Route only eligible, visible, non-HUD/non-modal empty taps to `liveMap.requestGroundTap(screenX, screenY)`.
6. Keep NPC hit taps on `requestNpcApproach(npcId)`.
7. Build a fresh APK and repeat the 14 representative tap route plus screenshots at central plaza, east market and south gate.

## Verification

- IMPLEMENTED: yes.
- Isolated Java navigation compile: PASS.
- `WorldMoveTargetAudit`: PASS.
- `WorldMoveTargetExplorationAudit`: PASS on current 2240×1552 village.
- `WorldArbitraryTapAcceptanceAudit`: PASS — 14 targets.
- Renderer/`WorldLiveMapLayer` isolated compile with minimal Android graphics stubs: PASS.
- `git diff --check`: PASS.
- BUILD VERIFIED (full Gradle/APK): not claimed.
- RUNTIME VERIFIED (device): no; pending Director wiring and APK playtest.

## 2026-09-10 Director integration blocker — superseding movement canon

Latest user canon is now `design/PLAYTEST_CANON_20260910_2149.md` as corrected on main. The current `WorldMoveTargetController` does **not** satisfy it and must not be treated as tile-locked movement:

- current path lattice uses `DEFAULT_CELL_SIZE=16f`, not the 64×32 isometric tile adjacency contract;
- A* expands cardinal lattice neighbors `(±1,0)/(0,±1)` in that 16-unit world grid rather than explicit NW/NE/SW/SE adjacent isometric tiles;
- runtime `tick()` normalizes arbitrary waypoint vectors and consumes `walkSpeed*dt`, so logical movement advances by fractional/free-pixel distances;
- `reconstruct()` may append the exact arbitrary ground tap `(gx,gy)` as a final waypoint, explicitly creating a non-tile-centered final leg.

Therefore current main fails the new acceptance gate for one-step tile adjacency, zero drift, tile-center final snap, and direct World step-direction delivery to Character. Do not expand the village further until this controller is replaced/reworked around actual isometric tile nodes and the required one-step/round-trip/10-step/camera/path-adjacency audits pass.

## Next World priority

P0 is no longer map expansion. First implement the corrected 4-diagonal, one-adjacent-isometric-tile movement contract. Only after it passes should work continue on a single coherent one-screen village slice with properly projected buildings and props.

## M2 active line — first coherent isometric building

- Active branch: `agent/world/playable-village` (reuse this branch/PR for the remaining one-screen village work).
- Base main: `e04b758745c41ecef4cdc2a810846af7bcda3762`.
- M1 disposition: tile-locked movement was integrated by Director at `b8f36e6787267faca86a66bfe863713afb6c4b9e`; main CI compile and assembleDebug passed. Do not reimplement it here.

### Implemented M2 increment

- Replaced the central-plaza `plaza_landmark_a` fallback with the first authored isometric SHOP footprint.
- `AdaptedMillesIsoBuildingLayer` is the stable data surface for its 2:1 diamond footprint, wall/roof height, door foot, approach tile, evidence and asset status.
- The live `AdaptedMillesMapRenderer.draw(...)` call already used by GameView now renders two sloped wall faces, four hip-roof faces, a face-aligned door/window, projected shadow and plaza threshold stones. It does not use a flat front-facing facade rectangle.
- Map structure bounds and `WorldDef` collision bounds are both `(672,480)–(800,544)`; the visual footprint uses the same bounds.
- The entrance approach point `(768,560)` is an authored PLAZA tile center, so tap/NPC movement remains tile-locked.
- Geometry and art remain `ADAPTED/B`; unverified source art remains `PENDING_CROP`.

### Integration and verification

- No new GameView wiring is required: current main calls `AdaptedMillesMapRenderer.draw(canvas, worldAdapter)`.
- `AdaptedMillesIsoBuildingAudit`: PASS (2:1 projection, structure/collision bounds, entrance and plaza tile connection).
- Renderer compiled in isolation with minimal Android graphics stubs: PASS.
- `git diff --check`: PASS.
- Full Gradle/APK and device visual inspection are not run here; BUILD/RUNTIME VERIFIED are not claimed for this branch.

### Next one result

Extend the same isometric building data/render path to the next two structures visible in the opening camera, then add fence/tree grouping around their collision edges. Do not expand map bounds.

## 2026-09-11 PASS 41 — tile-locked runtime integration

- Branch: `agent/world/20260911-0117`
- Base: latest `main` `9639a7004c55cc5b5e2e770ee6cfa6fe16dae0c9`
- Source of truth: `design/PLAYTEST_CANON_20260910_2149.md`
- This is a clean implementation on current main; superseded Draft PR code was not used as the implementation base.

### Runtime delta

- `WorldMoveTargetController` now uses the authored `AdaptedMillesIsometricTileLayer` centers as its only navigation graph.
- Legal logical edges are exactly NW `(-32,-16)`, NE `(+32,-16)`, SW `(-32,+16)`, SE `(+32,+16)`.
- Arbitrary eligible ground taps snap to the nearest occupiable authored tile; the old exact-coordinate final leg is removed.
- Logical walking commits complete adjacent tile steps at a fixed cadence and snaps to the exact destination center. No `walkSpeed*dt` coordinate accumulation remains.
- `WorldRuntimeAdapter` normalizes the off-grid prototype spawn to the nearest traversable tile and rolls back any partial `RuntimeState.tryMove` diagonal.
- `Snapshot.lastStepDirection` is the World-owned facing authority consumed directly by Character presentation.
- Ground, NPC approach and monster approach remain distinct request kinds but share the same tile graph.

### Actual GameView path

- Joystick repeat now calls `worldAdapter.step(Direction)`; it no longer calls `state.tryMove(v*speed*dt)`.
- Empty-map taps use the snapped `Snapshot.targetX/targetY` for both walking and the visible destination marker.
- NPC taps route to `requestNpcApproach`; dialogue opens only after the tile path reaches interaction range.
- Combat auto-approach routes to `requestMonsterApproach`; the deferred action resumes only after reaching its tile goal.
- No player-owned free-pixel `state.tryMove(...)` call remains in `GameView`.

### Verification

- `IsometricTileMovementAudit`: PASS under the JDK compiler API/source runner.
- Covers all four one-step deltas, NW↔SE and NE↔SW round trips, an exact 10-step zero-drift route, off-grid tap snap, camera world↔screen round trip, and left/right paths.
- `git diff --check`: PASS.
- Full Gradle/APK: not available in this runner; BUILD VERIFIED is not claimed.
- Device runtime: pending Director build/playtest.

### Next acceptance gate

Director should build this branch and verify joystick and taps on device. Once tile locking is observed, World should move to the remaining P0 only: replace the rejected flat-front house composition with one coherent one-screen isometric village slice. Do not expand map bounds or object counts first.
