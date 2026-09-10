# PROJECT DARK Playtest Canon — 2026-09-10 21:49 KST

This file supersedes older presentation/movement assumptions when they conflict with this device playtest.

## User-confirmed runtime status

Baseline APK/main: `4909bb2bf28cb72ab4a3facb17521c2bf12599b1`.

- App launches successfully on device: runtime startup is verified for this baseline.
- Leftward/left-facing movement presentation visibly breaks.
- Current **SE (우하)** presentation is not visually implemented as actual SE and must be corrected independently rather than treated as a generic mirror.
- Current village/map presentation is rejected. Isometric/diamond ground tiles combined with flat 2D house drawings do not read as one game world.
- Current map is not a production-quality village; do not continue by merely adding more of the same tiles/flat boxes.
- Player movement must be tile/grid coherent. Free-form world drift or arbitrary pixel vectors are rejected.
- Gameplay direction is exactly **4 diagonal directions: NW / NE / SW / SE**. There are no N/E/S/W cardinal gameplay directions and no 8-direction expansion.
- One logical movement step means exactly one adjacent isometric tile.
- Character presentation is rejected and requires direct implementation of the latest user-provided martial-artist sprite reference rather than another concept/mockup.

## P0 reset

### Character
1. Treat the current procedural renderer as temporary runtime-safe fallback only.
2. Implement the latest user-provided character reference directly: source frame `24×32`, runtime scale `1.50`, nearest-neighbor/pixel-perfect rendering.
3. Use exactly four independently correct visual directions: `NW / NE / SW / SE`.
4. Fix asymmetric left/right presentation first and explicitly correct **SE** so face/head, torso, feet, arms and shield visually face lower-right.
5. Do not infer direction through unsafe mirroring or row reuse. Audit enum -> atlas row -> rendered facing for all four directions, including SW↔SE and NW↔NE swap protection.
6. Character is a martial artist: do not render/equip a weapon. Shield only may be shown. ATTACK uses an unarmed motion.
7. Required visual states: `IDLE / WALK / ATTACK / SKILL / HIT / DIE`.
8. Rebuild the sprite/atlas resource path with runtime-safe dimension/loading checks before making it default. Do not repeat the V5 inline/base64 constructor crash. Missing/decode-invalid/shape-invalid resources must fall back without killing app startup.

### World / Map
1. Stop treating the current `AdaptedMillesMapRenderer` output as production-quality art.
2. Ground, roads, buildings, entrances, props, shadows and collision footprints must share one isometric projection and scale language.
3. Flat front-on 2D house rectangles on diamond tiles are rejected.
4. Build one coherent village slice first: connected road/plaza + 3–5 properly projected buildings + entrances/props/trees/fences + NPC/monster placement before expanding bounds.
5. World bounds, tile counts and structure counts alone are not progress.
6. Whole gameplay screenshots are reference-only and must not be stretched as the live map texture.

### Movement
1. Gameplay movement uses exactly four diagonal steps: `NW / NE / SW / SE`.
2. One logical step = exactly one adjacent isometric tile.
3. For the current `64×32` diamond projection, adjacent tile-center screen deltas are:
   - `NW = (-32, -16)`
   - `NE = (+32, -16)`
   - `SW = (-32, +16)`
   - `SE = (+32, +16)`
   Runtime grid coordinates may differ internally, but the projected result must be equivalent.
4. Tap-to-move resolves the tap to the nearest traversable isometric tile/node.
5. Pathfinding returns an adjacent tile/node sequence, not arbitrary pixel-space drift.
6. Runtime consumes only adjacent-tile steps. Visual interpolation between centers is allowed, but logical position changes only source tile -> adjacent destination tile and lands exactly on the destination center/anchor.
7. Free-pixel target accumulation and arbitrary `(dx,dy)` movement are rejected.
8. Camera/world transforms must not change tile-step semantics.
9. World supplies Character facing directly from each `NW/NE/SW/SE` path step.
10. Required audits: one-step adjacency, NW↔SE and NE↔SW round-trip exact origin, 10-step zero accumulated drift, camera-invariant stepping, every tap-path segment adjacent, and direction/facing mapping consistency.

## Completion gate

Do not call the next visual milestone successful until a device build demonstrates all of:
- NW/NE/SW/SE travel follows the isometric tile grid exactly one adjacent tile per step;
- no accumulated pixel drift and final positions snap to tile centers;
- left-facing travel renders without corruption;
- SE visually reads as actual lower-right and is not swapped with another row/direction;
- buildings and ground read as one coherent isometric scene rather than flat houses on diamond tiles;
- the tested one-screen village slice looks like a game map, not a debug tile canvas;
- the 24×32 / 1.50 martial-artist character reads correctly with no weapon and unarmed attack;
- invalid/missing character sprite resources cannot crash app startup.
