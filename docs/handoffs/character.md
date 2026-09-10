# Character / Animation Handoff

## 2026-09-10 18:50 KST — agent/character/20260910-1850

### Priority
Continue exhaustive item-image recovery before further animation polish. Current goal is exact image-to-canonical-item pairing, then immediate renderer binding.

### Source/data state
- Continued from `agent/character/20260910-1825`; prior Character visual work remains unmerged.
- `https://minimob.tistory.com/99` remains the highest-value surviving visual archive. Its page parser exposes the original legacy Tistory/Daum image links by section.
- This pass recovered and indexed **18 concrete legacy image URLs** from that page: 8 ring-section images, 4 glove-section images and 6 legging-section images.
- Direct fetch of every tested legacy CDN URL still returns HTTP 403. The URLs survive, but their binaries cannot currently be inspected through the automated retrieval path.
- A stronger current Nexon canonical text source was found at `https://cs.nexon.com/HelpBoard/popuphelpview/24025`. It independently lists reward rows including aqua glove, iron/silver/gold/planum shields, aqua leggings, aqua-leather glove, gold-aqua ring and magma boots.
- Historical Nexon board sources continue to independently validate shield progression and low-level equipment families.

### Completed this run
- Added `LegacyArchiveSectionImageCatalog` with the 18 recovered image URLs and deterministic section/ordinal metadata.
- Explicitly models `URL_RECOVERED_BINARY_BLOCKED -> BINARY_RECOVERED -> ITEM_CONFIRMED`; URL survival alone cannot authorize pixels.
- Strengthened `FanItemCrossValidationCatalog` with the current Nexon customer-support reward table as a primary canonical-name cross-check for high-signal equipment.
- Added `IT_LEGGING_AQUA` to the visual-section backlog and retained `IT_GLOVE_AQUA`, `IT_GLOVE_SILVER`, `IT_GLOVE_AQUALEATHER`, and `IT_RING_GOLDAQUA` as `IMAGE_SECTION_MATCH` targets.
- Exact image-hash searches were also attempted for the recovered CDN hashes; no cached/reposted binary match was returned in this pass.

### Concrete blocker
The source page and exact legacy image URLs are now known, but the CDN refuses automated binary retrieval (HTTP 403) and exact hash searches did not surface a mirror. Therefore `IMAGE_ITEM_CONFIRMED` remains 0. Promoting any ring/glove/legging image by ordinal alone would be speculative because each section contains more images than the adjacent named table rows.

### Next Character pass
1. Search alternate mirrors/caches/reposts using the recovered 18 CDN hashes and source-page title.
2. Search visual reposts for the highest-signal canonical names: `골드아쿠아링`, `아쿠아글러브`, `아쿠아레더글러브`, `은제방패`, `마그마부츠`.
3. If one binary becomes visually inspectable and its label can be tied to a canonical item, promote immediately to `IMAGE_ITEM_CONFIRMED`, record crop/anchor metadata, then bind EQUIPMENT/OFF_HAND presentation.
4. Do not infer image identity from source-page order alone.

### Evidence discipline
- Current Nexon support/reward pages: strong canonical item-name/reward evidence.
- Historical Nexon boards: item-family/level lineage evidence.
- Fan section page + recovered CDN URL: `[V] IMAGE_SECTION_MATCH` only.
- Unseen binary: never `IMAGE_ITEM_CONFIRMED`.
- No visual-similarity-only binding.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
