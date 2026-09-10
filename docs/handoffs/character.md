# Character / Animation Handoff

## 2026-09-10 17:05 KST — agent/character/20260910-1705

### Priority
Continue exhaustive item-image coverage before more animation polish. Current family remains shields.

### Search result this run
- Re-searched the seven canonical shield names individually across current web/Nexon evidence pools.
- No source was found that safely ties an isolated inventory icon or equipped-world sprite to any of the seven current canonical shield IDs.
- A fan/community screenshot for `기사단 방패` was found at `https://gall.dcinside.com/mgallery/board/view/?id=darkages&no=3326`. The image visibly contains shield-related inventory/game imagery, but `기사단 방패` is not one of the seven canonical shield IDs in the current Item_Master batch.
- Therefore it is **not** promoted to any canonical shield. Current render-ready shield count remains 0.

### Completed this run
- Added `ItemVisualCandidateEvidence` as a staging layer for candidate screenshots/images discovered during research.
- Candidate rows record source URL/host, provenance, surface hint (`INVENTORY_ICON`, `EQUIPPED_WORLD`, `MIXED_SCREENSHOT`, `UNKNOWN`) and identity status (`UNMAPPED`, `FAMILY_ONLY`, `ITEM_CONFIRMED`).
- Added a hard `canPromoteToRender()` rule: an image cannot become renderer-authoritative until exact item identity is confirmed and the relevant image surface is known.
- Recorded the `기사단 방패` screenshot as `FAN + MIXED_SCREENSHOT + FAMILY_ONLY`; no canonical Item ID is assigned.
- Updated `data/design/ITEM_VISUAL_MANIFEST.md` with this candidate-evidence workflow and current shield acquisition state.

### Quality / blocker delta
This pass prevents visual-similarity contamination of the item catalog. A shield-looking image from the same game is no longer enough to bind a canonical shield ID. Exact identity remains required before `READY_FOR_RENDER` and OFF_HAND presentation.

### Current blocker
The seven canonical shields have text/name evidence but no positively identified image evidence in the searched pool. Until a source labels the visible icon/sprite or a trusted asset package is found, attaching a specific shield graphic would be invention.

### Next Character pass
1. Broaden image acquisition from shield-name pages to old Nexon guide attachments, archived item/shop screenshots, and trusted asset dumps if present in repo/source packages.
2. In parallel, start the next equipment family (common gloves/leggings/shoes) so image-catalog progress is not blocked by shields alone.
3. Promote the first exact item image to `PENDING_CROP`/`READY_FOR_RENDER` only after item identity is positively established.

### Boundaries preserved
No `GameView.java`, combat/AI/damage, world/pathfinding, RPG mutation/progression/save, HUD/input, dialogue/quest, APK packaging or Director integration changes.
