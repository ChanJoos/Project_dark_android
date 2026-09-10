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
| 08 | 505311612568076792? | invalid-placeholder | RETIRED TYPO — do not use |
| 08 | 5053116125680767931 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767931` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |
| 09 | 5053116125680767934 | `https://file.nexon.com/NxFile/download/FileDownloader.aspx?oidFile=5053116125680767934` | SOURCE_LOCATED / CONTENT_FETCH_PENDING |

The retired typo row above is retained only to make accidental use detectable; the valid attachment set is the nine numeric oidFile rows 7924/25/26/27/28/29/30/31/34.

Important: the web retrieval environment resolves the attachment endpoints from the Nexon page, but direct binary fetch still returns cache misses. This does not justify guessing image order/content.

## Additional positively labelled Milles scene sources

These sources materially reduce the visual search space because the post text/title itself identifies Milles or a Milles landmark. They remain `[V]` when community-posted.

### NX_MILLES_LEGACY_HOMELESS_20051126
- Evidence: `[V]`.
- Page: `https://lod.nexon.com/Community/screenshot/125266?Category2=2`
- Published: `2005-11-26 21:49`.
- Title: `밀레스의 노숙자들`.
- Post body explicitly says the scene is in Milles.
- Use: high-value legacy Milles visual reference; candidate for old-map building/floor/object vocabulary.
- Geometry status: `REFERENCE_ONLY` until image pixels and landmarks are inspected.

### NX_MILLES_GILLENOA_ROSEMARY_20040527
- Evidence: `[V]`.
- Page: `https://lod.nexon.com/Community/screenshot/121256?Category2=2`
- Published: `2004-05-27 03:10`.
- Title: `헉...길레노아 실종사건`.
- Body explicitly describes `그대 가게 앞에 있는 로즈마리` and `앞마당 로즈마리`, giving a historical spatial/semantic relationship between Gillenoa's shop and Rosemary in the Milles context.
- Master cross-check candidates: `NPC_GILLENOA_MILLES`, `NPC_ROSEMARY`, `LOC_MILLES_ROSEMARY (81,43)`.
- Important: prose establishes a relative landmark relationship, NOT an exact tile coordinate or transform anchor by itself.
- Status: `LANDMARK_RELATION_EVIDENCE`; pixel inspection pending.

### NX_MILLES_STATUE_20220427
- Evidence: `[V]`.
- Listing page: `https://lod.nexon.com/Community/screenshot?Category2=2&Page=24`
- Published: `2022-04-27`.
- Listing title: `[세오200년] 밀레스 조각상과 함께 기원`.
- Use: labelled Milles statue/landmark candidate and visual vocabulary reference.
- Exact post URL/image binary: `PENDING_SOURCE_RESOLUTION`.

### NX_MILLES_30TH_SCENE_20240418
- Evidence: `[V]`.
- Page: `https://lod.nexon.com/community/screenshot/137555`
- Published: `2024-04-18 10:15`.
- Title: `[넥슨30주년] 밀레스마을, 감탄을 하게되네요..`.
- Body explicitly states the shot is in Milles village.
- Use: modern Milles scene reference; compare persistent terrain/building vocabulary against legacy evidence before treating any feature as old-map geometry.
- Status: `REFERENCE_ONLY / CONTENT_FETCH_PENDING`.

### NX_MILLES_PARK_30TH_20240412
- Evidence: `[V]`.
- Page: `https://lod.nexon.com/Community/screenshot/137431?Category2=2&Page=27`
- Published: `2024-04-12 21:06`.
- Body explicitly says `촬영장소: 밀레스공원`.
- Use: Milles Park/event visual reference only; not canonical old `MAP_MILLES` geometry.
- Status: `EVENT_REFERENCE_ONLY`.

### NX_MILLES_PARK_30TH_20240322
- Evidence: `[V]`.
- Page: `https://lod.nexon.com/Community/screenshot/137372?Category2=2&Page=27`
- Published: `2024-03-22 19:38`.
- Body explicitly says `촬영장소 : [넥슨30주년]밀레스공원` and references `LOD #01 Mileth.wav`.
- Use: event-area reference only; soundtrack mention is not geometry evidence.
- Status: `EVENT_REFERENCE_ONLY`.

### NX_MILLES_30TH_OFFICIAL_EVENT_20240321
- Evidence: `[O]` for the event notice facts and images published by the official update/event channel.
- Page: `https://lod.nexon.com/news/event/82182`
- Published/event start: `2024-03-21`.
- Official text states a portal at `밀레스마을(61,119)` leads to `밀레스공원` and includes two images around that instruction.
- Canonical old-map authority: limited. This is strong official evidence for a 2024 Milles coordinate/portal fact, but must not silently overwrite older Master location rows or establish the old-town-map transform.
- Status: `OFFICIAL_MODERN_COORDINATE_REFERENCE`.

### NX_MILLES_EVENT_MAP_20240411
- Evidence: `[V]` — Nexon-hosted community screenshot.
- Page: `https://lod.nexon.com/Community/screenshot/137421?Category2=2`
- Published: `2024-04-11 12:23`.
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

Additional official modern coordinate evidence:
- 2024 official event notice: `밀레스마을(61,119)` portal → `밀레스공원`.

These coordinates belong to evidence/version contexts. They are calibration candidates only after corresponding visual landmarks are positively identified. A modern event coordinate must not be mixed into the legacy old-town-map calibration without evidence that the coordinate system/map revision is compatible.

## Calibration candidate registry

| Candidate | Evidence | Master/official coordinate | Visual source | Calibration eligibility |
|---|---|---|---|---|
| Rosemary | Master + `[V]` historical prose | `(81,43)` | 2004 Gillenoa/Rosemary source | `PENDING_PIXEL_IDENTIFICATION` |
| Milles Castle | Master | `(118,50)` | old-town 9-image set | `PENDING_ATTACHMENT_IDENTIFICATION` |
| Sea dungeon | Master | `(118,82)` | old-town 9-image set | `PENDING_ATTACHMENT_IDENTIFICATION` |
| Altar | Master | `(103,11)` / note 103 or 104 | old-town 9-image set | `BLOCKED_COORDINATE_AMBIGUITY` |
| Modia | Master | `(78,28)` | old-town/legacy scene search | `PENDING_VISUAL_SOURCE` |
| Tellidoah | Master | `(58,105)` | old-town/legacy scene search | `PENDING_VISUAL_SOURCE` |
| 30th park portal | `[O]` 2024 official event | `(61,119)` | official event page | `VERSION_SCOPED_REFERENCE_ONLY` |

No row is currently `CALIBRATION_READY`.

## Reconstruction tiers

- `SOURCE_PIXEL_REFERENCE`: pixels/details directly visible in positively identified Nexon-hosted evidence.
- `TRACE_RECONSTRUCTION`: PROJECT DARK recreation traced from source evidence; provenance links back to one or more source IDs.
- `ADAPTED_FILL`: newly authored tiles/objects needed only where source coverage is absent. Must match surrounding proportions/palette/geometry vocabulary without being represented as original art.
- `PENDING_CROP`: source exists but usable sprite/tile/object crop has not been positively identified or cleared for runtime use.
- `PENDING_VISUAL_IDENTIFICATION`: attachment/source exists but its exact map/object identity is unresolved.

## Next trace gate

`MillesTraceContract` must remain `PENDING_ANCHORS` until at least one old-town-map attachment is positively identified as Milles and at least two independently justified landmark correspondences are available for the intended projection model. No affine/isometric calibration may be committed before that gate is met. Modern/event-map coordinates and legacy old-map coordinates must remain version-scoped until compatibility is verified.
