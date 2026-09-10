# PROJECT DARK Playtest Canon — 2026-09-10 21:49 KST

This file supersedes older presentation/movement assumptions when they conflict with this device playtest.

## User-confirmed runtime status

Baseline APK/main: `4909bb2bf28cb72ab4a3facb17521c2bf12599b1`.

- App launches successfully on device: runtime startup is verified for this baseline.
- Leftward/left-facing movement presentation visibly breaks while rightward presentation reads correctly.
- Current village/map presentation is rejected. Isometric/diamond ground tiles combined with flat 2D house drawings do not read as one game world.
- Current map is not a production-quality village; do not continue by merely adding more of the same tiles/flat boxes.
- Player movement must be tile/grid coherent. Free-form world drift or arbitrary pixel vectors are rejected.
- Movement direction target is **8-direction gameplay**, but positions/waypoints/step vectors must be derived from the isometric tile grid so travel is spatially coherent.
- Character presentation is rejected and requires redesign rather than incremental polish of the current procedural/chibi silhouette.

## P0 reset

### Character
1. Treat current procedural renderer as temporary fallback only.
2. Fix asymmetric left/right presentation first; do not mirror/transform in a way that shears or disconnects the silhouette.
3. Establish a coherent 8-direction visual contract (N, NE, E, SE, S, SW, W, NW) or an evidence-safe directional reduction with explicit mapping, but gameplay direction must remain 8-way.
4. Rebuild the sprite/atlas pipeline with runtime-safe dimensions and loading checks before making it default.
5. No further V5-style base64 atlas rollout without a constructor/runtime decode audit.

### World / Map
1. Stop treating the current `AdaptedMillesMapRenderer` output as production-quality art.
2. Ground, roads, buildings, entrances, props, shadows and collision footprints must share one isometric projection and scale language.
3. Flat front-on 2D house rectangles on diamond tiles are rejected.
4. Build one coherent village slice first (connected road/plaza + several properly projected buildings + props) before expanding bounds.
5. World bounds expansion alone is not map progress.

### Movement
1. Tap-to-move resolves the tap to the nearest traversable isometric tile/node.
2. Pathfinding returns a tile/node path, not arbitrary pixel-space drift.
3. Runtime movement consumes the path in 8-direction grid-coherent steps.
4. Final logical position must land on the tile/node anchor (except explicit local interaction offsets).
5. Camera/world transforms must not change this grid contract.
6. Add device-oriented acceptance cases for leftward, rightward, up/down diagonal and mixed 8-way travel.

## Completion gate

Do not call the next visual milestone successful until a device build demonstrates all of:
- left and right travel render symmetrically without corruption;
- 8-direction movement follows the isometric grid rather than arbitrary vectors;
- buildings and ground read as one coherent isometric scene;
- the tested village slice looks like a game map, not a debug tile canvas;
- the player sprite is visually acceptable enough to continue rather than requiring another renderer rollback.
