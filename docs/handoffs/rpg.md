# RPG Agent Handoff

Run: `20260910-1501`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 27 — canonical skill-book + quick-slot projection

Direction: prioritize RPG state that becomes visible in play. Ownership remains strict: no `GameView.java`, combat execution, map/camera/pathfinding, character rendering, NPC presentation, workflow, APK packaging, or main merge changes.

### Implemented this pass

1. Expanded `RpgActionMetadataCatalog` from 5 canonical Warrior actions to the complete currently verified basic Warrior 1~5 circle slice `SK_전사_001`~`SK_전사_015` from `master/data/Skill_Master.csv`.
   - 숏블레이드 / 더블어택 / 윈드블레이드 / 디바투 / 트리플어택
   - 메가블레이드 / 바투 / 투핸드어택 / 드래곤모드 / 피닉스모드
   - 적무기쳐내기 / 레스큐 / 매드소울 / 완전방어 / 크래셔
   - Added target/range/effect summary/resource label while preserving canonical evidence.
   - `resourceCost` and `cooldown` remain nullable where Master has no authoritative numeric value.
   - Icons remain `PENDING_CROP:<actionId>` until real icon assets are cropped/verified.

2. Added explicit visible state for UI:
   - `RUNTIME_PROTOTYPE`
   - `LEARNED`
   - `CURRENT_JOB_LOCKED`
   - `OTHER_JOB`

   This prevents the skill book from presenting a catalogued action as learned.

3. Added `forJob(rpg, jobCode)` skill-book projection.
   - Integrator can render the Warrior skill tree/list before and after job selection without mutating player state.
   - Current Source of Truth still starts the player as `COMMONER` Lv1, so Warrior actions are not silently granted at boot.

4. Added `quickSlotCandidates(rpg)`.
   - Passive actions never become active quick-slot candidates.
   - Canonical actions enter candidates only when actually learned.
   - Existing executable prototype actions remain explicit `[B]` runtime bindings until Combat-owned canonical execution wiring replaces them.

5. Expanded `RpgVisibleProgressionPresentation.Snapshot` with `quickSlotCandidates` and added UI helpers:
   - `skillBook(rpg, jobCode)`
   - `learnedLabel(action)`
   - `costLabel(action)`
   - `cooldownLabel(action)`
   - unresolved numerics stay `PENDING`, never `0`.

6. Added `RpgActionPresentationAudit` regression boundary.
   - requires 15 Warrior canonical actions;
   - checks circle/passive/quick-slot semantics;
   - verifies unresolved numeric cost/cooldown remain null;
   - verifies unlearned canonical actions cannot leak into quick slots;
   - verifies a learned canonical action becomes a quick-slot candidate;
   - rejects unknown learned action IDs.

### Commits this pass

- `c95aebacb8223738fc74ed263514a3579e2367f4` — expand canonical Warrior actions and quick-slot projection
- `29bfaaa20c40713053112b69c9a640c619115eeb` — expose skill book and quick-slot candidates
- `b643d4542c0a35c067072f4379e5e526c47310a6` — add RPG action presentation audit
- `489db0736a638c05193604059b3460d3b0d93c27` — document PASS 27

### Existing live UI bridge on main

`GameView` already draws an RPG inventory panel and `RuntimeState.tick()` already forwards combat ledger events to `rpg.consumeCombat()`. The remaining visible integration is therefore an Integrator/UX wiring task, not a second RPG state implementation.

Integrator/UX should consume:
`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

Recommended next UI mapping:
- SK panel: `skillBook(state.rpg(), selectedJobCode)`
- quick slots: `snapshot.quickSlotCandidates`
- skill detail: name / circle / actionClass / targetLabel / rangeLabel / effectSummary / learnedLabel / costLabel / cooldownLabel
- reward toast/chat: `latestRewardLines`

Do not render `PENDING_CROP:*` as a real icon and do not display null cost/cooldown as zero.

### Reward truth boundary

Authoritative flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.

Ground drop/pickup remains retired. Current verified major-drop relations still lack authoritative probability/quantity, therefore deterministic item emission remains forbidden until data resolves.

### Persistence state retained

- schema-v1 snapshot persists progression/job/level/nullable EXP/nullable Gold/reward sequence/inventory/equipment/learned action IDs;
- SHA-256 deterministic codec;
- crash-safe file save store + backup recovery;
- `lastCombatSequence` survives restart for defeat/reward idempotency.

## Next RPG P0

1. Locate/project the canonical Magic master contract and provide the same stable metadata/skill-book/quick-slot surfaces for magic.
2. Add explicit reward grant outcomes to the visible reward DTO: `INVENTORY_FULL / INVALID_ITEM / INVALID_QUANTITY / UNRESOLVED_REWARD`, not merely successful grants.
3. Add evidence-safe job-selection/progression transition API only where canonical gate conditions are resolved; do not invent the early-job gate.
4. Resolve normal EXP start/threshold truth before live EXP mutation.
5. Director/Integrator must wire the new presentation into GameView and compile/build on integration; this RPG agent must not edit GameView itself.
