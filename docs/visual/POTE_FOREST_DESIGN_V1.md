# Pote Forest — visual and spatial draft v1

## Evidence and scope

Nexon describes Pote Forest as a distinct monster hunting area and has issued balancing updates for its monster population. That supports the forest-combat role, but does not establish an exact canonical map layout. The supplied captures show broad earth lanes, separate denser vegetation patches, and a winding stream with a crossing. Vegetation density varies by patch; the whole map should not be filled uniformly. This draft adapts those cues; its specific paths and landmarks are not asserted as original map facts.

## Ground first

- Fill the entire playfield with a continuous, source-derived dirt texture (`video_reference/terrain/dirt_path_fill_texture.png`).
- Scatter the POTE_GD cutouts with deterministic spacing and jitter as leaf-litter clumps. They are accents, not seamless floor tiles; a regular diamond carpet exposes their cutout edges.
- Use GD variants sparingly as small surface details; the continuous dirt fill remains the readable base.

## Forest grammar

- Place arrivals near the center on a broad visible trail; the first view should show the route and its nearby crossing.
- Put canopy masses at the perimeter and understory trees/shrubs behind the walking lane; collision follows trunks and solids, not the full canopy silhouette.
- Let the stream bend across the trail, with one visible bridge and traversable approaches on each bank.
- Use stumps, rocks, and small ground details as sparse landmarks at decision spaces.
- Keep the Milles travel guide one valid isometric movement step from the player spawn.

## Asset sufficiency

The current pack has 59 cutout assets across ground patches, tree masses, smaller trees, shrubs, ground cover, stumps, rocks, water, banks, and small details. That is enough to compose an initial forest region. There is no seamless forest floor source in the POTE pack, so the captured dirt fill is used as its substrate. A purpose-made adapted wooden bridge sprite crosses the stream in one tested location; this is explicitly an adaptation, not a claim about exact original placement.

## Acceptance status

The first render review exposed a coordinate binding error: the Pote camera followed the field player while the character sprite still used Milles coordinates. The route also read as isolated diamond patches between over-dense tree clumps. V2 fixes the active-world binding and rebuilds the ground accents and grove layout. CI validation covers Android tests, movement/render binding, three forest review frames, and debug APK assembly; visual acceptance remains pending review of the new captures.
The source commit is checked for valid UTF-8 before Android compilation so local file encoding cannot silently damage the CI build.
The movement regression now advances the Pote path and checks that the character renderer stays bound to the same field coordinates.

## V11 spatial correction from the source comparison

The CI render exposed that the previous creek ran diagonally through the middle third of the map. The reference and this design call for the water to frame the eastern edge. The shared creek centerline now stays in the east-side band, reaches the north and south map boundaries, and leaves one authored PNG bridge crossing; the map collider and renderer read the same centerline and bridge coordinates. QA checks a connected east-bank route, a blocked off-bridge shore point, and a disconnected east bank when the bridge deck tiles are removed.

The next rendered review showed the bridge deck extending into the eastern tree wall. Its runtime scale is reduced to match the channel width and keep both bank approaches visible.

The same review showed an oversized bare center in the playable camera. Four offset groves now break up that clearing into a narrower, irregular earth lane; the entry-to-east-bank path test guards against crowding out movement.

The next ground pass fills the woodland floor with overlapping production ground diamonds and uses a broad bare-earth route through the entry clearing.


## Acceptance checks after tile and movement rebuild
- Every floor cell uses the same 64x32 staggered diamond lattice as Milles; each direct input advances one adjacent tile in 0.60 s.
- Compare full forest, central clearing, and creek screenshots with the attached Pote references. CI image capture alone is not visual acceptance; inspect all frames and revise until the terrain reads as continuous ground and the canopy as a connected forest.
- Keep the field guide beside the entry and check the entry-to-clearing-to-water route for collision reachability.

The field map uses one shared terrain lattice for floor placement, direct-step input, and the walkable route test.

Compile follow-up: the tile renderer keeps its atlas cache and uses precomputed alpha bounds for each ground variant.

Visual iteration 2 narrows the soil route, crops tile extraction fringes, and reuses one creek segment at overlapping intervals to prevent broken water patches.

Visual iteration 3 removes the obsolete synthetic creek underlay; water now comes exclusively from a curved chain of production POTE_WT sprites.

Visual iteration 4 shifts the source creek into view between banks and fills two thin forest sectors while maintaining a clear connected trail.

Visual iteration 5 tightens the trail and increases creek segment overlap while reducing bank clutter; obstacle footprints track those same creek sections.


## Revision for playability and reference match

- Extend the eastern field bounds beyond the creek and carry the open trail through one authored bridge crossing into a second bank clearing. The stream remains blocked at all other tested points.
- Break the uniform canopy feel with three irregular dirt clearings; dense groves remain at the edge and between them.
- Register POTE_PURPLE as a B/ADAPTED runtime actor so MonsterAIController processes it. Its movement and hit use the shared tile and combat resolvers; canonical reward values remain unset.
- The QA pass checks navigation from entry over the bridge, a blocked creek outside the bridge, one-tile monster pursuit, and a shared-resolver monster hit.


## Screen recording review and V6 art pass

Reviewed representative map frames sampled every two seconds throughout the supplied 28-second Pote Forest recording. The source reads as mottled reddish-brown leaf litter with dark moss scattered over the floor, broad crooked deciduous trunks carrying separate round crown clusters, and open earth lanes dotted with low grass, small flowers, stones, and cut stumps. The former draft had a nearly uniform brown substrate, an overly regular diamond overlay, and a hand-drawn geometric crossing that did not sit in the sprite art.

V6 adds a detailed repeatable leaf-litter soil texture, reduces ground cutouts to irregular accents, introduces a branching oak sprite at a few canopy anchors, and uses a transparent pixel-art footbridge PNG placed on top of the water layer. The bridge remains a 2D game sprite and is scaled to the stream width; it is not a primitive Canvas drawing. The QA capture must confirm the bridge PNG appears over the creek, then keep movement and combat tests green.


## Continuous source-ground and tile pass V10

The former randomized GD diamond renderer produced a visible beveled checkerboard, even when its colors were blended. V10 uses the 32x16 Milles movement lattice as the ground-cell geometry, clips those adjoining diamond cells as one continuous surface, and samples a shared repeating bitmap in world coordinates. The runtime bitmap `video_reference/terrain/pote_forest_soil_v2.png` is a 576x448 composite of 24 clean 128x128 bare-ground crops from supplied recording frames at 8 s, 14 s, 22 s and 28 s. A 32-color palette preserves the captured reddish soil, fine ground noise and scattered moss without resampling. The seams between source crops are blended, and the final texture edges wrap for uninterrupted shader tiling. This terrain texture is registered in both the runtime manifest and catalog. It is a runtime texture, not a mock screenshot. POTE_GD cutouts remain available as localized ground details, and the tree, understory, stream, and bridge sprites remain independently anchored production objects. Movement, collision, and player presentation continue to use the same field adapter and navigation tile graph.

V10 visual acceptance is pending fresh rendered screenshots from the V10 APK; CI green alone is not visual PASS.

## Central arrival and regional density correction

The prior edge-creek layout did not match the supplied scenes: arrival was off to the side, the stream followed a nearly straight map edge, and the grove additions made the view too uniformly crowded. The current adapted layout moves arrival to the central trail, makes the trail visible through the arrival camera toward a crossing, and sends a bending creek across that route. A sampled creek collision blocks the banks except for a narrow bridge opening. Asymmetric groves, small plants along the trail shoulders, and open earth patches create denser and sparser regions instead of a continuous wall of vegetation. These coordinates remain an adaptation, not a claim about the original map data. Fresh rendered review and route tests are required before visual acceptance.


Runtime fixture coordinates for the adapted layout: arrival (800,528), trail through (790,510) and (1060,474) to bridge (1200,440). All are adapted coordinates, not original-map facts; the renderer and spatial tests on this branch are authoritative for the full creek point list and crossing reachability.

## Pamfet presentation and creek cleanup

The user-supplied `1000056959.jpg` direction board is split into four NW/NE/SW/SE idle crops and four matching roll-attack crops under `app/src/main/assets/pote/monsters/`. The live POTE_PURPLE render uses the same logical facing as its AI: idle selects the matching idle crop; attack wind-up selects the same-direction roll crop with a short lunge/tilt. The existing prototype combat profile and unresolved canonical stats remain unchanged. The adapted Pamfet spawn is visible in the entry encounter pocket.

Creek generation now places only water-segment sprites. Individually scattered bank, rock, bush, and groundcover props are removed from the channel; the authored bridge remains the sole crossing. CI captures all eight Pamfet presentations and asserts the creek channel has no separate non-water object anchors.

## Monster roster pose test (implementation in progress)

The Master-backed `PoteMonsterRoster` contains 16 Pote forest identities. The v0.4 review art contains 14 concept forms × 12 directional state poses. The test runtime now places the full roster at adapted, clear navigation centers across the field and routes its idle/walk/attack state plus live facing to those PNGs. Tap an actor to select it and use the existing combat controls to inspect its poses. The player can walk around an actor to exercise NW/NE/SW/SE.

This is a B/ADAPTED test deployment, not a canonical spawn population, coordinate set, stat profile, or visual acceptance. Strong Gnoll/Wolfrider/Treant use their base concept art because separate strong art is absent. `POTE_SPIRIT_TEST_B` uses the brown spirit concept under a test-only ID so the 40-HP fixture cannot claim the canonical spirit reward; the pack's black spirit form has no separate Monster_Master identity. Each walk/attack image is one representative pose, so this pass does not create looping frame animation. Build, APK, and device verification are pending for the implementation SHA.
