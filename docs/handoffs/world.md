# World handoff — PASS 38 BUILDING / ENTRANCE VISUALS

- Branch: `agent/world/20260910-1933`
- Parent World lineage: PR #61 / `agent/world/20260910-1847@98a98d11a2a02f00c9a73d50b113aceb7a3e5b60`
- Latest main checked before work: `2b66df9780142e3d84606f4ac0250dcabfa2d2b7`
- Geometry/visual status: `ADAPTED/B`; exact original Milles geometry and art remain unverified/`PENDING_CROP`.

## Canon response
No newer World canon supersedes PASS 37. Continue expanding the connected village, preserve camera/tap/collision/portal behavior, keep source-backed reconstruction preferred, and keep authored gaps explicitly replaceable `[ADAPTED]/[B]`.

## Visible map delta

### Structure visuals
- All 20 collision-aligned structures now have renderer-facing visual profiles.
- 14 HOUSE/HALL/SHOP structures render as actual layered silhouettes:
  - pitched roof
  - front facade
  - side-volume/shadow
  - eaves
  - visible door
  - exterior step
- WALL and LANDMARK structures have dedicated non-box silhouettes.
- Visual dimensions are prototype presentation only and do not change logical collision.

### Entrance layer
- Added 14 stable building entrance IDs (`entrance_<structureId>`).
- Each entrance exposes building ID, foot coordinate, visual width and exterior approach coordinate.
- Status is `VISUAL_ENTRANCE_ONLY_INTERIOR_PENDING`; no interior/portal is fabricated.
- `WorldMapProjection.entrances()` exposes the layer for future NPC/interaction routing.

### Vegetation / threshold readability
- Decorations increased from 26 to 38.
- Added entrance-adjacent BUSH objects around major hall/shop/house fronts.
- Added two visible GATEPOST objects at the outer south-gate threshold.
- South portal destination remains PENDING/fail-closed.

## Director / UX integration request
`GameView.java` remains World-non-owned.

1. Keep `AdaptedMillesMapRenderer.draw(canvas, worldAdapter)` before dynamic entity rendering.
2. The renderer now draws roof/wall/door/step building silhouettes automatically; do not overlay the old blocker/X-box presentation over these structures.
3. Continue using the same WorldRuntimeAdapter camera for map + NPC + monster + portal projection.
4. Entrances are visual/approach anchors only; do not trigger an interior transition unless a verified/accepted target map contract is added later.
5. Capture Android screenshots in central village, east market and outer south gate after integration to confirm building readability and camera scrolling.

## Verification
- IMPLEMENTED: yes.
- Static/source count: 20 structure visual profiles / 14 entrances / 38 decorations.
- BUILD VERIFIED: not claimed by World agent.
- RUNTIME VERIFIED: pending Director APK/device integration.
- `GameView.java`, CharacterRenderer, Combat, RPG, HUD and quest code were not modified.

## Next World priority
Continue visible map production rather than navigation-only audits: collision-aware vegetation/object clusters, district-specific roof/wall silhouette variation, then source-backed Milles replacements as visual identification/calibration becomes available.
