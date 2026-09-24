# Milles video-derived assets

The user recordings are compressed screen captures, not the original client package. These files
are source-backed crops and keyed object candidates; they are not lossless original resources.

## Reusable runtime assets

- `terrain/grass_tile_01..03.png`: recorded outdoor grass samples masked to 2:1 isometric tiles.
- `terrain/dirt_path_tile_01..02.png`: recorded ochre path samples masked to matching tiles.
- `objects/bench_video_cutout_01..04.png`: four individually keyed benches from the outdoor footage.
- Trees, fences, lamps, well, buildings, flowers, crates and other street props are independent
  transparent PNGs in their corresponding `assets/milles/production` category folders.
- `../maps/milles_garden.json` assembles those files as anchored objects grouped by village district.
  This is editable map data; adding or moving props does not require changing renderer code.
- The 64×32 navigation grid paints a single narrow tile path for each authored branch. The path
  branches share only the central plaza; runtime does not stroke several wide path ribbons over one
  another.

## Reference-only crops

The remaining rectangular files under `objects/` and `terrain/` document source appearance and
placement context. They retain neighboring pixels and are not runtime sprites. No full-frame video
snapshot is loaded by the Milles renderer.

The 2-second fountain has a floating name label across the left basin. An AI-assisted background
extraction reconstructs that obscured edge as `../street/OBJ_fountain_milles_reference.png`.
This is an adapted asset based on the footage, not a lossless original-client sprite.

The short uneven wooden palisade in the 10-second footage was reconstructed as separate
transparent sprites `../structures/fences/OBJ_palisade_milles_reference.png` and
`../structures/fences/OBJ_palisade_diagonal_{up,down}.png`. These are AI-assisted interpretations
of the visible stakes, not exact extractions. Eleven independently anchored angled segments form
four sides of a small garden enclosure, with a southwest entrance. Each remains editable in map data.
The authored foot anchors also have matching narrow `MillesProductionCollision` footprints;
the asset audit compares every fence placement against its blocker.

The 64×32 dirt and grass diamonds remain navigation cells. Runtime draws a consistent source-cropped
grass tile below the continuous path, then repeats the existing `dirt_path_fill_texture.png` crop
along the authored centerline with mirrored seams and rounded, narrow margins. This restores the earlier connected
soil silhouette while retaining independent image assets and tile-based collision.

The first three AI-assisted transition candidates were rejected after the native render showed
disconnected paths. They remain excluded from the runtime.

`manifest.json` records source, frame, crop bounds, processing and intended use. Source pixels are
not enlarged or resampled during extraction.
