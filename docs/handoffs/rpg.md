# RPG Agent Handoff

Run: `20260910-1548`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 28 — visible direct-reward outcome feed

Direction: make reward results visible and machine-readable for the live UI without touching Integrator-owned `GameView.java`.

Ownership remains strict: no combat execution, map/camera/pathfinding, character renderer, NPC presentation, workflow, APK packaging, or main merge changes.

### Source-of-Truth / current branch gate

Re-read the current RPG branch reward state before editing. The branch had already gained detailed reward mutation contracts beyond the PASS 27 handoff:
- `RewardGrantStatus`: `GRANTED / INVENTORY_FULL / INVALID_ITEM / INVALID_QUANTITY / UNRESOLVED_REWARD`;
- `RewardGrantOutcome` attached to `RewardResolution`;
- `consumeCombatWithOutcomes()` distinguishes processed defeat vs duplicate/stale vs non-defeat;
- `grantResolvedRewardItem()` is the direct-inventory detailed grant endpoint;
- `RpgInventoryPresentation.RewardNotice` already carries `grantOutcomes`.

The actual remaining gap was presentation: `RpgVisibleProgressionPresentation` only rendered successful aggregate auto-loot and discarded detailed grant failures.

### Implemented this pass

1. Expanded the visible reward feed with stable machine-readable `RewardLineKind`:
   - `EXP`
   - `ITEM_GRANTED`
   - `INVENTORY_FULL`
   - `INVALID_ITEM`
   - `INVALID_QUANTITY`
   - `UNRESOLVED`
   - `INFO`

2. Expanded `RewardLine` with structured fields:
   - `kind`
   - `text`
   - `itemId`
   - `quantity`
   - `grantStatus`
   - `evidence`

   UI consumers no longer need to parse Korean text to know whether a reward succeeded or failed.

3. `latestRewardLines()` now prefers explicit `RewardGrantOutcome` rows, preserving failed direct-grant attempts that `autoLootedItems` cannot represent.

4. Added `grantOutcomeLine()` presentation mapping:
   - successful grant → `<item> xN 자동 획득`
   - inventory limit → `<item> 획득 실패: 인벤토리 한도`
   - invalid item → `획득 실패: 알 수 없는 아이템 ...`
   - invalid quantity → `<item> 획득 실패: 잘못된 수량`
   - unresolved canonical reward → `<item> 보상 수량/확률 확인 필요`

5. Kept compatibility for older reward records that only expose successful `autoLootedItems` aggregates.

6. Added `RpgVisibleRewardFeedAudit` regression boundary. It verifies:
   - a valid resolved grant mutates inventory and maps to `ITEM_GRANTED`;
   - filling the canonical stack limit then granting one more maps to `INVENTORY_FULL` without mutation;
   - unknown item ID maps to `INVALID_ITEM`;
   - zero quantity maps to `INVALID_QUANTITY`;
   - nullable unresolved quantity maps to `UNRESOLVED`;
   - no unresolved value is silently treated as a successful grant.

### Commits this pass

- `4db800ac841e0a2c873b14e37bafa9b61d70d20e` — surface detailed reward grant outcomes to visible RPG feed
- `e95ccd8298c5e64a029843100e9563f2aeba10a5` — audit visible direct-grant reward outcomes

### Visible integration contract

Integrator/UX should continue to consume:
`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

For reward toast/chat, render `snapshot.latestRewardLines` and style by `RewardLine.kind`, not by parsing `text`.

Recommended behavior:
- `ITEM_GRANTED`: immediate acquisition toast + inventory count refresh;
- `INVENTORY_FULL`: visible warning, no item count increase;
- `INVALID_*`: QA/error-visible diagnostic in prototype builds;
- `UNRESOLVED`: evidence-safe pending message, never a fabricated reward;
- `EXP`: display reward fact only; do not yet mutate player EXP until progression truth is resolved.

### Reward truth boundary

Authoritative flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.

Ground drop/pickup remains retired.

Known major-drop relations still lack authoritative probability/quantity, so canonical live deterministic item emission remains forbidden. This pass improves visibility of the outcome contract; it does not invent missing drop values.

### Progression/action state retained

- Warrior canonical basic skill projection `SK_전사_001`~`015`;
- skill book + learned/locked/other-job state;
- learned active quick-slot candidates only;
- nullable unknown cost/cooldown preserved as PENDING;
- schema-v1 save/restore persists progression/job/level/nullable EXP/nullable Gold/reward sequence/inventory/equipment/learned action IDs;
- reward sequence survives restart for duplicate defeat suppression.

## Next RPG P0

1. Locate/project the canonical Magic master contract and provide equivalent metadata/skill-book/quick-slot surfaces.
2. Add evidence-safe job-selection/progression transition API only where canonical gate conditions are resolved; never invent the early job gate.
3. Resolve normal EXP start/threshold semantics before enabling player EXP mutation from currently verified monster EXP reward facts.
4. Director/Integrator should wire `RewardLine.kind` into the live toast/chat/inventory refresh path and run compile/APK/runtime validation.
5. Keep `GameView.java` changes outside this RPG branch.
