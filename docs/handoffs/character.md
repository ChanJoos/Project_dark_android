# Character / Animation Handoff

## 2026-09-10 18:05 KST — agent/character/20260910-1805

### Priority
Continue exhaustive item-image coverage before more animation polish. Acquisition now uses a two-step model: validate canonical item names/families against historical Nexon board data, then attach fan-archive imagery only after exact item identity is confirmed.

### Source/data state
- Continued from `agent/character/20260910-1735`; previous Character visual work remains unmerged.
- Re-validated low-level equipment against Nexon historical board `https://lod.nexon.com/Community/game/6222?Category2=1&SearchBoard=1`: leather glove/legging, shoes/gray shoes, leather/copper shields and related low-level equipment are explicitly enumerated.
- Independently re-validated glove lineage through Nexon historical board sources `https://lod.nexon.com/community/game/1702?SearchBoard=1` and `https://lod.nexon.com/Community/game/1643?Category2=1&SearchBoard=1`, covering leather/copper/aqua/silver/aqua-leather glove names and level/stat lineage.
- Re-validated the surviving fan archive `https://minimob.tistory.com/99` as the current highest-value visual source candidate because it groups dozens of item images by equipment section.
- Located `https://minimob.tistory.com/125` as an additional surviving gameplay/leveling archive with many embedded screenshots and explicit equipment discussion.
- Re-validated `https://lod-ontime.tistory.com/` and `https://lod-ontime.tistory.com/15` as mirrors of 현자의마을/앨시 item-reward information, including leather glove/legging and related low-level rewards.

### Completed this run
- Added `FanItemCrossValidationCatalog` to record per-item canonical name/family evidence across historical Nexon text sources and surviving fan visual archives.
- Seeded cross-validated rows for canonical common equipment: `IT_GLOVE_LEATHER`, `IT_GLOVE_COPPER`, `IT_GLOVE_AQUA`, `IT_GLOVE_SILVER`, `IT_GLOVE_AQUALEATHER`, `IT_LEGGING_LEATHER`, `IT_SHOES`, `IT_SHOES_GRAY`, `IT_SHIELD_LEATHER`, `IT_SHIELD_COPPER`.
- Added confidence states separating `CROSS_VALIDATED_TEXT`, `IMAGE_SECTION_MATCH`, and `IMAGE_ITEM_CONFIRMED`. Text cross-validation cannot accidentally promote an image to render authority.
- Added family queries so subsequent passes can deterministically process gloves/leggings/shoes/shields instead of repeating broad searches.

### Concrete blocker / next extraction step
The current fan archive clearly contains the needed item imagery, but legacy Tistory/Daum CDN anti-hotlink behavior still prevents direct automated extraction of the staged images. Exact image-to-item identity is therefore not yet promoted beyond section-level evidence. Next pass should inspect alternate mirrors/cached renderings/screenshots of the same archive and match each image to its adjacent item label. Once exact identity is visually confirmed, promote `IMAGE_ITEM_CONFIRMED` -> `PENDING_CROP`/`READY_FOR_RENDER` and test the corresponding EQUIPMENT/OFF_HAND layer.

### Evidence discipline
- Historical Nexon boards: authoritative for item-name/family/level lineage, not automatically for pixels.
- Established fan archive: `[V]` visual evidence until exact item/image pairing is cross-checked.
- Mirror blog: source-tracing/cross-validation evidence.
- Section membership alone never authorizes canonical render binding.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
