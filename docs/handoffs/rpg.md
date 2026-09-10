# RPG Agent Handoff

Run: `20260910-1955`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 35 — latest device-playtest canon first

Direction: latest user-confirmed device playtest canon supersedes prior next-step planning.

Main canonical amendment: `design/PLAYTEST_CANON_20260910_1938.md` at commit `d0fb8aa5a7d4093a18387c9ca8b242df651ca955`.

### RPG-owned P0
The live `combat_dummy_01` showed reward feedback but did not mutate inventory. The new canon explicitly permits an isolated `[B]/[ADAPTED] TEST REWARD` for vertical-slice verification: `IT_GLOVE_LEATHER x1`, deterministic, exactly once, never represented as original LOD drop data.

### Implemented
- `CanonicalMonsterRewardCatalog` now includes isolated `combat_dummy_01` QA fixture only.
- fixture: `IT_GLOVE_LEATHER`, probability `1.0` by test design, quantity `1`, no EXP, `Evidence.ADAPTED`, explicit QA source label.
- canonical POTE/ABEL/LYK reward hints remain unchanged and unresolved where probability/quantity are unknown.
- `hasNoInventedDropEmission()` excludes only explicitly marked QA fixtures; canonical entries remain fail-closed.
- `hasIsolatedDummyQaFixture()` validates fixture isolation.
- `RpgPlaytestDummyRewardAudit` verifies first defeat grants exactly one item and replay of the same combat event is `DUPLICATE_OR_STALE`, leaving inventory quantity exactly 1.

### Commits
- `667bef9dde20588ba9f1774bded57c341c5cf945` — playtest-canon dummy reward fixture
- `d4fc29475f07010f3426a7912bd8854d447e2eba` — exactly-once regression audit
- `fe05ebef80160a771c0adf2b591c2e0de53e9d1e` — PASS 35 history

### Device acceptance
`kill dummy -> one MONSTER_DEFEATED -> IT_GLOVE_LEATHER +1 -> reopen inventory and see item -> repeated consumption does not duplicate`.

No world-drop/pickup subsystem is reintroduced.

### Priority after this pass
Do not resume speculative skill/icon expansion ahead of newer playtest P0 amendments. Re-read latest main canon first on every run.

### Ownership preserved
No `GameView.java`, combat effect execution, MonsterAI, map/world, renderer, HUD/input, NPC/dialog rendering, workflow, APK packaging, or main merge changes.
