# PROJECT DARK — DEV HISTORY PASS 28 · RPG REWARD FEED

Date: 2026-09-10
Role: RPG · Progression · Persistence
Branch: `agent/rpg/20260910-1338`

## Implemented

- Direct reward attempts now preserve typed outcomes:
  `GRANTED / INVENTORY_FULL / INVALID_ITEM / INVALID_QUANTITY / UNRESOLVED_REWARD`.
- Every canonical drop hint is represented in reward history. Unresolved probability/quantity remains
  `UNRESOLVED_REWARD` and never mutates inventory.
- Added `consumeCombatWithOutcomes` so processed, duplicate/stale and ignored combat events are
  explicit while the existing RuntimeState call remains source-compatible.
- Added `RpgRewardFeedPresentation` with typed, timed UI snapshots for EXP, successful direct item
  grants and every failure/pending outcome.
- Feed synchronization is idempotent by combat sequence, preventing repeated ledger snapshots from
  displaying the same reward twice.

## Verification

- PLANNED: complete.
- IMPLEMENTED: complete on the existing RPG draft branch.
- BUILD VERIFIED: partial; deterministic Android-free audit covers direct mutation outcomes, feed
  deduplication, fade/despawn and unresolved reward visibility. Full Gradle/APK is not claimed.
- RUNTIME VERIFIED: no; Director must connect the feed snapshot to reward toast/chat presentation.

## Evidence safety

No missing probability, quantity, Gold or icon value was invented. Ground drops/pickup remain retired.
The current POTE_SPIRIT major-drop hint therefore produces an EXP line plus a visible
`UNRESOLVED_REWARD` line, not an item.
