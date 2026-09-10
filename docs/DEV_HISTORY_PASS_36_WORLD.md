# DEV HISTORY — PASS 36 WORLD

## Goal
Move from abstract rectangle/surface definitions into actual visible map production. No new pathfinding/audit subsystem was added in this pass.

## Implemented
- Added `AdaptedMillesIsometricTileLayer`.
- Current 1600×1120 prototype world is rasterized into a staggered 64×32 diamond field with 16-unit row step.
- Generated map contains 69 staggered rows / 1,691 renderable diamond tiles.
- Tile surface breakdown for the current authored village composition: GROUND 1,072 / ROAD 409 / PLAZA 187 / GATE 23.
- Each tile carries stable row/column, projected world center, diamond hit test, surface kind, deterministic visual variant slot, evidence/status and a PENDING_CROP tile asset slot.
- Added `AdaptedMillesObjectLayer` for 11 structure/object footprints with bottom-foot painter depth keys so buildings can participate in entity/object occlusion ordering.
- `WorldMapProjection` now exposes `tiles()` and `renderObjects()` directly to the runtime renderer.
- Added `AdaptedMillesMapRenderer`, an Android Canvas renderer that draws the 1,691 diamond tiles plus static structure silhouettes with camera culling. It is explicitly an `[ADAPTED]/[B]` geometric fallback, not fabricated original Nexon art.

## Evidence / canon safety
- Exact original Milles geometry remains unverified.
- All generated tiles and structures remain `[ADAPTED]/[B]` and explicitly replaceable.
- Asset refs are renderer slots under `PENDING_CROP`; they are not claims that original Nexon tile sprites have been identified.
- Geometric fallback colors/shapes are prototype presentation only and are intentionally distinguishable from final source-backed art.
- Collision geometry remains sourced from existing `WorldDef`; this pass did not invent new collision behavior.

## Verification state
- IMPLEMENTED: yes.
- SOURCE/STRUCTURE CHECK: yes; deterministic generation yields 1,691 tiles from current bounds/surface composition.
- BUILD VERIFIED: not claimed; Director Gradle integration pending.
- RUNTIME VERIFIED: not claimed; `GameView` must call the new map renderer on-device before a visual claim can be made.

## Director integration delta
Create one `AdaptedMillesMapRenderer` and call `mapRenderer.draw(canvas, worldRuntimeAdapter)` before dynamic entity rendering. This is intentionally a one-call integration surface; do not recreate tile generation or camera projection in `GameView`.

## Next World production pass
Move from geometric fallback to authored terrain transitions and object silhouettes/assets, then progressively replace PENDING_CROP slots with source-backed Milles visual material as identification/calibration becomes available.
