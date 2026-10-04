# V100 구 밀레스 배치와 전체 오브젝트 정리

User requests old Milles reference research plus all-district overlap/ground/depth repairs. Base: V99 closure26cc6090, active branchcodex/town-interiors-mobile-v86; mainbfd668d4 remains unchanged.

Evidence: supplied20.8sec original footage and visually inspected original in-game maps for Milles/north/east/west from Nexon-hosted community post136184 (2021-06-01). This is U evidence, not an official coordinate certification. New compact arrangement is ADAPTED: it does not reproduce every original district, river, sanctuary or building. Existing approved building artwork, five service entrances, interiors, actor appearance/movement, NPC/quest/combat/economy/save authority retained. All prior sprite image bytes remain unchanged.

Implementation:
- Original-like central grass island with six source benches, three rope lamps and leafy trees. Roads bypass the fountain instead of converging under it. Their source is the runtime PATHS; generated verge vegetation now consumes exactly those paths, including door branches.
- Remove stacked lamp pairs, bench/shrub pairs, extra market/arch clutter. Transparent bench footer uses measured alpha-foot registration. One consistent deciduous palette; orchard shifts as a whole and south flower facade moves48px to protect garden/roof separation.
- Tree beds and pond are ground layers; explicit ground_z puts the new independent transparent wooden bridge over water. Both door activation tiles remain intact; only one entrance marker is visible so the second does not paint over adjacent barrels or shop goods. Door markers keep their measured screen position and sort immediately above their owning facade. Standing scene objects, individual NPCs, monsters and interpolated player share stable foot-Y depth merging with standing objects pre-sorted once; source artwork untouched.
- Generated MillesSceneryFootprints binds real trunk/bench/fountain/well contacts to placements. Water tiles block standing except the SW↔NE bridge corridor. Existing buildings and30 fence segments remain authoritative. WorldDef consumes these contacts; no domain logic copied into the renderer.

Acceptance: deterministic asset/placement/contact audit, native depth pixel comparison, actual continuous navigation through all five service entrances and districts, bridge/water tests,12 full native district/actor frames plus wide viewport, existing town/interior/quest/world/skill/combat/save regressions. Exact source CI and APK proof follow execution. Physical phone and user visual acceptance pending.

New asset: structures/bridges/OBJ_bridge_milles_v100.png. Built-in ImageGen, project reconstruction, genuine RGBA alpha; one isolated brown rustic wooden rope bridge in 2:1 SW↔NE orthographic pixel art, thick corner posts and open ends, no environment/UI/text. Original prompt/source hashes and asset QA: verification/MILLES_V100_SOURCES.json. No new original mechanic or original pixel-identical certification.
