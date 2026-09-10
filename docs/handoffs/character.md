# Character / Animation Handoff

## 2026-09-10 15:23 KST — agent/character/20260910-1523

### Priority
Continue exhaustive item-image coverage before more animation polish. User clarified that the official Legend of Darkness homepage contains the item lists/images we should use as the primary acquisition pool.

### Source/data state
- Continued from `agent/character/20260910-1705`; prior Character work remains unmerged.
- Re-checked the official homepage and public sitemap. Public navigation exposes Game Info/Guide, News/Update, Cashshop and community surfaces, but a separately indexed normal-item DB route is not visible in the public sitemap.
- Official probability/manufacturing page `https://lod.nexon.com/cashshop/probability` provides large structured official item-name coverage, including canonical legacy items such as `가죽방패` and many equipment families.
- Official/legacy game-board pages also preserve canonical equipment lists such as `가죽방패/구리방패/철방패/은제방패/금제방패/플라늄방패/파파야방패`; these remain text evidence unless a visible image can be tied to the exact item.

### Completed this run
- Added `OfficialItemHomepageSourceCatalog` as the Character-owned provenance seed for crawling official LOD/Nexon pages and Nexon-hosted images.
- Encoded a strict rule that official text evidence alone cannot authorize rendering; an actual identified image source is still required.
- Reduced NPC presentation scale from `0.88` to `0.78` and shadow scale from `0.56` to `0.50`.
- Reduced Monster presentation scale from `0.86` to `0.76`, shadow scale from `0.60` to `0.52`, and tightened monster selection/telegraph/HP/damage chrome to match the smaller body footprint.
- Logical/world coordinates remain untouched; only presentation transforms/chrome changed.

### User-visible delta
NPCs and monsters occupy materially less screen space while retaining the same logical foot anchors. Monster HP/selection/telegraph/damage presentation now scales down with the body instead of visually remaining oversized.

### Item-image acquisition next
1. Crawl official LOD guide/news/cashshop pages and Nexon-hosted attachments rather than generic web image search.
2. Build `official item name -> image URL -> surface type -> canonical Item_ID` mappings in `ItemVisualManifest`.
3. Start with shields plus common gloves/leggings/shoes in parallel.
4. Promote only exact, visually identified assets to `PENDING_CROP`/`READY_FOR_RENDER`.
5. Once the first exact equipped-world asset is found, wire it through existing renderer layer binding without touching `GameView.java`.

### Evidence discipline
- Official homepage/sitemap/probability pages: `[O]` for source/navigation/item-name evidence.
- Nexon-hosted community pages: `[V]` unless official authorship is independently established.
- Exact item icons/equipped-world sprites: still unresolved for the current seven shield IDs.
- No generic or visually similar item art is promoted to canonical render truth.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
