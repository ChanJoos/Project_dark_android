# DEV HISTORY — PASS 36 WORLD

## Goal
Move from abstract rectangle/surface definitions into actual render-ready map production. No new pathfinding/audit subsystem was added in this pass.

## Implemented
- Added `AdaptedMillesIsometricTileLayer`.
- Current 1600×1120 prototype world is rasterized into a staggered 64×32 diamond field with 16-unit row step.
- Generated map contains 69 staggered rows / 1,691 renderable diamond tiles.
- Tile surface breakdown for the current authored village composition: GROUND 1,072 / ROAD 409 / PLAZA 187 / GATE 23.
- Each tile carries stable row/column, projected world center, diamond bounds/hit test, surface kind, deterministic visual variant slot, evidence/status and a PENDING_CROP tile asset slot.
- Added `AdaptedMillesObjectLayer` for 11 structure/object footprints with bottom-foot painter depth keys so buildings can participate in entity/object occlusion ordering.
- `WorldMapProjection` now exposes `tiles()` and `renderObjects()` directly to the runtime renderer.

## Evidence / canon safety
- Exact original Milles geometry remains unverified.
- All generated tiles and structures remain `[ADAPTED]/[B]` and explicitly replaceable.
- Asset refs are renderer slots under `PENDING_CROP`; they are not claims that original Nexon tile sprites have been identified.
- Collision geometry remains sourced from existing `WorldDef`; this pass did not invent new collision behavior.

## Verification state
- IMPLEMENTED: yes.
- SOURCE/STRUCTURE CHECK: yes; deterministic generation yields 1,691 tiles from current bounds/surface composition.
- BUILD VERIFIED: not claimed; Director Gradle integration pending.
- RUNTIME VERIFIED: not claimed; map tiles are not yet wired into `GameView` on-device.

## Next World production pass
Replace placeholder tile slots with a renderer-facing terrain palette/material contract and add visible object silhouettes/edge transitions while source-backed Milles visual identification continues in parallel. Director/UX should wire the tile/object lists now rather than continue drawing the old empty prototype background.
