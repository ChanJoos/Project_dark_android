## 2026-10-18 — V180 original Horror recovery IN PROGRESS, not an accepted replacement

Recovered official DarkAges741 hades.dat MPFs363–372; visually matched all10humanoid appearances in supplied Korean recording. Master order maps363/368warriors,365/370rogues,364/369mages,367/372clerics,366/371monks. Replaced substitute wardrobes with original indexed pixels, original anchors,5walk frames and2–4attack frames in four directions. HP/EXP/combat rules unchanged.

Recovered Korean20210608maps archive and retained exact original map bytes:6002lobby50x20,6003corridor10x35,6801second-floor corridor12x30,6401–6411rooms01–11. Original447foreground HPFs,190floor tiles and palette/SOTP bytes are retained offline. Original tile/object/depth/collision layers replace authored geometry for these14maps; shared32x16movement grid uses uniform8/7source scale. Floor cache32x256x256RGBA≤8,388,608bytes; no manual recycle.

Video labels independently read:49s=02,53s=08,65s=11,73s=03. The gray/red and violet grids at73/113s are TAB navigation overlays, not original flooring. Do not repeat the V179 overlay-as-floor mistake. Video113s2Fhall matches6801carpet/sofas/white ornaments;6003has5actualround doors. Second-floor hall has its observed end arch rather than invented round doors.

**12/13original map files and their exact layouts are still MISSING.** They retain isolated rejectedV179legacy code during recovery; they are NOT source-faithful and MUST NOT be delivered as a completed13-room remake. No new APK is authorized by the visual gate until the remaining source/parity is resolved. User sequential13/15actors/norespawn rule stays intact; this is a WIP branch, not an accepted final build. All other exact-original monster timing, phone FPS and source-obscured material remain unverified.

Native compilation/rendering/Horror-only navigation/combat/clear/save checks are pending. Functional checks cannot certify13-room original parity. Scoped commit opt-in `[horror-scoped]` suppresses common CI ONLY if changes since previously verifiedaf19016are confined to the explicit Horror allowlist/docs; unmarked or unlisted changes resume common CI. The focused workflow runs Horror tests only, uploads evidence, and deliberately builds/delivers no APK.

Retained sources/generator:master/source/horror_v180/{original_monsters.zip,original_maps.zip,map_sources.json};tools/build_horror_v180.py;tools/horror_original_formats.py. Latest main41fe6defunchanged; existingPR197/branch retained. Do not redownload/reidentify these sources after interruption; resume from retained bytes.

