# World handoff — PASS 34

- Branch: `agent/world/20260910-1840`
- Base: `main@ef211e54ff71027477020f9347d2acf96ceb9561`
- Geometry: current Milles map remains `[ADAPTED]/[B]`; original geometry still unverified.

## Runtime integration surface
Use `WorldRuntimeAdapter` as the single World→GameView/UX integration surface.

It provides:
- `map()` → `WorldMapProjection`
- `screenToWorld()` / `worldToScreen()`
- `requestGroundScreenTap()` / `requestGroundWorld()`
- separate `requestNpcApproach(npcId)`
- `tickNavigation(dt)` with WALK first, camera follow second
- direct-input/action/explicit move cancellation
- player world/screen coordinates + camera coordinates in `FrameSnapshot`
- current portal overlap in the same frame snapshot
- `canPlayerOccupy()` matching current RuntimeState blocker/NPC/monster occupancy rules

Director/UX should not duplicate path/camera/collision logic in `GameView.java`.

## Actual map production started
`AdaptedMillesMapLayer` is now the first renderable village composition rather than only blocker rectangles/anchors.

Surface layer:
- whole-map ground
- north cross-road
- west/east road branches
- central plaza
- south spine
- south-west / south-east lanes
- south gate

Object/structure layer:
- 3 north structures
- west/east structures
- 2 south structures
- 2 plaza landmarks
- west/east perimeter structures

`WorldMapProjection.surfaces()` and `.structures()` expose these directly for renderer integration. All remain `[ADAPTED]/[B]` / `PENDING_CROP`, so they can be replaced zone-by-zone with calibrated source-backed Milles geometry/assets later.

## Director / UX integration request
1. Construct `WorldRuntimeAdapter(runtime, worldViewportWidth, worldViewportHeight)` for MILLES.
2. Empty eligible map tap → `adapter.requestGroundScreenTap(x,y)` after UI hit rejection.
3. NPC tap → `adapter.requestNpcApproach(npc.id)`; do not also send a ground move.
4. Per frame call `adapter.tickNavigation(dt)` before rendering.
5. Render `map.surfaces()` then `map.structures()` in world-space through `adapter.worldToScreen(...)`.
6. Render NPC/monster/portal from the same map/runtime projection; keep HUD screen-space.
7. South portal remains fail-closed because target map is still PENDING.

## Verification
- IMPLEMENTED: yes.
- BUILD VERIFIED: not claimed in this World pass.
- RUNTIME VERIFIED: pending Director APK/device integration.
- `GameView.java`, CharacterRenderer, Combat, RPG, HUD and quest code were not modified.

## Next World priority
Continue map creation itself: isometric tile/object composition and visual replacement of the `[ADAPTED]` zones, not more isolated audit passes.
