# Character / Animation Handoff

## 2026-09-10 16:50 KST — agent/character/20260910-1650

### Priority
Per user direction, Character work remains focused on complete item-image coverage before additional micro-animation work. Current acquisition family: shields.

### Source/data state
- Continued from `agent/character/20260910-1635`; prior Character work remains unmerged.
- Canonical shield IDs remain: `IT_SHIELD_LEATHER`, `IT_SHIELD_COPPER`, `IT_SHIELD_IRON`, `IT_SHIELD_SILVER`, `IT_SHIELD_GOLD`, `IT_SHIELD_PLANUM`, `IT_SHIELD_PAPAYA`.
- Official Nexon probability page `https://lod.nexon.com/cashshop/probability` explicitly names `가죽방패`.
- Nexon-hosted game-board post `https://lod.nexon.com/community/game/7009?SearchBoard=1` explicitly enumerates the common shield family `가죽방패/구리방패/철방패/은제방패/금제방패/플라늄방패`; this is Nexon-hosted community evidence `[V]`, not automatically `[O]` official-art evidence.
- Nexon-hosted Papaya quest guide `https://lod.nexon.com/community/game/791?SearchBoard=1` explicitly names `파파야방패` as the quest reward.
- The current search pass did **not** produce a positively identifiable isolated inventory icon or equipped-world sprite for any of the seven shields. Therefore none is promoted to image `SOURCE_FOUND`, `PENDING_CROP`, or `READY_FOR_RENDER` yet.

### Completed this run
- Added `ShieldVisualEvidenceCatalog` for the seven canonical shield IDs.
- Separated text/item-existence evidence from image evidence in code. A textual source can prove an item name without falsely making that item render-ready.
- `ItemVisualManifest.Entry` now exposes `textEvidenceUrl`, `textEvidenceKind`, `imageSourceUrl`, and `imageProvenance` independently.
- Added `readyForEquippedRender()` gating: an equipped sprite is only renderable when status is `READY_FOR_RENDER` and a positively identified image source exists.
- Added `pendingImageAcquisition()` and `readyForEquippedRender()` queries so subsequent Character passes can work the backlog deterministically.
- Seeded all seven shield evidence rows. Current verified shield-image count is intentionally 0 rather than inventing procedural/original-looking shield art.

### User-visible / quality delta
This pass prevents a major asset-integrity bug: item names or inventory evidence can no longer accidentally authorize an arbitrary world-character shield sprite. The renderer pipeline now has a hard gate requiring verified image evidence before a canonical shield is visually equipped.

### Evidence discipline
- `가죽방패` official probability-page name evidence: `[O]` text only.
- Shield list and Papaya quest posts: Nexon-hosted community evidence `[V]` text only.
- Isolated shield inventory icons: unresolved.
- Equipped-world shield sprites: unresolved.
- No unrelated web image or visually similar shield was accepted as Legend of Darkness evidence.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.

### Next Character pass
1. Continue shield-image acquisition using Nexon-hosted guide/community attachments and old board captures; inspect attachments rather than relying only on page text search.
2. Promote the first shield only after item identity can be visually tied to a canonical name/ID.
3. Once one shield has a verified equipped-world source, crop it by direction, define foot/hand anchor offsets, set `READY_FOR_RENDER`, and wire the OFF_HAND renderer preview.
4. Do not use generic shield drawings as canonical item art.
