# V218 global lag investigation — 2026-10-21

User reports continuing unplayable whole-game lag, especially Mine; previously dense Rucesion coast was playable. DEVICE_FAILED_USER_REPORTED; installed exact APK and complete handset root cause UNKNOWN. Do not blame population or certify a phone fix from host measurements.

Director sole editor, existing PR197/codex/rucesion-coast-v157; base ba3f8bc010035652dd469673e6f20b32c5d2c424, main41fe6def5120b6c33b58f609e52fbaf2ead700ee unchanged.

Confirmed code defect: GameView.inventoryQuantity projected the complete inventory twice per HUD frame for two potion counts. RpgInventoryPresentation.inventoryRows creates every ItemRow, regex-normalizes names, copies job sets and checks requirements. Current sandbox contains over1500items. V212 ItemWindow cache did not cover this independent HUD path; V214 compact quest projection did not remove it. Replace only quantity lookup with the authoritative live ownership map. Preserve names/requirements on requested inventory paths, population, collision, graphics, cadence and transactional saves.

Focused checks: live quantities through loot/use/buy/restart; actual HUD with inventoryRows forbidden; paired previous two full projections vs direct lookups on identical items; real update+draw with live actors, repeated ground input, autosave and cold tails in Milles/Rucesion coast/Mine;12map trips and bounded Mine caches. Adjacent compact quest HUD, ItemWindow revision cache, autosave ownership and restart matrix because shared HUD consumes RPG ownership and saves. Narrow lookup change is not a new movement/combat rule; observed whole-game failure also triggers existing common full regression fallback (no duplicate full push run).

Main reference recovered without another upload: Screen_Recording_20261021_205405_YouTube(2).mp4,218970186bytes,97.384062seconds,2340x1080; actually decoded/reviewed overview with outdoor wooden building, wooden interiors and cave basement. Previous 'unreadable' status superseded. Exact building/map/monster reference reconciliation remains outstanding after urgent performance work. No dungeon expansion.

IMPLEMENTED: direct HUD lookup. BUILD/HOST/PHONE pending until exact-source results. Next: inspect profiles, address reproduced additional bottlenecks, verify source/build artifact identity and deliver explicitly scoped candidate, physical handset pending.
