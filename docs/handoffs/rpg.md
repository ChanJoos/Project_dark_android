# RPG Agent Handoff

Run: `20260910-1748`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 31 — Commoner → basic-job transition contract

Direction: turn the Lv10 job gate into an actual RPG-owned mutation path without fabricating unresolved starter-skill acquisition rules.

### Source-of-Truth gate
Re-read:
- `master/data/Progression_Master.csv`
- `master/data/Content_Gates.csv`
- `master/data/Skill_Milestones.csv`
- `master/data/Skill_Requirements.csv`

Confirmed:
- P00 exits at `Lv10 + 기본기 습득`;
- G01 enforces Commoner → basic-job selection;
- five basic jobs are represented: 전사 / 도적 / 마법사 / 성직자 / 무도가;
- 1-circle milestone representatives are 전사=숏블레이드, 도적=찌르기/센스몬스터, 마법사=마레노, 성직자=쿠로, 무도가=정권;
- corresponding IDs are `SK_전사_001`, `SK_도적_001`, `SK_도적_002`, `SK_마법사_001`, `SK_성직자_001`, `SK_무도가_001`;
- `Skill_Requirements.csv` still leaves most exact acquisition requirements as `확인 필요`.

### Implemented

1. Added `BasicJobSelectionService`.

Supported job codes:
- `WARRIOR`
- `ROGUE`
- `MAGE`
- `CLERIC`
- `MARTIAL_ARTIST`

Each `JobOption` exposes display name plus the canonical 1-circle milestone action IDs for UI guidance. These IDs are informational milestones, not automatically learned skills.

2. Added typed selection outcomes:
- `SELECTED`
- `INVALID_JOB`
- `NOT_COMMONER`
- `LEVEL_NOT_MET`
- `BASIC_SKILL_PROOF_REQUIRED`
- `STATE_RESTORE_FAILED`

3. Added explicit `BasicSkillProof` boundary.
- `PENDING`: no mutation;
- `SATISFIED`: allows transition only after the owning quest/NPC/progression flow has independently verified the unresolved basic-skill gate.

This prevents Lv10 alone from silently becoming enough while still providing a real mutation endpoint once external orchestration has evidence.

4. Successful selection mutates persisted RPG state through the existing validated snapshot/restore path:
- `progressionNode: COMMONER -> BASIC_JOB`
- `currentJobCode: COMMONER -> selected basic job`
- level / cumulative EXP / Gold / inventory / equipment / combat sequence / learned action IDs are preserved.

5. No starter skill is auto-learned during job selection. Acquisition semantics remain unresolved in canonical requirements and must not be invented.

6. Added `BasicJobSelectionAudit` covering:
- Lv9 rejected even with satisfied proof;
- Lv10 + pending proof rejected without mutation;
- Lv10 + satisfied proof selects WARRIOR and moves node to BASIC_JOB;
- transition survives save → restore;
- learned-action set remains unchanged;
- second basic-job selection after leaving COMMONER is rejected.

### Commits this pass
- `8541443f2ffafb98f5b7515e9d629bdbed1ebc10` — evidence-safe basic job selection transition
- `51b29c5bf3431022cd32b1c01aafefca1bd96913` — job selection persistence/regression audit

### Integration contract
Quest/NPC/Integrator ownership should call:
`BasicJobSelectionService.select(state.rpg(), jobCode, proof)`

Rules:
- do not synthesize `SATISFIED` from Lv10 alone;
- only the owning progression/NPC/quest flow should promote the proof after its own gate is resolved;
- use `BasicJobSelectionService.options()` to render the five job choices and milestone hints;
- after `SELECTED`, refresh `RpgVisibleProgressionPresentation.snapshot(state.rpg())` so job-aware skill visibility and equipment requirements update immediately.

### Existing progression retained
- full Lv1→99 cumulative EXP curve with `[B]` provenance;
- actual EXP mutation and Lv99 cap;
- normalized EXP HUD progress;
- direct-inventory reward outcome feed;
- reward-sequence idempotency;
- save/restore validation;
- Warrior action metadata and learned quick-slot candidates.

### Evidence boundary
Milestone starter actions are project milestone mappings; exact acquisition costs/requirements remain unresolved where `Skill_Requirements.csv` says `확인 필요`. Do not auto-grant them or reinterpret them as verified acquisition rules.

### Ownership preserved
No `GameView.java`, combat effect execution, MonsterAI, map/world, character renderer, NPC/dialog rendering, workflow, APK packaging, or main merge changes.

## Next RPG P0
1. Project basic-job options into the single visible RPG snapshot so Integrator does not need a second service lookup.
2. Resolve starter-skill acquisition semantics where stronger O/V evidence exists; add typed learn-action mutation without fabricating missing costs/items/stats.
3. Expand action metadata beyond Warrior to Rogue/Mage/Cleric/Martial Artist 1-circle milestones.
4. Connect successful first-job transition to job-aware skill-book/quick-slot surfaces.
5. Director/Integrator should wire the five-choice job UI/NPC interaction and run compile/APK/runtime validation.
