# PROJECT DARK — DEV HISTORY PASS 20 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## User decision applied

New explicit visual reconstruction policy:
- gather as much usable visual reference as possible from the Nexon Dark Ages website;
- reconstruct/trace areas covered by source evidence;
- create PROJECT DARK-authored fill only for genuinely missing coverage;
- never represent newly authored fill as original art;
- retain provenance boundaries between source reference, traced reconstruction, and adapted fill.

This policy was promoted into `design/SOURCE_OF_TRUTH.md` Revision M001 / D003.

## Nexon-hosted Milles source research

Primary source page identified:
- `https://lod.nexon.com/Community/screenshot/136184?Category2=2`
- title: `구 마을맵 지도 동서북 밀레스 구아벨 구뤼케시온 구루어스 구마인`
- published: 2021-06-01 13:27
- the Nexon page exposes nine attached images.

All nine Nexon `oidFile` attachment endpoints were recorded in a new provenance manifest. Direct binary fetch from the current web retrieval environment returned cache misses, so attachment order/content was NOT guessed and the exact Milles attachment remains `PENDING_VISUAL_IDENTIFICATION`.

Supporting Milles event-map screenshot identified:
- `https://lod.nexon.com/Community/screenshot/137421?Category2=2`
- published: 2024-04-11 12:23
- post text explicitly identifies the screenshot as taken in a `밀레스 이벤트 맵`.
- retained as `REFERENCE_ONLY`; it is not automatically valid geometry authority for the old canonical `MAP_MILLES`.

Because these are user-posted community screenshots hosted on Nexon infrastructure, hosting provenance is verified but evidence remains `[V]` unless independently established as official/original `[O]` art.

## Implementation completed

### 1. Added Milles Nexon visual source manifest

Created:
`data/design/NEXON_VISUAL_SOURCE_MANIFEST_MILLES.md`

The manifest records:
- source page URLs and dates;
- evidence grade;
- Master mapping relation;
- all nine Nexon attachment `oidFile` identifiers and downloader endpoints;
- `PENDING_VISUAL_IDENTIFICATION` status for the exact Milles image;
- reconstruction tiers: `SOURCE_PIXEL_REFERENCE`, `TRACE_RECONSTRUCTION`, `ADAPTED_FILL`, `PENDING_CROP`, `PENDING_VISUAL_IDENTIFICATION`;
- calibration rule requiring positively identified landmarks before tile-to-screen projection is enabled.

Commit:
- `1ec287a2a6c4e7b09a3c5a8aa7643a90ab6b6b71` — `Add Nexon-hosted Milles visual source manifest`

### 2. Updated Source of Truth visual reconstruction policy

Updated:
`design/SOURCE_OF_TRUTH.md`

Revision advanced to `M001 / D003`.

Added explicit policy that:
- Nexon-hosted historical/current imagery should be actively collected;
- available source-covered areas should be reconstructed before inventing replacements;
- genuinely uncovered areas may be authored as `[ADAPTED]` / prototype `[B]` material;
- source/traced/adapted pixels remain separately auditable;
- user community screenshots on Nexon do not automatically become `[O]`;
- whole screenshots remain prohibited as final map textures;
- final world decomposition remains `TILE / OBJECT / COLLISION / NPC / MONSTER_SPAWN / PORTAL`.

Commit:
- `52b05bc263d4995a6e20075fb868245114a1c041` — `Codify Nexon-first visual reconstruction policy`

## Validation

GitHub Actions Run #157 (ID `34437123389`) for commit `52b05bc2...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

## DESIGN_CONFLICT / PENDING

No canonical Map/NPC/Location/Monster/Item/Skill/Progression IDs or values were changed.
No unknown attachment was labeled as Milles without visual verification.
No screenshot was promoted into a final map texture.
No original pixel art was fabricated.

PENDING:
- fetch/inspect the nine old-town-map attachment binaries outside the current cache-limited retrieval path;
- positively identify which attachment(s) correspond to Milles;
- mark exact visible landmarks against Master coordinate candidates;
- establish at least two independently justified, non-collinear calibration correspondences before enabling `MillesTraceContract` projection;
- trace source-covered tile/object/collision regions;
- author `[ADAPTED]` fill only for gaps that remain after source collection is exhausted.

## Result

PROJECT DARK now has a codified Nexon-first world reconstruction policy and a concrete Milles visual provenance registry. Work can proceed from a source-collection pipeline rather than treating all missing visual material as permanently blocked, while still preventing unverified screenshots or newly authored fill from being mislabeled as original canon.

## Next World · Character bottleneck

1. Continue Nexon site mining for Milles screenshots, buildings, NPC-visible scenes, entrances, water edges, floor tiles, and portals.
2. Obtain/inspect the nine old-town-map binaries and positively identify the Milles attachment(s).
3. Bind verified visual landmarks to Master location coordinates and promote `MillesTraceContract` from `PENDING_ANCHORS` only when evidence is sufficient.
4. Start `TILE -> OBJECT -> COLLISION` trace for source-covered areas, then create clearly tagged `[ADAPTED]` fill only where source coverage is absent.
