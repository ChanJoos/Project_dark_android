# DARK Reference Archive — Source of Truth

PROJECT DARK visual reconstruction must use Dark Ages / 어둠의전설 references first. Generic retro-MMORPG, Mir2-like, or other third-party game styling is rejected.

## Canonical visual rules
- Primary visual source: user-provided original Dark Ages screenshots + Nexon Dark Ages official archive.
- Starter player canon: 평민 / PEASANT / pre-class. Martial artist is not the base character.
- Character facing: NW / NE / SW / SE diagonal 3/4 view. Front-facing RPG sprites are rejected.
- Preserve the original game's small adult-proportioned character silhouette and dense/irregular terrain texture.
- World art must read as a continuous old Dark Ages map, not isolated modern 2:1 showcase tiles.
- Do not claim reconstructed/generated assets are original game assets.
- Never use Legend of Mir 2 or another third-party MMORPG as a visual reference.

## Reference tiers
- P0: user-provided screenshots of the target old-Dark-Ages visual lineage.
- P0: Nexon official archive showing old town/map imagery.
- P1: Nexon official historical screenshots useful for object/character/terrain extraction.
- P2: modern Nexon screenshots/guides, usable for system continuity and identity evidence; they must not override P0 appearance.

## Verified official reference seeds

### DA-OLDMAP-001 — Old-town map archive — P0
- URL: https://lod.nexon.com/Community/screenshot/136184?Category2=2
- Title: 구 마을맵 지도 동서북 밀레스 구아벨 구뤼케시온 구루어스 구마인
- Published: 2021-06-01
- Attachment count visible in official archive: 9 images.
- Coverage named by official post: 밀레스 / 아벨 / 뤼케시온 / 루어스 / 마인 old-town maps.
- Use: world layout, continuous terrain, road/grass boundaries, building density and old-map composition.
- Classification: OFFICIAL_ARCHIVE / OLD_MAP / P0

### DA-MILLES-2005-001 — Historical Milles population screenshot — P0/P1 bridge
- URL: https://lod.nexon.com/Community/screenshot/125266?Category2=2
- Title: 밀레스의 노숙자들
- Published: 2005-11-26
- Official archive text explicitly identifies the location as 밀레스 and includes a screenshot.
- Use: period-authentic character/world scale, crowd density, old Milles surface/object appearance.
- Classification: OFFICIAL_ARCHIVE / MILLES / HISTORICAL / P1; promote visual traits matching user P0 screenshots.

### DA-OLDMILLES-2022-001 — Official old-Milles reopening evidence — P0 context
- URL: https://lod.nexon.com/community/game/7529?SearchBoard=1
- Title: [이벤트] 세오 200년 "마이소시아 시간 탐험"
- Published: 2022-04-14
- Official post explicitly records `(구)밀레스 마을` reopening and Milles coordinate 55,87.
- Use: confirms old-Milles is an explicit official historical map lineage rather than a generic reconstruction target.
- Classification: OFFICIAL_ARCHIVE / OLD_MILLES / HISTORICAL_CONTEXT / P0-CONTEXT

### DA-HIST-2004-001 — Historical character screenshot
- URL: https://lod.nexon.com/Community/screenshot/121162?Category2=2
- Published: 2004-05-13
- Official archive contains a period screenshot of multiple game characters.
- Use: period character scale/proportion evidence when consistent with target P0 lineage.
- Classification: OFFICIAL_ARCHIVE / CHARACTER / HISTORICAL / P1

### DA-CHARCREATE-2025-001 — Official character creation contract — P2 system evidence
- URL: https://lod.nexon.com/info/guide/82283
- Official guide states creation selects gender, hair and hair color before naming the character.
- Use: supports a class-neutral BODY_BASE + HAIR_STYLE + HAIR_COLOR starter architecture rather than hard-coding a class costume as the base avatar.
- Classification: OFFICIAL_GUIDE / CHARACTER_CREATION / SYSTEM_CONTINUITY / P2

### DA-PEASANT-2026-001 — Official class-state continuity evidence — P2 system evidence
- URL: https://lod.nexon.com/News/hades/146547
- Official update text explicitly refers to the `평민` job/class state in current game rules.
- Use: confirms PEASANT remains a real class-state concept; it does not define old-era visual appearance by itself.
- Classification: OFFICIAL_UPDATE / PEASANT / SYSTEM_CONTINUITY / P2

### DA-MILLES-2024-001 — Milles event-map screenshot — P2
- URL: https://lod.nexon.com/community/screenshot/137555
- Title: [넥슨30주년] 밀레스마을, 감탄을 하게되네요..
- Published: 2024-04-18
- Explicitly states screenshot was taken in 밀레스마을.
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

## User-provided P0 visual observations
These are extraction rules from target screenshots supplied in the project conversation, not claims about every historical Dark Ages build.

### Outdoor target
- Terrain reads as continuous textured ground rather than a showcase grid of isolated diamond tiles.
- Grass/dirt boundaries are irregular and dense.
- Vegetation is integrated into the ground composition rather than presented as cute standalone props.
- Character occupies a small fraction of screen/world scale.

### Indoor target
- Large continuous floor plane, walls, stairs, counters/furniture and black outside-map void form one room composition.
- Interior objects share the same low-resolution texture density as the floor/walls.

### Starter peasant target
- Starter role is 평민/pre-class, not 무도가.
- Base architecture is class-neutral: BODY_BASE + hair, with equipment/class appearance layered later.
- Paper-doll order: BODY_BASE -> HAIR_STYLE -> HAIR_COLOR -> HEAD -> TOP -> BOTTOM -> GLOVES -> SHOES -> WEAPON -> OFFHAND.
- Four facings are diagonal 3/4: NW / NE / SW / SE.
- Native frame contract remains 24x32, runtime scale 1.50, foot anchor target [12,30].
- No class-specific markers such as martial-artist headband/shield may leak into the starter base.

## Production extraction contract — Gate V1
The next production asset pass MUST be derived against the P0/P1 archive above. No free-form style generation.

### World V1 deliverables
- `terrain/grass_base`: seamless continuous-ground texture vocabulary, no visible showcase-diamond border.
- `terrain/dirt_base`: old-Milles dirt/road vocabulary.
- `terrain/grass_dirt_transition`: irregular transition family; minimum N/E/S/W plus diagonal/corner coverage in runtime composition.
- `vegetation`: ground-integrated grass/flower/bush clusters matching source density.
- `architecture`: first Milles exterior assembled from wall/roof/door/window/stair vocabulary observed in references, not invented fantasy architecture.

### Character V1 deliverables
- Starter peasant/pre-class only; no martial-artist costume or shield.
- BODY_BASE must remain compatible with later paper-doll layers.
- Native 24x32 frame contract, runtime 1.50 scale.
- IDLE + WALK only.
- NW / NE / SW / SE are true diagonal 3/4 facings. No front/back substitute and no simple left/right mirror acceptance.
- Foot anchor target `[12,30]` for all frames; any exception must be measured and documented before runtime wiring.
- Four WALK frames run at 5 fps so one animation cycle matches one 0.80 sec adjacent-tile step.

### Comparison gate
Before a production peasant atlas is wired into the playable renderer, make a side-by-side composite against at least one user P0 screenshot and one official historical reference. Reject if:
- character reads as a class-specific fighter/martial artist instead of starter peasant;
- character reads chibi/front-facing;
- terrain exposes a repeated 64x32 diamond showcase pattern;
- building reads Mir2/Chinese-fantasy/generic modern pixel-art;
- vegetation looks like isolated cute props rather than part of terrain;
- object/character/world scale materially diverges from the old-Dark-Ages reference.

## Extraction queue
1. P0 old Milles map images from DA-OLDMAP-001.
2. DA-MILLES-2005-001 period Milles screenshot for scale/texture cross-check.
3. Historical screenshots containing low-level/unstyled characters suitable for peasant silhouette extraction.
4. Outdoor terrain samples: grass, dirt/path, edge transitions, vegetation clusters.
5. Milles architecture: exterior wall/roof/door/window/stair vocabulary and relative scale.
6. Indoor samples: wood/stone floor, walls, stairs, counters, furniture, map-edge void.
7. Starter peasant: silhouette, diagonal facing, head/body proportions, common foot anchor.
8. NPC/monster/item/class-specific appearances only after first playable Milles + peasant screen passes visual comparison.

## Acceptance policy
A production visual asset is rejected when it:
1. looks like Mir2 or another generic old MMORPG rather than Dark Ages;
2. changes the diagonal character-facing contract;
3. replaces original Dark Ages proportions with modern cute/high-detail pixel-art proportions;
4. uses decorative architecture/props unsupported by the reference archive;
5. is generated from text-only style prompting without a Dark Ages reference basis;
6. exposes obvious repeated 64x32 diamonds when the target reference reads as continuous terrain;
7. hard-codes a later class appearance into BODY_BASE;
8. is wired into runtime before a P0/P1 side-by-side visual comparison passes.

## Current status
- Milles v0.1: REJECTED — Mir-like visual direction.
- Milles v0.2: REJECTED — still generic modern isometric pixel-art direction.
- Previous front-facing character preview: REJECTED.
- Generic AI-generated Dark-Ages-like sheets: REJECTED for production use — insufficient reference fidelity.
- Martial artist starter contract: RETIRED.
- Starter canon is now PEASANT/pre-class with paper-doll-compatible BODY_BASE.
- Runtime no longer resolves legacy martial assets for the starter character.
- Active next gate: produce one reference-accurate 120x128 `player_peasant_idle_walk.png`, then side-by-side validate before production promotion.
