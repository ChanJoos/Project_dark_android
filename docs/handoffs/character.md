# Character / Animation Handoff

## 2026-09-10 19:05 KST — agent/character/20260910-1905

### Priority
Continue exhaustive item-image recovery before further animation polish, but stop repeating acquisition lanes that are demonstrably blocked. Current rule: one strong retry of a blocked archive path, then pivot to the next surviving evidence pool.

### Source/data state
- Continued from `agent/character/20260910-1850`; previous Character visual work remains unmerged.
- Re-ran exact-hash searches for representative legacy Tistory/Daum CDN IDs (`1409EC385046382A0D`, `140CA4385046382B08`, `1243DD3C5055B3501F`, `2065923C505599F426`) and searched high-signal canonical names (`골드아쿠아링`, `마그마부츠`, `아쿠아글러브`, `은제방패`).
- Exact hash searches again produced no usable mirrored binary.
- `https://minimob.tistory.com/128` gives a useful positional clue: the article text discusses `골드아쿠아링` at level 81 and exposes an adjacent Tistory image link (`https://t1.daumcdn.net/cfile/tistory/13637C4250EA87772F`). Direct fetch of that exact image still returns HTTP 403, so even this stronger adjacency is not enough to claim the binary contents.
- Historical Nexon reward pages remain strong canonical text anchors: `https://lod.nexon.com/community/game/7536?SearchBoard=1` ties 은제방패/금제방패/아쿠아각반/아쿠아레더글러브/골드아쿠아링/플라늄방패/마그마부츠 to specific reward levels; `https://lod.nexon.com/community/game/7539?SearchBoard=1` independently ties 아쿠아글러브/은제방패/금제방패/아쿠아각반/아쿠아레더글러브/골드아쿠아링/플라늄방패.

### Completed this run
- Added `ItemVisualAcquisitionDecision` to prevent repeated Character passes from burning time on the same blocked legacy CDN hashes.
- Marked `LEGACY_TISTORY_HASH_RECOVERY` as `EXHAUSTED_FOR_NOW` with the exact blocked hashes preserved for future retry only if a new mirror/cache source appears.
- Activated `NEXT_SURVIVING_ARCHIVE_PASS` with canonical targets `IT_RING_GOLDAQUA`, `IT_BOOTS_MAGMA`, `IT_GLOVE_AQUA`, `IT_SHIELD_SILVER`, `IT_LEGGING_AQUA`.
- Preserved the hard rule that unseen binaries cannot become `IMAGE_ITEM_CONFIRMED` and therefore cannot drive canonical equipped rendering.

### Concrete blocker
The Tistory/Daum legacy path is now conclusively blocked for this toolchain: exact source URLs are known, direct fetch is HTTP 403, and exact-hash searches found no mirrors. `IMAGE_ITEM_CONFIRMED` remains 0 on this lane. Repeating the same hash search is no longer productive.

### Pivot / next Character pass
1. Do **not** retry the exhausted hashes unless a newly discovered mirror/cache explicitly references them.
2. Move immediately to other surviving fan archives, reposted screenshots, video frames, cafe/blog mirrors, and Nexon community screenshots for the five active high-signal targets.
3. Prefer sources where the item name is visible in the same screenshot as the icon/equipped character, so exact identity can be established without relying on page order.
4. The first visually labelled match should be promoted to `IMAGE_ITEM_CONFIRMED`, then cropped/anchored and bound to the appropriate `EQUIPMENT` or `OFF_HAND` presentation layer in the same run.

### Evidence discipline
- Nexon historical/reward pages: canonical item-name/family/level evidence.
- Fan image adjacent to matching text but binary unseen: `IMAGE_SECTION_MATCH`, not exact confirmation.
- Exact item name visible in same inspectable image: eligible for `IMAGE_ITEM_CONFIRMED` after cross-check.
- No visual-similarity-only binding.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
