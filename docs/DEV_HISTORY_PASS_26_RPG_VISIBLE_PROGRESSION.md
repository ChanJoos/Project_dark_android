# PROJECT DARK — DEV HISTORY PASS 26 · RPG VISIBLE PROGRESSION

Date: 2026-09-10
Role: RPG · Progression · Persistence
Branch: `agent/rpg/20260910-1338`
Draft PR: #5

## Direction

This pass deliberately shifts away from deeper persistence internals and toward RPG state that can be rendered by the live mobile UI.

The RPG ownership boundary remains intact: no `GameView.java`, combat execution, map/camera/pathfinding, character renderer, or NPC presentation files were modified.

## Implemented delta

### 1. Canonical-aware HUD action metadata

`RpgActionMetadataCatalog` now exposes a stable presentation DTO with:
- `actionId`
- `name`
- `iconKey`
- `jobCode`
- `circle`
- `actionClass`
- nullable `resourceCost`
- nullable `cooldown`
- `runtimeBound`
- learned/state
- evidence

The currently executing CAST/SKILL/KICK prototype actions remain explicit `[B]` runtime bindings.

The first canonical `Skill_Master.csv` slice was projected for the Warrior path:
- `SK_전사_001` 숏블레이드
- `SK_전사_002` 더블어택
- `SK_전사_003` 윈드블레이드
- `SK_전사_004` 디바투
- `SK_전사_005` 트리플어택

Master rows do not resolve concrete resource-cost/cooldown numbers for these entries, so those values remain nullable instead of being fabricated as zero.

### 2. Single visible RPG snapshot

Added `RpgVisibleProgressionPresentation` as an RPG-owned read-only UI contract.

It aggregates:
- player job / level / EXP / Gold / progression node;
- inventory presentation rows;
- action metadata suitable for SK/MAGIC HUD surfaces;
- latest reward lines suitable for toast/chat/panel rendering.

Reward lines can expose:
- resolved EXP facts;
- direct inventory grant lines (`<item> xN 자동 획득`) when a reward is actually emitted;
- explicit reward-data-pending feedback when the monster has no canonical reward resolution.

`numericOrPending()` ensures unresolved EXP/Gold/cost/cooldown values are never silently shown as zero.

## Existing live-runtime bridge confirmed

Current `main` already has:
- `RuntimeState.tick()` forwarding combat ledger events into `rpg.consumeCombat(events,this)`;
- a visible inventory panel in `GameView` backed by `RpgInventoryPresentation`;
- visible current level sourced from `state.rpg().normalLevel()`;
- latest reward status rendered inside the inventory panel.

Therefore this pass does not create another RPG state or duplicate UI mutation logic. It adds the stable presentation surface needed for the Integrator-owned HUD to show more of the existing RPG runtime state.

## Evidence boundary retained

Reward flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.

Ground drop/pickup remains retired.

Known canonical major-drop relationships still lack authoritative probability/quantity. This pass does not manufacture deterministic drops. Unknown numeric values remain `null/PENDING`.

## Integration request

Integrator/UX should consume:

`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

Recommended mapping:
- player panel: job, level, EXP, Gold;
- quick-slot/detail surface: canonical action name/icon/actionId/cost/cooldown/learned state;
- reward feedback: latest reward lines immediately after `MONSTER_DEFEATED`;
- inventory/equipment: continue using existing `RpgInventoryPresentation` rows.

No RPG rules should be reimplemented in `GameView`.

## Next RPG P0

1. Continue canonical Skill/Magic projection for the first playable job path.
2. Expose per-grant outcomes for `INVENTORY_FULL`, `INVALID_ITEM`, `INVALID_QUANTITY`, and unresolved rewards so the UI can display the exact failure reason.
3. Enable EXP mutation only after normal EXP start/threshold rules have sufficient evidence.
4. Preserve defeat/reward idempotency across save/restore.
