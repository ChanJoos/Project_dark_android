# Character / Animation Handoff

## 2026-09-10 16:35 KST — agent/character/20260910-1635

### Priority change
Per user direction, stop spending Character passes on micro-animation polish. Build complete canonical item-image coverage first, then equip/render items family-by-family on the character.

### Source/data state
- Continued from `agent/character/20260910-1620`; previous Character visual work remains unmerged.
- Re-read `master/data/Item_Master.csv`. Canonical rows expose Item ID, name, category/subcategory, job, circle, level, equip slot, Source_ID and confidence.
- Confirmed examples across the equipment backlog include gloves, leggings, shoes, earrings, rings, shields, necklaces and belts. Shield family includes `IT_SHIELD_LEATHER`, `IT_SHIELD_COPPER`, `IT_SHIELD_IRON`, `IT_SHIELD_SILVER`, `IT_SHIELD_GOLD`, `IT_SHIELD_PLANUM`, `IT_SHIELD_PAPAYA`.
- Official Nexon right-menu guide `https://lod.nexon.com/info/guide/82295` proves inventory equipment filtering and equipped-item display in character info, but does not positively map each visible pixel/icon to canonical Item IDs.
- Official Nexon probability/manufacturing page `https://lod.nexon.com/cashshop/probability` gives text-level existence evidence for named items such as `가죽방패`; text existence is not image evidence.

### Completed this run
- Added `ItemVisualManifest` as a Character-owned, read-only projection over **all** RPG runtime item definitions. New Item_Master rows therefore automatically enter the visual backlog instead of requiring a manually maintained hard-coded list.
- Added independent visual status for inventory icon vs equipped-world sprite: `PENDING_SOURCE`, `SOURCE_FOUND`, `PENDING_IDENTIFICATION`, `PENDING_CROP`, `READY_FOR_RENDER`, `NO_SOURCE_FOUND`.
- Added presentation-only render-layer mapping for weapon/shield/equipment/accessory candidates without redefining RPG equip semantics.
- Added `data/design/ITEM_VISUAL_MANIFEST.md` as the canonical visual collection policy and evidence registry seed.
- First collection order is now shields -> common gloves/leggings/shoes -> weapons by job -> armor/clothing -> accessories.

### Important visual rule
Inventory icon and equipped sprite are different evidence surfaces. Finding an item icon does not authorize using it as the world-character sprite. Accessories are only `characterVisibleCandidate` until visibility is positively proven.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.

### Next Character pass
Start the shield family image collection. For each canonical shield, locate official/Nexon-hosted visual evidence, record provenance and image URL, identify inventory icon vs equipped sprite separately, crop/anchor only when positively identified, then promote one shield at a time to `READY_FOR_RENDER` and test it through the existing character equipment binding.
