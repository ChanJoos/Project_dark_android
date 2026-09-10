# World handoff — PASS 36 MAP PRODUCTION

- Branch: `agent/world/20260910-1847`
- Base lineage: stacked on World PR #57 head `b4b37e0b96aae33f50e6f55a2647384b731d4e3e` because runtime adapter/map projection work is not yet in main.
- Geometry/visual status: `[ADAPTED]/[B]`; exact original Milles geometry and tile/object assets remain unverified/PENDING_CROP.

## Runtime integration surface
Use `WorldRuntimeAdapter` as the single World→GameView/UX integration surface. It owns camera projection, screen↔world conversion, ground tap movement, NPC approach, WALK ticking and current occupancy/portal lookup.

## Actual map now available
### TILE layer
`AdaptedMillesIsometricTileLayer`
- 64×32 diamond tiles
- 16-unit staggered row step
- 69 rows
- 1,691 tile instances over current 1600×1120 village
- current composition: 1,072 GROUND / 409 ROAD / 187 PLAZA / 23 GATE
- each tile exposes row/column, world center, diamond geometry/hit-test, deterministic variant slot, evidence/status and PENDING_CROP asset ref

### OBJECT layer
`AdaptedMillesObjectLayer`
- 11 static structure instances from the authored village composition
- collision footprint preserved from the same structure definitions
- foot point + depthKey supplied for painter ordering with field entities

### Visible renderer
`AdaptedMillesMapRenderer`
- Android Canvas renderer for the TILE + static OBJECT layers
- camera-culls off-screen tiles/objects
- currently uses deliberately simple geometric `[ADAPTED]/[B]` fills so the village is visible immediately without pretending unverified art is original
- source-backed tile/object art can replace each `PENDING_CROP` slot without changing navigation/collision contracts

## Director / UX integration request
`GameView.java` remains World-non-owned. Integration is now intentionally small:
1. Keep/create one `WorldRuntimeAdapter` for MILLES.
2. Keep/create one `AdaptedMillesMapRenderer`.
3. In world draw order, call `mapRenderer.draw(canvas, worldAdapter)` before player/NPC/monster rendering.
4. Dynamic entities should continue using the same camera/world projection; HUD remains screen-space.
5. Empty world taps use `worldAdapter.requestGroundScreenTap`; NPC taps use the separate NPC approach route.

Do not redraw the old empty background over this map renderer. Do not duplicate tile generation/camera math inside GameView.

## Verification
- IMPLEMENTED: yes.
- MAP DATA GENERATED: 1,691 diamond tiles + 11 static objects.
- BUILD VERIFIED: not claimed in this World pass.
- RUNTIME VERIFIED: pending Director wiring and real Android screenshot/playtest.
- No CharacterRenderer, Combat, RPG, HUD, quest or `GameView.java` modifications were made.

## Next World priority
Continue map content production: terrain transition/edge variants, richer static object silhouettes, vegetation/roadside decoration, entrance readability and then source-backed Milles tile/object replacement as source calibration becomes available. Do not return to isolated navigation-audit work unless a runtime defect requires it.
