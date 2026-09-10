# Character / Animation Handoff

## 2026-09-10 17:35 KST — agent/character/20260910-1735

### Priority
Continue exhaustive item-image coverage before more animation polish. Acquisition scope is now broadened beyond Nexon-only pages into trusted fan archives, old blogs, cafes, and community mirrors, while preserving strict provenance.

### Source/data state
- Continued from `agent/character/20260910-1705`; previous Character visual work remains unmerged.
- Re-validated the common legacy equipment families against Nexon historical board data. `https://lod.nexon.com/community/game/7009?SearchBoard=1` lists gloves, leggings, shoes, earrings, rings and the common shield family together; `https://lod.nexon.com/community/game/1487?SearchBoard=1` independently lists the shield lineage including leather/copper/iron/silver/gold/planum/papaya.
- Located a surviving established fan archive with actual embedded item imagery: `https://minimob.tistory.com/99` (`비승급 전사의 아이템 셋팅`). The page contains dozens of item images grouped under weapon, armor, ring, glove, legging, necklace and belt sections, with item names/tables adjacent to the image groups.
- The fan page exposes direct legacy Daum/Tistory CDN image URLs, but automated fetch currently returns HTTP 403 for tested image URLs. Therefore the images are preserved as acquisition candidates rather than falsely marked extracted/verified.
- Located `https://lod-ontime.tistory.com/` as a surviving mirror/index that attributes level-reward/item material to 현자의마을 앨시. It is useful for source tracing but is not promoted to image truth by itself.

### Completed this run
- Added `FanItemVisualSourceCatalog` to rank/record fan/community source pages by trust tier, coverage family, and known embedded image count.
- Seeded the large `minimob.tistory.com/99` archive as `ESTABLISHED_FAN_ARCHIVE` with multi-family visual coverage and at least 50 embedded images visible from the page structure.
- Extended `ItemVisualCandidateEvidence` with `SECTION_IMAGE` / `SECTION_MATCH` states so images found inside a known glove/legging/armor section can be retained without prematurely assigning a canonical Item ID.
- Staged four concrete surviving Daum/Tistory CDN image URLs from the archive: two glove-section images, one legging-section image, and one armor-section image.
- Kept `canPromoteToRender()` strict: section membership alone is not enough for canonical binding. Exact item identity must still be confirmed before inventory/equipped rendering.

### Concrete blocker / next extraction step
The first high-value fan archive is found and contains real item images, but its legacy CDN rejects automated image fetches with HTTP 403. The next pass should use alternate surviving mirrors, cached copies, page screenshots, or browser/manual extraction paths to visually inspect each staged image and match it to the adjacent item labels. Once an image can be positively tied to an Item ID, promote it to `ITEM_CONFIRMED` -> `PENDING_CROP`/`READY_FOR_RENDER` and test the corresponding EQUIPMENT/OFF_HAND layer.

### Evidence discipline
- Nexon historical boards: authoritative for item-name/family cross-reference, not necessarily original image pixels.
- Established fan archive: `[V]` visual evidence until cross-checked.
- Mirror blog: source-tracing evidence only.
- CDN image URL without confirmed item label: `SECTION_MATCH`, never canonical render truth.
- No visual-similarity-only binding.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
