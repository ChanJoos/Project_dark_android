## 2026-10-21 — V208 recording recovered; missing-video blocker resolved

Resumed from source checkpoint c506afd7359e54b04db8428a0ce554063d470a84 on codex/rucesion-coast-v157 / PR197. The existing upload copy Screen_Recording_20261020_221456_YouTube(3).mp4 was successfully materialized and decoded locally. No further user upload is needed for this reference.

Verified media: 325003721 bytes, SHA256 9bca1e3f0cb9ed6f03f378f8d249f065f7e9873c7a403d0f00ee1ee26c5895d8, H264 2340x1080, duration 127.974271 seconds. Reviewed nine temporal frames across the recording and a full-resolution frame at 100 seconds: visible title 카스마늄제1·3갱도, X78/Y82, blue armored axe-wielding actor, other humanoids, stone floor, timber-supported walls, torch and ore-cart motifs. Visual forms do not yet establish all canonical species/stat/equipment identities. The prior missing-video statements below are historical and superseded.

User also supplied https://www.youtube.com/watch?v=1SYWIXiesug, titled [어둠의전설] 드랍율 헬! 헬옷먹으러 가본 카스마늄 3갱도, 15:32. Browser page/title/duration loaded; playback remained black at 0:00, so do not claim its live video was watched. Analysis uses the recovered uploaded recording.

Additional source recovery: verified the existing official DarkAges741 installer SHA256 dbbd937d4dd1eed310b7b1de4c5c3d98e829ed77e5493b0857063528a47e1093 and unpacked its original DAT archives without running the installer. Recovered the previously referenced original map archive (2478 map entries). Generated internal MPF contact sheets to identify source-backed candidates; Korean palette/identity equivalence and exact map-file selection are still unverified. These bytes are source research, not production admission.

Runtime remains unchanged from the last V207 verified source b75b417598201b5c94588626bc02ab9b753555b3. No new mine hunting map, NPC destination, monster catalog, runtime atlas binding, reward table or APK is complete. Next: correlate recorded forms with exact MPFs and Korean colors, match recorded topology to the original map cells, then implement independent source-backed geometry/art and shared combat/reward/save paths. Unresolved HP/magic/drop facts must remain explicit; player-HP Quick-dungeon brackets are not monster HP.

Validation of this recovery: ffprobe metadata, complete media SHA256, successful temporal/full-resolution frame decode and direct visual inspection. No Android build, gameplay or original-map parity verification is claimed by this documentation commit.

---

## 2026-10-21 V208 Mine hunting — source stage only, implementation blocked by missing recording

User requests square original mine accessed from Mine travel NPC, all recorded original monsters and native atlases, per-species combat, worn equipment direct-to-inventory drops, promoted-player highest mobile XP/Gold, and map parity verification. Armor photo readable: five rows cross-referenced in master/source/mine_hunt_v208/catalog.json. Negative HP/MP/core stats preserved; thief AC -57 comes from existing Master and community corroboration, not the photo. Sex-independent stats are user-confirmed. Three existing V184 armor registrations remain; two unresolved appearances are not falsely admitted.

Requested recording Screen_Recording_20261020_221456_YouTube.mp4 is absent locally; other retained town/Horror videos are not substitutes. Map dimensions/tiles/collision, complete roster, original monster atlas bindings, combat/magic/reward/drop odds remain unresolved. Official 2004 tunnel notice and dated fan sources are distinguished; Quick dungeon player HP brackets/rewards must not be used as monster HP/original-mine XP. No hunting map/NPC entry/runtime stats or new APK delivered for V208. Current last verified runtime remains V207 b75b4175. Resume with reattached mine video (or accessible original URL), identify exact mine/version and all visible actors, recover original assets then wire shared combat/reward/save and verify real NPC roundtrip, defeat/inventory/restart, caches and source-map comparison. Source validator checks row mapping, signs, provenance and no premature runtime admission; it does not verify game runtime or map fidelity. See master/changes/V208_MINE_HUNT_SOURCE.json.

