# PROJECT DARK — DEV HISTORY PASS 27 · WORLD / MAP

Date: 2026-09-10
Role: World · Map Engine

## Source-of-Truth gate

Continued from the latest World handoff on `agent/world/20260910-1455` after re-checking current main lineage and canonical world rules. Original Milles geometry remains unverified, so no authored coordinate in this pass is promoted beyond `[ADAPTED]/[B]`.

## Implemented

### Data-driven exploration semantics

Added `world/WorldSpatialLayout.java` with immutable named `Area` and `Anchor` DTOs. Areas distinguish `ROAD / PLAZA / GATE / OPEN_SPACE`; anchors expose stable semantic positions for world rendering, navigation and UX adapters without forcing `GameView.java` to rediscover map meaning from pixel coordinates.

Added `world/AdaptedMillesVillageLayout.java` for the existing expanded prototype village. It explicitly defines:
- north-south road spine,
- north cross-road,
- central plaza,
- west and east lower lanes,
- south gate,
- spawn/plaza/NPC-approach/lane/gate/portal anchors.

All entries are tagged `ADAPTED/B` and `PROTOTYPE_REPLACE_WITH_VERIFIED_MILLES`; the gate/portal remains target-pending.

### Traversal audit

Added `WorldSpatialLayoutAudit` verifying:
- unique semantic IDs,
- spawn/plaza/gate anchors lie in named navigable areas,
- portal anchor lies inside a GATE area even where spatial areas overlap,
- authored inspection route exceeds 900 logical world units,
- east/west lower-lane anchors are materially separated to provide route choice.

Android-free isolated compile and audit execution: **PASS** (`WorldSpatialLayoutAudit PASS`).

## User-visible delta

Once Director/UX consumes the spatial layer, the prototype village no longer needs to be presented as an undifferentiated empty floor around collision rectangles. Road/plaza/gate regions and traversal anchors are explicit world data, enabling visible road/plaza presentation and consistent tap destinations/minimap semantics while retaining the existing camera, collision, path and portal contracts.

## Ownership / integration

`GameView.java` was not modified. Character/combat/RPG/HUD files were not modified. Director/UX should consume `AdaptedMillesVillageLayout.create()` as world-provided semantic layout data and keep screen-space UI outside this contract.

## Remaining P0

- Wire camera + move-target + spatial areas into runtime rendering/input.
- Verify on Android that named roads/plaza scroll with world-space while HUD stays fixed.
- Replace `[ADAPTED]/[B]` areas incrementally when verified Milles TILE/OBJECT/COLLISION geometry becomes available.
- South portal target map/spawn remains evidence-blocked.
