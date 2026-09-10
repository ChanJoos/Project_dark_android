# RPG Agent Handoff

Run: `20260910-1454`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 26 — visible progression/action projection

User direction for this pass: stop spending the pass on persistence internals and make RPG state consumable by the live UI.

Ownership boundary is preserved: this RPG branch does **not** modify `GameView.java`, CombatResolver, map/camera/pathfinding, character rendering, or NPC presentation.

### Implemented

1. Expanded `RpgActionMetadataCatalog` from prototype-only action metadata into a canonical-aware stable HUD catalog.
   - Existing executing CAST/SKILL/KICK prototype bindings stay explicit `[B]` and `runtimeBound=true`.
   - Added canonical `Skill_Master.csv` projection for:
     - `SK_전사_001` 숏블레이드
     - `SK_전사_002` 더블어택
     - `SK_전사_003` 윈드블레이드
     - `SK_전사_004` 디바투
     - `SK_전사_005` 트리플어택
   - Exposes `actionId/name/iconKey/jobCode/circle/actionClass/resourceCost/cooldown/runtimeBound/learned/state/evidence`.
   - Unresolved Master resource-cost/cooldown values stay nullable. They are not converted to zero.
   - `visibleFor(rpg)` provides the HUD-facing projection without coupling HUD to combat execution internals.
   - Learned canonical IDs now pass the same `isKnownAction()` validation used by save/restore.

2. Added `RpgVisibleProgressionPresentation`.
   - Single read-only snapshot for visible RPG UI.
   - Exposes player job/level/nullable EXP/nullable Gold/progression node.
   - Reuses existing inventory rows.
   - Exposes HUD action metadata via `RpgActionMetadataCatalog.visibleFor(rpg)`.
   - Converts the latest reward resolution into player-facing reward lines such as `EXP +...`, `<item> xN 자동 획득`, or an explicit reward-data-pending line.
   - `numericOrPending()` prevents unresolved numeric values from silently rendering as zero.

### Existing live UI bridge already on main

Current `GameView` already owns and draws `RpgInventoryPresentation`, an inventory-open panel, selected item detail, equip outcome and latest reward status. `RuntimeState.tick()` already forwards the combat ledger into `rpg.consumeCombat(events,this)`. Therefore the RPG state is already in the live runtime path; the missing integration is presentation breadth, not another RPG state instance.

### Integrator / UX request — next visible wiring

Consume `new RpgVisibleProgressionPresentation().snapshot(state.rpg())` from the Integrator-owned HUD/GameView layer.

Recommended visible mapping:
- bottom player panel: `player.jobCode`, `player.level`, `player.exp`, `player.gold`;
- SK/MAGIC quick-slot/detail overlay: `actions` using `name/iconKey/actionId/resourceCost/cooldown/learned/state`;
- reward toast/chat line immediately after `MONSTER_DEFEATED`: `latestRewardLines`;
- existing inventory panel continues using `RpgInventoryPresentation` rows.

Do **not** reinterpret `PENDING_CROP:*` as a real icon asset and do not render null cost/cooldown as 0.

### Reward truth boundary

Authoritative reward flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.
Ground drop/pickup remains retired.

Current canonical drop probability/quantity remains unresolved for the known reward hints, so this RPG agent still does not fabricate deterministic item emission. An unresolved reward must remain visibly pending instead of manufacturing an item.

## Persistence state retained from PASS 25

- `RpgSaveSnapshot` schema v1 persists progression/job/level/nullable EXP/nullable Gold/reward sequence/inventory/equipment/learned action IDs.
- `RpgSaveCodec` provides deterministic binary encoding with SHA-256 integrity validation.
- `RpgFileSaveStore` provides temp-write/fsync/replace plus last-known-good backup recovery.
- `lastCombatSequence` persists, preserving defeat/reward idempotency across process restart.

Director integration still required for Android app-private save directory + process-kill restore verification; do not move that lifecycle wiring into this RPG branch.

## Next RPG P0

1. Continue `Skill_Master.csv` projection beyond the first canonical slice, prioritizing actions for the first playable job path rather than broad low-value coverage.
2. Add equivalent Magic Master projection if/when the canonical magic CSV contract is located and stable.
3. Expand direct-grant outcome DTO so `INVENTORY_FULL / INVALID_ITEM / INVALID_QUANTITY / unresolved reward` can be surfaced verbatim by the visible reward feed.
4. Resolve normal EXP start/threshold truth before enabling EXP mutation; resolved monster EXP facts alone are insufficient to invent a level curve.
5. Keep all GameView/HUD rendering edits in Integrator ownership.
