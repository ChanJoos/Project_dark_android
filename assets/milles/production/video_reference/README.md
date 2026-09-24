# Milles video-derived assets

The PNGs in this folder were extracted from the two user-provided recordings named in
`manifest.json`. The recordings are compressed screen captures, not original client package files,
so these are faithful crops from the footage rather than lossless recovery of the game's source art.

## Runtime terrain

- `terrain/grass_tile_01..03.png`: three recorded outdoor grass samples masked to 2:1 isometric diamonds.
- `terrain/dirt_path_tile_01..02.png`: two recorded dirt-path samples masked to 2:1 isometric diamonds.
- `terrain/dirt_path_fill_texture.png`: a repeatable stroke texture derived from the first dirt sample.
- `AdaptedMillesMapRenderer` tiles the recorded grass beneath the scene and draws dirt as rounded,
  textured ribbons along authored paths. The gameplay/navigation grid remains 64×32. It also draws
  existing production PNG assets for buildings, trees, bushes, fence, lamps, noticeboard and well.
  The layout is a creative reconstruction, not a restoration of the original map.

## Video-derived object sprites

- `objects/bench_video_cutout_01..04.png`: four individual benches keyed from the recorded lawn. These
  are transparent runtime sprites selected across the seating locations. They retain source video
  compression and need device review for edge artifacts.

## Reference crops

The other files under `objects/` and the non-tile images under `terrain/` are labeled rectangular
reference crops. They show fences, lamps, tree enclosure/well, buildings, inn interior, flooring and
path context, but retain neighboring scene pixels and are not runtime sprites. This distinction avoids
presenting a scene crop as a finished isolated asset.

`manifest.json` gives each crop's recording, frame, approximate time, exact source rectangle, output
size, processing and intended use. The source pixels were not enlarged or resampled.
