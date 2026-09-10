# World handoff — PASS 37 MAP EXPANSION

- Branch: `agent/world/20260910-1847`
- Draft PR: #61
- Code commit: `de9ea41994c0bdf5a81789f0481a4c1bcf36c649`
- Geometry/visual status: `ADAPTED/B`; exact original Milles geometry and art remain unverified/`PENDING_CROP`.

## Latest canonical response

The Design Constitution now explicitly says the current expanded prototype is still too small. PASS
37 therefore expands the physical world before continuing cosmetic-only work.

## Visible map state

### Extent and districts
- Bounds: 2240×1552 logical units, previously 1600×1120.
- Existing central village is preserved.
- New connected regions: east market square, east outer lane, south commons and outer south gate.
- Target-pending south portal moved to the new outer boundary; destination remains unresolved and disabled.
- New exploration anchors: `east_market`, `east_outer_lane`, `south_commons`.

### TILE layer
- 3,312 renderable 64×32 diamonds, previously 1,691.
- 2,123 GROUND / 640 ROAD / 527 PLAZA / 22 GATE.
- 710 tiles have transition-edge masks so roads, plazas and gates have readable borders.
- Tile asset refs remain replaceable `PENDING_CROP` slots.

### OBJECT and decoration layers
- 20 collision-aligned structures, previously 11.
- 26 non-collision visual decorations: trees, fences, signs, well, benches and lamps.
- Decorations expose stable IDs, depth keys, evidence/status and asset refs.
- `WorldMapProjection.decorations()` exposes the new layer.
- `AdaptedMillesMapRenderer` draws tiles, transitions, decorations and static structures.

## Director / UX integration request

`GameView.java` remains World-non-owned.

1. Keep one `WorldRuntimeAdapter` and one `AdaptedMillesMapRenderer`.
2. Draw `mapRenderer.draw(canvas, worldAdapter)` before dynamic player/NPC/monster rendering.
3. Do not redraw the old empty background over the map.
4. Continue routing eligible empty-map taps through `requestGroundScreenTap`; NPC taps remain separate.
5. Use the same camera projection for all dynamic entities. The expanded bounds are consumed automatically.
6. Capture an Android screenshot/playtest of traversal from the central plaza to east market and south gate.

## Verification

- Isolated Java compilation: PASS.
- `MapExpansionAudit PASS`.
- Draft PR #61 remains open, draft and mergeable.
- Full Gradle/APK/runtime screenshot: not verified by the World agent.

## Next World priority

Continue visible map production: replace rectangular structures with roof/wall/door silhouettes, add
collision-aligned vegetation and entrance objects, then replace `PENDING_CROP` visual slots with
source-backed Milles assets as calibration becomes available. Navigation-only audits are not the
priority unless an actual runtime defect appears.
