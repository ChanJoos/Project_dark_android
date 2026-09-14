# PROJECT DARK — Milles World Direction

Status: ACTIVE DESIGN DIRECTION
Owner: Integration Director
Scope: `WORLD_MILLES_001`

## Purpose

Milles must be developed as a coherent place, not as a collection of assets placed around spawn.

The project will not require complete screenshot/video coverage of every part of the original Milles. Where original evidence exists, it has priority. Where evidence is incomplete, the missing areas must be designed as a coherent `[ADAPTED]` extension that preserves the observed atmosphere and gameplay function of Milles.

## Core interpretation

Milles is treated as a quiet medieval village with a readable civic structure and lived-in spatial logic.

The target is not a symmetrical showcase board. It is a believable village whose roads, districts, landmarks, water, vegetation and buildings explain why they are where they are.

## Spatial hierarchy

### Central civic square
- Primary orientation landmark.
- Fountain or well, benches, noticeboard, lamps, trees/flower beds.
- Major routes converge here.
- Must provide visual breathing room rather than dense prop packing.

### East — church / quiet district
- Church is a major landmark.
- Stone approach, garden/trees, fences, quiet seating and optional cemetery-direction language where consistent with evidence.
- Lower prop density so the church reads clearly.

### West — equipment / craft district
- Weapon/armor-related commercial functions and small workshop language.
- Crates, carts, signs and utility props may support the district.
- Narrower supporting streets than the central square.

### North — residential / service district
- Inn, general services, homes, flower/herb/potion-related activity as supported by available assets/evidence.
- More vegetation and fenced domestic spaces.

### South — market / village gate
- Stalls, carts, signs and broader route toward the village exit.
- Acts as the future connection axis to outside fields/forest content.

### South-east / water-side district
- Pond/lake/stream character, bridge or crossing where spatially useful, benches, reeds/bushes/trees and waterside path.
- Must create a quieter contrast with the civic/commercial core.

## Build order

The village must be built in this order:

1. World bounds and continuous ground foundation.
2. Main square and primary road hierarchy.
3. Secondary paths and district boundaries.
4. Water bodies, banks, bridge/crossing and terrain transitions.
5. Major landmark footprints.
6. Buildings by district and world-space coordinates.
7. Vegetation, fences and structural separators.
8. Street furniture and small props.
9. Collision, entrances, NPC/portal anchors and traversal verification.
10. Device/visual acceptance pass.

Do not start by placing buildings and fill leftover space with roads.

## Ground / road direction

Logical movement may remain on the canonical isometric navigation grid, but visible terrain must not expose the grid as a repeated checker/diamond board.

Visual terrain should read as connected surfaces:
- broad grass as village default;
- brighter stone/dirt treatment for the civic square;
- wider primary village routes;
- narrower secondary paths;
- increased stone language near church/civic structures;
- dirt/stone mix near commercial zones;
- darker/wetter transitions near water;
- denser grass/vegetation toward village edges.

Road edges should avoid hard, repetitive cell-by-cell visual boundaries where possible.

## Object and asset policy

`assets/milles/production` is a candidate asset library, not an approved final asset set.

Every object used in Milles must be classified as one of:
- `REUSE`: quality, scale, projection and style are acceptable.
- `REWORK`: usable concept/source but requires scale, anchor, palette, silhouette, padding or perspective correction.
- `REMAKE`: current asset is not sufficient for production use.
- `NEW`: required by the village design but not currently present.

Missing or inadequate objects must be recreated when needed by the village design. Existing assets must never force the layout.

Likely object families include, as needed:
- church/civic architecture and supporting fences/garden elements;
- residential/commercial buildings;
- bridge/crossing pieces;
- pond/lake bank and water-side vegetation;
- benches, lamps, noticeboards, signs, wells/fountains;
- market stalls, carts, crates and workshop props;
- trees, bushes, flowers, reeds and boundary vegetation;
- fences, walls, gates and district separators.

## Art/runtime contract for new or reworked objects

All production-bound Milles objects must follow a common contract:
- 2:1 isometric presentation compatible with the game camera;
- consistent view angle and lighting direction;
- consistent pixel density and world scale;
- transparent PNG where appropriate;
- bottom-center or explicitly defined semantic ground anchor;
- nearest-neighbour-safe rendering for pixel art;
- associated collision/footprint metadata where interactive or blocking;
- depth sorting compatible with world-space placement.

## Evidence policy

Precedence:
1. user-provided reference video/screenshots;
2. measured/derived reference analysis;
3. verified source-backed original/archive material;
4. existing project master/design data;
5. `[ADAPTED]` reconstruction.

Do not invent unsupported original facts. If a specific original spatial fact is unknown, mark it as unresolved and use an `[ADAPTED]` coherent village solution rather than leaving the map empty or creating arbitrary clutter.

## Runtime placement rule

Major buildings, water features, landmarks and district objects must use stable world-space coordinates.

Do not use `spawn + arbitrary offset` as the production layout model.

Camera movement must reveal additional coherent village space, not empty background or asset clusters falling out of frame.

## Acceptance intent

Milles is accepted only when a player can move across the map and the scene consistently reads as one believable medieval village from multiple camera positions.

A successful build should visibly provide:
- a recognizable center;
- distinct but connected districts;
- readable road hierarchy;
- natural open space;
- water-side and green-space variation;
- landmarks with enough surrounding room to read;
- no repeated debug/checker ground;
- no dense spawn-centered asset cluster;
- no empty camera-exposed voids inside intended playable bounds.

`BUILD_VERIFIED` is not `DEVICE_VERIFIED` and neither is `VISUAL_ACCEPTED`.
