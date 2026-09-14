# Milles Frame Analysis

Canonical source: `Screen_Recording_20260915_003420_YouTube.mp4`
Status: working measurement sheet; refine with additional frame extraction as implementation proceeds.

## Confirmed reference characteristics
- Overall spatial read is grass-first, not repeated brown-tile-first.
- Roads/paths branch diagonally through the visible village and define navigation/readability.
- A central fountain/plaza-like landmark provides the strongest central orientation cue.
- Trees/vegetation are grouped and often visually bounded by fences rather than scattered uniformly.
- Water/river-side or lake-side scenery forms a distinct edge district rather than a tiny isolated prop.
- Large buildings occupy outer/edge sightlines and frame the village; they should not all be clustered into the immediate spawn center merely to fill empty space.
- Landmark spacing leaves breathing room; composition should read as a town, not an asset test board.
- Camera/world scale must be validated against the video whenever user-visible scale changes are proposed.

## Implementation comparison checklist
For every Milles change, compare at least:
1. percentage of viewport occupied by plain repeated ground,
2. path direction/rhythm,
3. distance from player to nearest major landmark,
4. player-to-building scale,
5. vegetation/fence grouping,
6. water visibility and edge composition,
7. whether moving one screen in any direction still preserves coherent village context.

## Current project delta to eliminate
- Previous runtime versions exposed too much repeated ground.
- Earlier dense-town correction moved buildings inward but still did not reproduce the reference spatial hierarchy.
- `AdaptedMillesMapRenderer` must therefore be iterated from the reference composition rather than by arbitrary density tuning.

## Measurement discipline
When a worker has binary-video access, add concrete frame/time measurements here before/with implementation. Measurements should include timestamp/frame, observed feature, current runtime value if known, target/reference observation, and implementation delta.

Do not invent unseen portions of Milles. Mark them `UNRESOLVED`.