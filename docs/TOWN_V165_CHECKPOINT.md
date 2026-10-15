## Exact V165 focused verification and saved candidate

Runtime source f46260966a1592700a7343735526cf60ef344647, focused Actions38741585115/job116444384033 SUCCESS:83tests/13suites/0failure/error/skip. Reviewed32town/dialog native frames plus NPC pairs/actual scenes. NPC/player same-outfit pixels identical, scale1.7, manager visible heights51/50/49px(Milles/Abel/Rucesion). No NPC scale or tree change. Shared17service signs;6town pairs/forest/close/gutters verified.

APK165/com.projectdark.mobile.v165test built2026-10-15T22:25:09.282130+09:00,80447229bytes,SHA25658e049fc4f6b1d63ba0620035de1d15bffc6e6be2c383469367dd29b1e3b7f1a. APK artifact11975189912/native11975009837 ZIP SHA/CRC independently verified; embedded identity and89packaged town files match exact source. Truncated local APK transfer resumed from verified byte offset; final full hash/CRC PASS. APK/3review PNGs/verification saved successfully. APK libfile_fb8e636068fc81918ecca8701d226473.

Common PR38741598068/push38741585099 IN_PROGRESS; do not claim full common success yet. Physical phone/user visual PENDING. No main merge. Receipt master/changes/V165_BUILD_RUNTIME_RECEIPT.json. No additional runtime/art changes needed pending regressions and handset feedback.

## Native-size preflight correction

Initial image_gen alpha contained near-transparent perimeter noise. Naive nonzero-alpha crop shrank poles to27px. Full native preview caught this BEFORE APK delivery. Crop uses alpha>=128 bounds, preserving output interior alpha; poles now115–135px high within unchanged canvases. Native pole-height assertion remains enabled. Initial6abd0c97 candidate superseded by this correction; no redraw.

# V165 town polish checkpoint

Branch codex/rucesion-coast-v157 / PR197; base629e4f34; main41fe6def unchanged. Last exact-source full common success: V164 e834b49e /38720772628 and38720764872.

User corrected request2026-10-15 22:06 KST: STREETLAMPS, not trees. V165 changes no tree pixels. Five lamp sprites from2retained generated reference repairs preserve original native canvas/anchor; source and before bytes retained under master/source/town_v165. Source metadata reference.json.

Town travel offers Milles/Abel/Rucesion excluding current, plus Pote forest; Milles shows exactly Abel village/Rucesion village/Pote forest. Piet omitted from offered towns; legacy Piet save/manager can depart safely. Removed guide name/role, free-travel chatter and generic town descriptions; forest label 2써클 사냥터 (Lv 11~Lv 40). Shared cached service signs at existing doors in all3towns. No service inventory or navigation changes.

NPC code uses exact player CharacterRenderer at1.70scale; no arbitrary enlargement. New native test compares pixel-identical NPC/player with same outfit and foot and saves measurements/pairs plus real GameView frames. Physical user report remains PENDING until measured/runtime-reviewed.

IMPLEMENTED/STATIC_MASTER_PASS; COMPILE,NATIVE,APK PENDING. Focused TownPolishV165Test plus existing town/services/NPC/quest/save regressions; verify_v165_apk exactsource identity. Next: commit, run CI, diagnose failures without regeneration, inspect actual lamps/travel/signs and compare NPC measurements; deliver exact165APK. No main merge.

Governing inputs consumed: AGENTS.md, docs/DIRECTOR_GUIDE.md, docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md, design/DATA_CONTRACT.md, design/SOURCE_OF_TRUTH.md, current TownTravel/TownTravelWindow/GameView/TownInteriorDef and both town layouts/renderers, NpcActorRenderer/CharacterRenderer/NpcAppearanceTest, supplied feedback and Nexon-hosted lamp screenshot. Contract master/changes/TOWN_POLISH_V165.json.
