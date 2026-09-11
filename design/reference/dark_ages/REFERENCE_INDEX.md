# DARK Reference Archive — Source of Truth

PROJECT DARK visual reconstruction must use Dark Ages / 어둠의전설 references first. Generic retro-MMORPG, Mir2-like, or other third-party game styling is rejected.

## Canonical visual rules
- Primary visual source: user-provided original Dark Ages screenshots + Nexon Dark Ages official archive.
- Character facing: NW / NE / SW / SE diagonal 3/4 view. Front-facing RPG sprites are rejected.
- Preserve the original game's small adult-proportioned character silhouette and dense/irregular terrain texture.
- World art must read as a continuous old Dark Ages map, not isolated modern 2:1 showcase tiles.
- Do not claim reconstructed/generated assets are original game assets.
- Never use Legend of Mir 2 or another third-party MMORPG as a visual reference.

## Reference tiers
- P0: user-provided screenshots of the target old-Dark-Ages visual lineage.
- P0: Nexon official archive showing old town/map imagery.
- P1: Nexon official historical screenshots useful for object/character/terrain extraction.
- P2: modern Nexon screenshots, usable only for continuity/location evidence, not to override P0 appearance.

## Verified official reference seeds

### DA-OLDMAP-001 — Old-town map archive — P0
- URL: https://lod.nexon.com/Community/screenshot/136184?Category2=2
- Title: 구 마을맵 지도 동서북 밀레스 구아벨 구뤼케시온 구루어스 구마인
- Published: 2021-06-01
- Attachment count visible in official archive: 9 images.
- Coverage named by official post: 밀레스 / 아벨 / 뤼케시온 / 루어스 / 마인 old-town maps.
- Use: world layout, continuous terrain, road/grass boundaries, building density and old-map composition.
- Classification: OFFICIAL_ARCHIVE / OLD_MAP / P0

### DA-MILLES-2024-001 — Milles event-map screenshot — P2
- URL: https://lod.nexon.com/Community/screenshot/137421?Category2=2
- Title: [넥슨30주년] 축하해~ 어둠의 전설 화이팅!
- Published: 2024-04-11
- Explicitly states screenshot was taken in 밀레스 이벤트 맵.
- Use: modern continuity/reference only; do not override old-map P0 art direction.
- Classification: OFFICIAL_ARCHIVE / MILLES / MODERN_REFERENCE / P2

### DA-MILLES-INDEX-2024-001 — Milles screenshot cluster — P2
- URL: https://lod.nexon.com/Community/screenshot?Category2=2&Page=20
- Verified visible titles include: 밀레스공원, 업뎃 후 밀레스 마을, 업뎃 후 밀레스 마을2.
- Use: locate additional Milles screenshots and compare later revisions.
- Classification: OFFICIAL_ARCHIVE / MILLES / SCREENSHOT_CLUSTER / P2

### DA-MILLES-INDEX-2024-002 — Milles screenshot cluster — P2
- URL: https://lod.nexon.com/Community/screenshot?Category2=2&Page=11
- Verified visible titles include: 밀레스마을에서 한컷!, 밀레스마을,행복한30주년!, 밀레스 3단 케이크 앞.
- Use: additional Milles visual sampling; modern-reference only.
- Classification: OFFICIAL_ARCHIVE / MILLES / SCREENSHOT_CLUSTER / P2

### Official screenshot archive index
- URL: https://lod.nexon.com/community/screenshot
- Classification: OFFICIAL_ARCHIVE / SCREENSHOT_INDEX

### Historical screenshot seed
- URL: https://lod.nexon.com/Community/screenshot/121162?Category2=2
- Published: 2004-05-13
- Classification: OFFICIAL_ARCHIVE / HISTORICAL / P1

## User-provided P0 visual observations
These are extraction rules from the target screenshots supplied in the project conversation, not claims about every historical Dark Ages build.

### Outdoor target
- Terrain reads as continuous textured ground rather than a showcase grid of isolated diamond tiles.
- Grass/dirt boundaries are irregular and dense.
- Vegetation is integrated into the ground composition rather than presented as cute standalone props.
- Character occupies a small fraction of screen/world scale.

### Indoor target
- Large continuous floor plane, walls, stairs, counters/furniture and black outside-map void form one room composition.
- Interior objects share the same low-resolution texture density as the floor/walls.

### Martial-artist target
- Small adult-proportioned silhouette, not modern chibi.
- Silver/white hair, red headband, bare upper torso, dark navy lower garment, dark gloves, red/white footwear.
- Four facings are diagonal 3/4: NW / NE / SW / SE.
- SE must visibly face down-right; front-facing/down-facing RPG sprites are invalid.
- Native frame contract remains 24x32 with runtime scale 1.50 unless verified source extraction proves a better canonical frame contract.

## Extraction queue
1. P0 old Milles map images from DA-OLDMAP-001.
2. Outdoor terrain samples: grass, dirt/path, edge transitions, vegetation clusters.
3. Milles architecture: exterior wall/roof/door/window/stair vocabulary and relative scale.
4. Indoor samples: wood/stone floor, walls, stairs, counters, furniture, map-edge void.
5. Martial artist: silhouette, diagonal facing, head/body proportions, foot anchor.
6. NPC/monster/item references only after first playable Milles screen passes visual comparison.

## Acceptance policy
A production visual asset is rejected when it:
1. looks like Mir2 or another generic old MMORPG rather than Dark Ages;
2. changes the diagonal character-facing contract;
3. replaces original Dark Ages proportions with modern cute/high-detail pixel-art proportions;
4. uses decorative architecture/props unsupported by the reference archive;
5. is generated from text-only style prompting without a Dark Ages reference basis;
6. exposes obvious repeated 64x32 diamonds when the target reference reads as continuous terrain;
7. is wired into runtime before a P0 side-by-side visual comparison passes.

## Current status
- Milles v0.1: REJECTED — Mir-like visual direction.
- Milles v0.2: REJECTED — still generic modern isometric pixel-art direction.
- Previous front-facing character preview: REJECTED.
- Generic AI-generated Dark-Ages-like sheet: REJECTED — insufficient reference fidelity.
- Active next gate: extract/normalize P0 old-Milles visual vocabulary before generating or wiring replacement production art.
