# POTE Production Asset Manifest v0.1

Evidence basis: user-supplied Pote forest screenshots (2026-09-26) + repository VIS_POTE / SCR_POTE_2004 references.
Rule: no primitive circle/oval placeholder may be classified as a production Pote asset.

## Terrain vocabulary
| ID | class | status | relationship role |
|---|---|---|---|
| TILE_ground_brown_01 | terrain | REFERENCE_DERIVED_V1 | dominant forest floor |
| TILE_ground_brown_02 | terrain | TODO | low-frequency floor variation |
| TILE_ground_moss_01 | terrain | TODO | moist/vegetated edge |
| TILE_path_bare_01 | terrain | TODO | traversed/open corridor |
| TILE_water_01 | water | TODO | stream body |
| TILE_water_edge_* | water-edge | TODO | stream/ground transition |

## Object vocabulary required before map composition
- TREE_canopy_large_A/B/C: broad dense canopy, blocking trunk footprint, canopy may overdraw actors.
- TREE_twisted_A/B: exposed crooked trunk / sparse canopy, blocking.
- TREE_dead_A/B: dead/hostile silhouette, blocking.
- TREE_young_A/B: smaller vertical tree, blocking.
- BUSH_dense_A/B/C: shrub clusters; collision depends on footprint.
- GROUND_foliage_A/B/C: fern/grass/flower clumps, non-blocking.
- STUMP_A/B, ROOT_A/B: stump/root accents; small blocker where warranted.
- ROCK_stream_A/B/C, ROCK_ground_A/B: stream/ground rocks.
- WATER_stream_A/B + WATER_edge_N/E/S/W: animated later; geometry first.

## Spatial grammar (must drive placement)
forest boundary -> dense large trees -> understory/bush -> groundcover
walkable corridor -> bare soil/path -> sparse groundcover -> encounter pocket
stream -> water body -> bank edge -> stream rocks -> moisture vegetation -> tree canopy
monster encounter -> readable open pocket -> peripheral blockers -> escape/continuation corridor
zone transition -> widening/narrowing corridor -> landmark vegetation/rock cluster -> portal

Do not start production map placement until terrain + tree/object vocabulary has usable assets and footprints.
