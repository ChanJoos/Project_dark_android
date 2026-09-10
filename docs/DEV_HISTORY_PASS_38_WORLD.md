# DEV HISTORY — PASS 38 WORLD

## Goal
Continue the existing visible Milles map-production lineage from PASS 37. This pass does not add navigation audits or touch other agents' files; it turns collision rectangles into readable village architecture and introduces stable entrance presentation contracts.

## Latest canonical check
- Latest main checked first: `2b66df9780142e3d84606f4ac0250dcabfa2d2b7`.
- No newer World canon supersedes the current requirements: keep expanding an explorable village, retain camera/tap/collision contracts, keep unverified geometry `[ADAPTED]/[B]`, and preserve source-replaceable TILE/OBJECT/COLLISION/NPC/MONSTER_SPAWN/PORTAL layers.
- Main advanced through Character work; no conflicting World design change was found.

## Implemented visible delta
- Added `AdaptedMillesStructureVisualLayer` for all 20 current structure footprints.
- 14 HOUSE/HALL/SHOP structures now have prototype wall height, pitched roof rise/overhang, door width/height, evidence/status and replaceable `PENDING_CROP` asset refs.
- WALL and LANDMARK structures receive dedicated volumetric silhouette treatment instead of generic rectangles.
- Added `AdaptedMillesEntranceLayer` with 14 stable `entrance_<structureId>` IDs and exterior approach coordinates. These are visual/approach anchors only; they do not fabricate interior maps or transitions.
- Reworked `AdaptedMillesMapRenderer.drawObjects()` so buildings render as front wall + side volume + pitched roof + eaves + door + exterior step while preserving the exact existing collision footprint.
- Expanded decoration layer from 26 to 38 objects with entrance-adjacent bushes and two outer south-gate posts.
- `WorldMapProjection` now exposes `structureVisuals()` and `entrances()` in addition to existing tile/object/decor layers.

## Canon / evidence safety
- No structure silhouette, visual height, roof shape or entrance is claimed as verified original Milles geometry/art.
- All new presentation is `ADAPTED/B` and explicitly replaceable by source-backed Milles assets.
- Existing collision rectangles remain authoritative and unchanged.
- Visual entrance markers do not imply active interiors; status is `VISUAL_ENTRANCE_ONLY_INTERIOR_PENDING`.
- South portal target remains PENDING/fail-closed.

## Ownership
- `GameView.java`: untouched.
- CharacterRenderer/Combat/RPG/HUD/quest files: untouched.
- Changes are limited to World map projection/layers/renderer and World handoff/history.

## Verification state
- IMPLEMENTED: yes.
- SOURCE/STRUCTURE CHECK: 20 visual profiles / 14 stable entrances / 38 decorations.
- BUILD VERIFIED: not claimed in this World pass.
- RUNTIME VERIFIED: pending Director integration and Android screenshot/playtest.

## Next World production
Continue the map itself: add collision-aware tree/hedge clusters and lane-side object groups, improve roof/wall silhouette variation by district, then replace PENDING_CROP slots zone-by-zone when Milles visual sources are positively identified/calibrated.
