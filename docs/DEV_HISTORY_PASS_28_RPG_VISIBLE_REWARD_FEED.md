# PROJECT DARK — DEV HISTORY PASS 28 · RPG VISIBLE REWARD FEED

Date: 2026-09-10
Role: RPG · Progression · Persistence
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`

## Goal

Make direct monster reward outcomes consumable by the live UI without touching Integrator-owned `GameView.java`.

## Existing branch contract confirmed

The current RPG branch already had detailed direct-grant outcomes:
- `GRANTED`
- `INVENTORY_FULL`
- `INVALID_ITEM`
- `INVALID_QUANTITY`
- `UNRESOLVED_REWARD`

`RewardResolution` already carried `grantOutcomes`, and `RpgInventoryPresentation.RewardNotice` already projected them. The missing layer was the visible RPG feed, which still only surfaced successful aggregate auto-loot.

## Implementation

### Structured reward feed

`RpgVisibleProgressionPresentation` now exposes `RewardLineKind` and structured reward-line fields so UI code never has to parse display text.

Kinds:
- `EXP`
- `ITEM_GRANTED`
- `INVENTORY_FULL`
- `INVALID_ITEM`
- `INVALID_QUANTITY`
- `UNRESOLVED`
- `INFO`

Each row also carries `itemId`, `quantity`, `grantStatus`, and `evidence` where applicable.

### Direct grant result mapping

Added `grantOutcomeLine()` and changed `latestRewardLines()` to prefer explicit `RewardGrantOutcome` values.

This allows the player-facing feed to distinguish:
- successful direct inventory acquisition;
- inventory capacity failure;
- invalid canonical item ID;
- invalid quantity;
- unresolved canonical reward facts.

A compatibility fallback remains for older reward records that expose only `autoLootedItems`.

### Audit

Added `RpgVisibleRewardFeedAudit`.

It verifies:
1. valid direct grant mutates inventory and yields `ITEM_GRANTED`;
2. stack-limit overflow yields `INVENTORY_FULL` without granting;
3. unknown item yields `INVALID_ITEM`;
4. zero quantity yields `INVALID_QUANTITY`;
5. null unresolved quantity yields `UNRESOLVED`;
6. unresolved reward data cannot silently become a successful item grant.

## Evidence boundary

This pass does not invent canonical drop probability or quantity. Known major-drop relations still remain non-emittable where probability/quantity are unresolved.

Authoritative flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.

Ground drop/pickup remains retired.

## Integration handoff

Integrator/UX should render `RpgVisibleProgressionPresentation.snapshot(state.rpg()).latestRewardLines` and branch on `RewardLine.kind`.

Recommended visible behavior:
- `ITEM_GRANTED`: acquisition toast and inventory refresh;
- `INVENTORY_FULL`: warning toast;
- `INVALID_*`: prototype QA-visible error;
- `UNRESOLVED`: pending/evidence-safe notice;
- `EXP`: display the verified reward fact only until player EXP mutation truth is resolved.

No `GameView.java`, combat execution, world/map, renderer, NPC, workflow, APK packaging, or main merge changes were made by this RPG pass.
