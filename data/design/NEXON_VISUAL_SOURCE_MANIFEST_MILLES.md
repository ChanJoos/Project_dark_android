# PROJECT DARK — Nexon-hosted Visual Source Manifest · Milles

Status: ACTIVE EVIDENCE MANIFEST
Updated: 2026-09-10
Scope: World · Character / `MAP_MILLES`

## Policy

This manifest records visual evidence hosted on the official Nexon Dark Ages website. Hosting on `lod.nexon.com` or `file.nexon.com` proves source provenance, but user-posted community screenshots are not automatically promoted to official/original `[O]` art evidence. Community screenshots hosted by Nexon are recorded as `[V]` unless the image is separately verified as an official asset/screenshot.

The target reconstruction policy is:
1. collect and preserve every useful Nexon-hosted visual reference;
2. trace/reconstruct only details supported by those references and Master data;
3. never ship a whole screenshot as the final map texture;
4. decompose the reconstructed world into `TILE / OBJECT / COLLISION / NPC / MONSTER_SPAWN / PORTAL`;
5. where source coverage is genuinely absent, create replacement/fill assets as PROJECT DARK-authored material and tag them `[ADAPTED]` (or `[B]` while still prototype), never as original artwork;
6. preserve a provenance boundary so sourced, reconstructed, and authored pixels can be audited separately.

## Primary Milles source set

### NX_MILLES_OLD_TOWN_MAP_SET_20210601

- Evidence: `[V]` — Nexon-hosted community screenshot post; useful legacy visual evidence, not automatically official art.
- Page: `https://lod.nexon.com/Community/screenshot/136184?Category2=2`
- Published: `2021-06-01 13:27`
- Post title: `구 마을맵 지도 동서북 밀레스 구아벨 구뤼케시온 구루어스 구마인`
- Page explicitly contains 9 attached images.
- Master relation: `Asset_Map_Mapping.csv` → `MAP_MILLES / MAP_OLD_TOWNS / REFERENCE / SOURCE_FOUND`.
- Current identification status: `PENDING_VISUAL_IDENTIFICATION` — the exact attachment(s) corresponding to Milles must be positively identified before calibration or tracing.

Attachment endpoints exposed by the Nexon page:

| Attachment | Nexon oidFile | Direct endpoint | Status |
|---|---:|---|---|
| 01 | 5053116125680767924 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767924` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 02 | 5053116125680767925 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767925` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 03 | 5053116125680767926 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767926` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 04 | 5053116125680767927 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767927` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 05 | 5053116125680767928 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767928` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 06 | 5053116125680767929 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767929` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 07 | 5053116125680767930 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767930` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 08 | 5053116125680767931 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767931` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 09 | 5053116125680767934 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767934` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |

Important: the web retrieval environment can resolve these attachment endpoints from the Nexon page, but direct binary fetch currently returns a cache miss. This does not justify guessing image order/content.

### NX_MILLES_EVENT_MAP_20240411

- Evidence: `[V]` — Nexon-hosted community screenshot.
- Page: `https://lod.nexon.com/Community/screenshot/137421?Category2=2`
- Published: `2024-04-11 12:23`
- Post text explicitly states the screenshot was taken in a `밀레스 이벤트 맵`.
- Use: modern Milles/event-area visual vocabulary reference only.
- Geometry authority for canonical old `MAP_MILLES`: `NO` unless specific overlap is verified.
- Status: `REFERENCE_ONLY`.

## Supporting coordinate evidence

Master `Location_Points.csv` retains these Milles points:
- `LOC_MILLES_CASTLE` `(118,50)`
- `LOC_MILLES_SEA` `(118,82)`
- `LOC_MILLES_ALTAR` `(103,11)` with note `103,11 또는 104,11`
- `LOC_MILLES_ROSEMARY` `(81,43)`
- `LOC_MILLES_MODIA` `(78,28)`
- `LOC_MILLES_TELLIDOAH` `(58,105)`

These coordinates are calibration candidates only after the corresponding visual landmarks are positively identified. They must not be fitted to the current prototype screenshot by assumption.

## Reconstruction tiers

- `SOURCE_PIXEL_REFERENCE`: pixels/details directly visible in positively identified Nexon-hosted evidence.
- `TRACE_RECONSTRUCTION`: PROJECT DARK recreation traced from source evidence; provenance links back to one or more source IDs.
- `ADAPTED_FILL`: newly authored tiles/objects needed only where source coverage is absent. Must match surrounding proportions/palette/geometry vocabulary without being represented as original art.
- `PENDING_CROP`: source exists but usable sprite/tile/object crop has not been positively identified or cleared for runtime use.
- `PENDING_VISUAL_IDENTIFICATION`: attachment/source exists but its exact map/object identity is unresolved.

## Next trace gate

`MillesTraceContract` must remain `PENDING_ANCHORS` until at least one old-town-map attachment is positively identified as Milles and at least two non-collinear landmark correspondences can be justified. No affine/isometric calibration may be committed before that gate is met.
