# Pote Forest — visual and spatial draft v1

## Evidence and scope

Nexon describes Pote Forest as a distinct monster hunting area and has issued balancing updates for its monster population. That supports the forest-combat role, but does not establish an exact canonical map layout. The supplied in-game captures are the visual reference for broad leaf-covered ground, dense tree masses, open combat lanes, and a stream along the right edge. This draft adapts those cues; its specific paths and landmarks are not asserted as original map facts.

## Ground first

- Fill the entire playfield with a continuous, source-derived dirt texture (`video_reference/terrain/dirt_path_fill_texture.png`).
- Scatter the POTE_GD cutouts with deterministic spacing and jitter as leaf-litter clumps. They are accents, not seamless floor tiles; a regular diamond carpet exposes their cutout edges.
- Reserve lighter/clearer GD variants for the start and encounter spaces so the playable route stays legible.

## Forest grammar

- Build a connected south-west entry route to the north-east clearing through negative space between groves.
- Put canopy masses at the perimeter and understory trees/shrubs behind the walking lane; collision follows trunks and solids, not the full canopy silhouette.
- Keep the stream meandering along the eastern edge with bank, water, and rock assets. It should enrich the edge without cutting the map in half.
- Use stumps, rocks, and small ground details as sparse landmarks at decision spaces.
- Keep the Milles travel guide one valid isometric movement step from the player spawn.

## Asset sufficiency

The current pack has 59 cutout assets across ground patches, tree masses, smaller trees, shrubs, ground cover, stumps, rocks, water, banks, and small details. That is enough to compose an initial forest region. There is no seamless forest floor source in the POTE pack, so the existing captured dirt fill is used as its substrate. The current pack does not contain a verified guide NPC crop, bridge/ford, or canonical zone landmark; those remain pending source assets rather than being fabricated.

## Acceptance status

This is an implemented visual draft. Build, runtime, and visual acceptance remain unverified until the Android tests and device captures are run. The GitHub Actions workflow builds the debug APK and uploads it with the device-review render captures.
