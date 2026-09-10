# Character / Animation Handoff

## 2026-09-10 18:25 KST — agent/character/20260910-1825

### Priority
Continue exhaustive item-image recovery before further animation polish. Current goal is exact image-to-canonical-item pairing, not more renderer micro-detail.

### Source/data state
- Continued from `agent/character/20260910-1805`; prior Character visual work remains unmerged.
- Historical Nexon sources continue to validate the canonical item families. `https://lod.nexon.com/Community/game/6222?Category2=1&SearchBoard=1` validates low-level glove/legging/shoe/shield progression; `https://lod.nexon.com/Community/game/1643?Category2=1&SearchBoard=1` validates glove lineage; `https://lod.nexon.com/community/game/1487?SearchBoard=1` validates the shield lineage through papaya shield.
- Nexon level-reward posts independently validate higher rows such as aqua glove, silver shield, silver/gold shield, aqua-leather glove, gold-aqua ring, planum shield and magma boots.
- `https://minimob.tistory.com/99` remains the highest-value surviving fan visual archive. The parsed page still exposes section image links and adjacent item tables for rings/gloves/leggings/etc.
- Direct legacy image links such as `https://t1.daumcdn.net/cfile/tistory/1409EC385046382A0D` remain HTTP 403 through automated retrieval. Exact binary inspection is therefore still blocked.

### Completed this run
- Added `LegacyItemImageRecoveryCatalog` so page-level proof that item imagery exists is tracked separately from actual recovered image binaries.
- Expanded `FanItemCrossValidationCatalog` to 17 canonical items across gloves, leggings, shoes, shields and ring families.
- Promoted glove-family rows and `IT_RING_GOLDAQUA` to `IMAGE_SECTION_MATCH`: surviving fan pages contain images in the same labelled item section, while exact per-image identity is still intentionally unclaimed.
- Added independent Nexon reward references for `IT_GLOVE_AQUA`, `IT_GLOVE_SILVER`, `IT_GLOVE_AQUALEATHER`, `IT_SHIELD_SILVER`, `IT_SHIELD_GOLD`, `IT_SHIELD_PLANUM`, `IT_BOOTS_MAGMA`, and `IT_RING_GOLDAQUA`.
- Added deterministic `visualSectionMatches()` query for the next extraction pass.

### Concrete blocker
No exact image binary could be recovered from the legacy Tistory/Daum CDN in this pass. Search/image-query results confirm surviving Legend of Darkness imagery elsewhere, but none safely isolates one of the current canonical low-level equipment items with both visible pixels and exact label. Therefore `IMAGE_ITEM_CONFIRMED` remains 0 and no fake EQUIPMENT/OFF_HAND sprite was rendered.

### Next Character pass
1. Search cached/reposted copies of the exact legacy CDN hashes and the same minimob article sections.
2. Search by higher-signal canonical item names such as `골드아쿠아링`, `마그마부츠`, `아쿠아글러브`, `은제방패` where surviving Nexon reward posts give strong textual anchors.
3. The first visually labelled match should be promoted to `IMAGE_ITEM_CONFIRMED`, then cropped/bound to `ItemVisualManifest` and renderer layer immediately.

### Evidence discipline
- Nexon historical pages: canonical name/family/reward lineage evidence.
- Fan page section with images + adjacent labels: `[V] IMAGE_SECTION_MATCH` only.
- Exact image binary blocked/unseen: never `IMAGE_ITEM_CONFIRMED`.
- No visual-similarity-only binding.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
