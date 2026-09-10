# RPG Agent Handoff

Run: `20260910-1601`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 30 — full Lv1→99 EXP HUD projection + Lv10 job gate

Direction: prioritize visible gameplay progression while preserving RPG ownership boundaries. No `GameView.java` edits in this branch.

### Source-of-Truth gate
Re-read:
- `master/data/Level_EXP_Curve.csv`
- `master/data/Progression_Master.csv`
- `master/data/Content_Gates.csv`

Confirmed:
- the full 98-row Lv1→99 cumulative curve is already projected in `LevelExpCurveCatalog`;
- all curve rows retain `Evidence.B` project-balance provenance;
- Lv99 cumulative endpoint is 150,000,000;
- P00 defines the early exit as `Lv10 + 기본기 습득`;
- G01 defines Commoner→basic-job as early job-selection conditions met;
- the exact canonical learned action ID(s) required for the P00 basic-skill condition are still unresolved.

### Implemented this pass

1. Added `RpgProgressionPresentation` for HUD/progression UI.

`LevelProgress` exposes:
- current level;
- cumulative EXP;
- cumulative EXP at current-level start;
- next-level cumulative target;
- EXP earned inside current level;
- EXP required for current level;
- EXP remaining to next level;
- normalized HUD `ratio` in `[0,1]`;
- Lv99 cap state;
- segment / primary hunting metadata;
- evidence.

2. Added explicit Commoner→basic-job gate projection.

`JobSelectionGateStatus`:
- `LEVEL_NOT_MET`
- `SKILL_REQUIREMENT_PENDING`
- `READY`
- `NOT_COMMONER`

Current correct runtime behavior:
- Commoner below Lv10 → `LEVEL_NOT_MET`;
- Commoner Lv10+ → `SKILL_REQUIREMENT_PENDING`;
- the branch intentionally does not fabricate READY because exact required basic-skill IDs are not yet canonicalized.

3. Expanded `RpgVisibleProgressionPresentation.Snapshot` with:
- `levelProgress`
- `jobSelectionGate`

Integrator can now obtain player summary, EXP-bar state, job gate, inventory, action metadata, quick-slot candidates and reward lines through one RPG snapshot.

4. Added `RpgProgressionPresentationAudit` covering:
- Lv1 / EXP 0 → required 10,000 / ratio 0;
- EXP 5,000 → ratio 0.5 / 5,000 remaining;
- cumulative EXP 300,000 → Lv10 / 78,000 required to Lv11;
- Lv10 Commoner gate → `SKILL_REQUIREMENT_PENDING`;
- cumulative EXP 150,000,000 → Lv99 / cap ratio 1;
- further EXP at Lv99 → `LEVEL_CAP`;
- visible snapshot contains both progression DTOs.

### Commits this pass
- `013c057163d0371dfe402cf9f4fa5f321f93e7d0` — full level progress + job gate presentation
- `08b96668613ae1e02b976fe01db05873f0baefff` — expose progression DTOs in visible snapshot
- `cfccf03e551e6a4ec13c44708adc2afe963da8c2` — progression presentation audit

### Existing progression state retained
- all 98 Level_EXP_Curve rows are projected;
- verified monster EXP mutates cumulative EXP and can advance multiple levels;
- duplicate/stale combat sequence does not double-grant EXP;
- progression caps at Lv99 / 150,000,000 cumulative EXP;
- legacy save with `normalExp=null` remains unresolved and is not silently converted to zero;
- save/restore validates level and cumulative EXP interval consistency.

### Visible integration contract
Integrator/UX should consume:
`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

EXP HUD:
- fill ratio: `snapshot.levelProgress.ratio`
- level: `snapshot.levelProgress.level`
- current-level earned: `snapshot.levelProgress.expIntoLevel`
- level requirement: `snapshot.levelProgress.expRequiredThisLevel`
- remaining: `snapshot.levelProgress.expToNextLevel`

Job-selection UI/NPC flow:
- read `snapshot.jobSelectionGate.status`;
- below Lv10 show level requirement;
- at Lv10+ show pending basic-skill requirement until canonical skill IDs are resolved;
- do not infer readiness in GameView.

### Evidence boundary
The Lv1→99 curve is `[B]` project balancing data, not `[O]`/`[V]` original constants. Verified monster EXP rewards may flow through this curve while the curve itself remains `[B]`.

### Ownership preserved
No `GameView.java`, combat execution, MonsterAI, map/world, renderer, NPC rendering, workflow, APK packaging, or main merge changes.

## Next RPG P0
1. Resolve P00/G01 exact basic-skill requirement IDs from canonical skill/progression sources.
2. Implement typed Commoner→basic-job mutation/result API once the requirement is evidence-safe.
3. Persist/restore the first-job transition and learned starting actions.
4. Project Magic metadata after first-job flow is stable.
5. Integrator should wire real EXP bar + job gate into the live HUD/NPC flow and build the integrated APK.
