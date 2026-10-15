# V165 town polish checkpoint

Branch codex/rucesion-coast-v157 / PR197; base629e4f34; main41fe6def unchanged. Last exact-source full common success: V164 e834b49e /38720772628 and38720764872.

User corrected request2026-10-15 22:06 KST: STREETLAMPS, not trees. V165 changes no tree pixels. Five lamp sprites from2retained generated reference repairs preserve original native canvas/anchor; source and before bytes retained under master/source/town_v165. Source metadata reference.json.

Town travel offers Milles/Abel/Rucesion excluding current, plus Pote forest; Milles shows exactly Abel village/Rucesion village/Pote forest. Piet omitted from offered towns; legacy Piet save/manager can depart safely. Removed guide name/role, free-travel chatter and generic town descriptions; forest label 2써클 사냥터 (Lv 11~Lv 40). Shared cached service signs at existing doors in all3towns. No service inventory or navigation changes.

NPC code uses exact player CharacterRenderer at1.70scale; no arbitrary enlargement. New native test compares pixel-identical NPC/player with same outfit and foot and saves measurements/pairs plus real GameView frames. Physical user report remains PENDING until measured/runtime-reviewed.

IMPLEMENTED/STATIC_MASTER_PASS; COMPILE,NATIVE,APK PENDING. Focused TownPolishV165Test plus existing town/services/NPC/quest/save regressions; verify_v165_apk exactsource identity. Next: commit, run CI, diagnose failures without regeneration, inspect actual lamps/travel/signs and compare NPC measurements; deliver exact165APK. No main merge.

Governing inputs consumed: AGENTS.md, docs/DIRECTOR_GUIDE.md, docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md, design/DATA_CONTRACT.md, design/SOURCE_OF_TRUTH.md, current TownTravel/TownTravelWindow/GameView/TownInteriorDef and both town layouts/renderers, NpcActorRenderer/CharacterRenderer/NpcAppearanceTest, supplied feedback and Nexon-hosted lamp screenshot. Contract master/changes/TOWN_POLISH_V165.json.
