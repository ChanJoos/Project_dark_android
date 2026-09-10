# RPG / Progression / Save handoff

## 2026-09-10 19:46 KST — PASS 35 / live defeat reward integration

### Source state and priority
- Latest main at run start: `9302f302999401903e74197026831ed765e071c6`.
- Latest canonical constitution explicitly marks the tested APK's missing defeat-to-inventory result as a P0 integration defect.
- The older long-running RPG PR #5 contains broader skill/progression work but is not mergeable against current main. This pass starts from current main and isolates only the new P0 reward delta.
- Ground drops and pickup remain retired. No World entity or pickup path was added.

### Live defect root cause
- The Milles runtime currently spawns `combat_dummy_01`, an explicitly `[B]` training monster.
- `CanonicalMonsterRewardCatalog` correctly has no reward relation for that prototype ID.
- Therefore each live defeat reached RPG but resolved to `PENDING_NO_CANONICAL_MONSTER_REWARD`; inventory mutation never occurred.

### Implemented
- Added `AdaptedPrototypeRewardCatalog`, separate from canonical rewards.
- Added deterministic test policy `ADAPTED_TRAINING_REWARD_V1`:
  - source monster: `combat_dummy_01` `[B]`
  - reward item: `IT_B_TRAINING_TOKEN` / `훈련 증표 [B]`
  - quantity: 1 `[B]/[ADAPTED]`
- Registered the training token as a non-equippable prototype item, preventing confusion with any original drop/item acquisition relation.
- `RpgProgressionState.consumeCombat()` now checks canonical rewards first, then the isolated adapted prototype catalog.
- The reward uses the existing centralized `autoLootResolvedItem()` endpoint and mutates inventory directly; no ground state exists.
- Reward DTO/presentation now expose `RewardSource`, `policyId`, evidence, and per-item `AutoLootResult` so UX/QA can distinguish canonical, adapted test, unresolved, success, full, invalid-ID, and invalid-quantity outcomes without parsing text.

### Runtime verification
`AdaptedPrototypeRewardRuntimeAudit` uses the real Milles `RuntimeState` loop:
1. kill the actual `combat_dummy_01` instance;
2. run `RuntimeState.tick()` so the existing combat ledger reaches RPG;
3. verify inventory receives exactly one training token and reward source is `ADAPTED_TEST`;
4. replay retained ledger snapshots and verify no duplicate grant;
5. let RuntimeState respawn the same monster, defeat its second life, and verify inventory quantity becomes exactly two.

Android-free isolated Java compile/run:
- `AdaptedPrototypeRewardRuntimeAudit`: PASS
- existing `RewardPipelineAudit`: PASS
- existing `MonsterDefeatIdempotencyAudit`: PASS
- existing `PotePrototypeRewardBoundaryAudit`: PASS
- `git diff --check`: PASS

### Director / UX integration note
- No new GameView wiring is required for the first visible test: current `RuntimeState.tick()` already calls `rpg.consumeCombat()`, and current reward banner/inventory panel already consume `RpgInventoryPresentation`.
- After this branch is integrated, defeating the live training monster should show `자동루팅 · 훈련 증표 [B] x1`; opening inventory should show its incremented quantity.
- Device play should verify one banner/grant per life and no grant duplication during later frames.

### Boundaries and remaining work
- No CombatResolver/MonsterAI, World, CharacterRenderer, HUD layout/input, NPC/dialogue, quest, or GameView source was changed.
- No canonical monster drop was fabricated. Canonical unresolved probabilities/quantities remain untouched and fail closed.
- Persistence for the new prototype item depends on merging/rebasing the save backend from the older RPG branch; this pass proves the live in-process E2E grant first.

### Next RPG P0
1. Rebase the minimal save/store subset from RPG PR #5 onto current main and persist `IT_B_TRAINING_TOKEN`, reward cursor, inventory/equipment, and learned actions across process restart.
2. Preserve explicit grant outcomes in the persisted/replayed reward feed without granting again after restore.
3. Split the oversized/conflicted RPG PR #5 into current-main, reviewable progression/skill/persistence PRs.
