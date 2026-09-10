# RPG Agent Handoff

Run: `20260910-1955`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 35 — latest device-playtest canon first

Direction: supersede the previous next-step plan and align RPG runtime first with the latest canonical amendment `design/PLAYTEST_CANON_20260910_1938.md` on main.

### Latest canonical amendment read first
Main commit `d0fb8aa5a7d4093a18387c9ca8b242df651ca955` records the 2026-09-10 19:38 KST device playtest as USER-CONFIRMED / CANONICAL AMENDMENT.

RPG-owned P0 from that amendment:
- reward presentation is visible but the live inventory does not actually receive an item from `combat_dummy_01`;
- live dummy has no canonical reward relation;
- for vertical-slice verification only, an explicitly isolated `[B]/[ADAPTED] TEST REWARD` is allowed;
- preferred fixture: `IT_GLOVE_LEATHER`, quantity 1, deterministic, exactly once;
- it must never be presented as original monster drop data.

### Implemented
1. `CanonicalMonsterRewardCatalog`
   - preserves all existing canonical monster reward hints unchanged and unresolved where probability/quantity are unknown;
   - adds an isolated `combat_dummy_01` QA fixture;
   - fixture gives no EXP and exactly one `IT_GLOVE_LEATHER`;
   - fixture carries `qaFixture=true`, `Evidence.ADAPTED`, probability `1.0`, quantity `1`, and explicit source label `[B]/[ADAPTED] TEST REWARD — PLAYTEST_CANON_20260910_1938`;
   - `hasNoInventedDropEmission()` now ignores explicitly labeled QA fixtures while still failing if a canonical/original hint silently becomes deterministic;
   - added `hasIsolatedDummyQaFixture()` structural invariant.

2. `RpgPlaytestDummyRewardAudit`
   - emits one `MONSTER_DEFEATED` event for `combat_dummy_01`;
   - verifies first consumption is `PROCESSED_DEFEAT`;
   - verifies inventory `IT_GLOVE_LEATHER == 1`;
   - verifies reward history contains a single `GRANTED` outcome;
   - verifies the dummy grants no EXP;
   - consumes the same ledger event again and verifies `DUPLICATE_OR_STALE`;
   - verifies item quantity remains exactly 1 and reward history remains one entry.

### Commits this pass
- `667bef9dde20588ba9f1774bded57c341c5cf945` — align reward catalog with latest device-playtest canon
- `d4fc29475f07010f3426a7912bd8854d447e2eba` — exactly-once dummy direct-inventory regression audit

### Integration contract
Combat must emit one `MONSTER_DEFEATED(targetId="combat_dummy_01")` event.
RPG then resolves the isolated QA fixture through the existing direct-inventory pipeline.
UX should refresh inventory from RPG state after the reward event; no pickup/world-drop behavior is involved.

Device acceptance from the canonical amendment:
`kill dummy -> one reward event -> inventory IT_GLOVE_LEATHER +1 -> reopen inventory and see item -> repeated event/UI consumption does not duplicate grant`.

### Canon boundary
This fixture is a runtime verification aid only. It is not Master/original LOD reward evidence and must be removed/replaced when a source-backed live Milles monster/reward relation becomes the vertical slice target.

### Previous PASS 34 remains available but is no longer the immediate priority
- full Skill_Master CSV-backed runtime ingestion;
- complete skill-book/detail presentation;
- current official starter acquisition overrides;
- official skill-icon source manifest with per-skill crop still pending.

### Ownership preserved
No `GameView.java`, combat resolver execution, MonsterAI, map/world, renderer, HUD/input, NPC/dialog rendering, workflow, APK packaging, or main merge changes.

## Next RPG P0
1. Re-read latest main canonical amendments before any further feature work.
2. Integrator must wire/verify the dummy reward on device and ensure inventory presentation refreshes after mutation.
3. After the new playtest P0s are cleared, resume the skill/icon completeness work rather than expanding unrelated backend scope.
