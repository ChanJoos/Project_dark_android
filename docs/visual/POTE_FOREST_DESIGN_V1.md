# Pote Forest — visual and spatial draft v1

## Evidence and scope

Nexon describes Pote Forest as a distinct monster hunting area and has issued balancing updates for its monster population. That supports the forest-combat role, but does not establish an exact canonical map layout. The supplied in-game captures are the visual reference for broad leaf-covered ground, dense tree masses, open combat lanes, and a stream along the right edge. This draft adapts those cues; its specific paths and landmarks are not asserted as original map facts.

## Ground first

- Fill the entire playfield with a continuous, source-derived dirt texture (`video_reference/terrain/dirt_path_fill_texture.png`).
- Scatter the POTE_GD cutouts with deterministic spacing and jitter as leaf-litter clumps. They are accents, not seamless floor tiles; a regular diamond carpet exposes their cutout edges.
- Use GD variants sparingly as small surface details; the continuous dirt fill remains the readable base.

## Forest grammar

- Build a connected south-west entry route to the north-east clearing through negative space between groves.
- Put canopy masses at the perimeter and understory trees/shrubs behind the walking lane; collision follows trunks and solids, not the full canopy silhouette.
- Keep the stream meandering along the eastern edge with bank, water, and rock assets. It should enrich the edge without cutting the map in half.
- Use stumps, rocks, and small ground details as sparse landmarks at decision spaces.
- Keep the Milles travel guide one valid isometric movement step from the player spawn.

## Asset sufficiency

The current pack has 59 cutout assets across ground patches, tree masses, smaller trees, shrubs, ground cover, stumps, rocks, water, banks, and small details. That is enough to compose an initial forest region. There is no seamless forest floor source in the POTE pack, so the existing captured dirt fill is used as its substrate. The current pack does not contain a verified guide NPC crop, bridge/ford, or canonical zone landmark; those remain pending source assets rather than being fabricated.

## Acceptance status

The first render review exposed a coordinate binding error: the Pote camera followed the field player while the character sprite still used Milles coordinates. The route also read as isolated diamond patches between over-dense tree clumps. V2 fixes the active-world binding and rebuilds the ground accents and grove layout. CI validation covers Android tests, movement/render binding, three forest review frames, and debug APK assembly; visual acceptance remains pending review of the new captures.
The source commit is checked for valid UTF-8 before Android compilation so local file encoding cannot silently damage the CI build.
The movement regression now advances the Pote path and checks that the character renderer stays bound to the same field coordinates.

The next ground pass fills the woodland floor with overlapping production ground diamonds and uses a broad bare-earth route through the entry clearing.


## Acceptance checks after tile and movement rebuild
- Every floor cell uses the same 64x32 staggered diamond lattice as Milles; each direct input advances one adjacent tile in 0.60 s.
- Compare full forest, central clearing, and creek screenshots with the attached Pote references. CI image capture alone is not visual acceptance; inspect all frames and revise until the terrain reads as continuous ground and the canopy as a connected forest.
- Keep the field guide beside the entry and check the entry-to-clearing-to-water route for collision reachability.

The field map uses one shared terrain lattice for floor placement, direct-step input, and the walkable route test.

Compile follow-up: the tile renderer keeps its atlas cache and uses precomputed alpha bounds for each ground variant.

Visual iteration 2 narrows the soil route, crops tile extraction fringes, and reuses one creek segment at overlapping intervals to prevent broken water patches.
