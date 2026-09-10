# DEV HISTORY — PASS 32 RPG BASIC JOB CIRCLE ONE

Run: `20260910-1753`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`

## Goal
Make the first-job choice immediately change the visible skill-book surface for all five basic jobs without fabricating acquisition rules, MP costs, cooldowns, or icons.

## Canonical sources
- `master/data/Skill_Master.csv`
- `master/data/Skill_Milestones.csv`
- `master/data/Skill_Requirements.csv`

## Implemented
- Expanded `RpgActionMetadataCatalog` with all canonical circle-1 rows currently present in `Skill_Master.csv`:
  - WARRIOR: `SK_전사_001`, `SK_전사_012`
  - ROGUE: `SK_도적_001`, `SK_도적_002`
  - MAGE: `SK_마법사_001`
  - CLERIC: `SK_성직자_001`, `SK_성직자_002`, `SK_성직자_003`
  - MARTIAL_ARTIST: `SK_무도가_001`
- Preserved unresolved numeric resource cost and cooldown as null.
- Preserved pending icons as `PENDING_CROP:<actionId>`.
- Added `sourceEvidence` so composite source provenance such as `O/V` is not collapsed into a single runtime enum.
- Added `forJobAndCircle(rpg, jobCode, circle)`.
- Expanded the single visible RPG snapshot with `basicJobOptions` and `currentCircleOneActions`.
- After a successful basic-job selection, current-job circle-1 actions become `CURRENT_JOB_LOCKED`; they do not become learned automatically.
- Learned active actions become quick-slot candidates only after `setLearnedAction()` succeeds.
- Added `RpgBasicJobCircleOneAudit` covering per-job counts, evidence preservation, job-selection visibility, no implicit quick-slot unlock, and explicit learning.

## Evidence boundary
`Skill_Master.csv` identifies names/circle/action class/targets/effects, but `Skill_Requirements.csv` still leaves most acquisition requirements unresolved. Therefore catalog presence is not acquisition proof and job selection does not auto-learn these actions.

## Integration request
Integrator/UX should read one snapshot:
`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

Use:
- `snapshot.basicJobOptions` for the five-choice job UI;
- `snapshot.currentCircleOneActions` after selection for the starter skill panel;
- `action.visibilityState` / `learnedLabel(action)` for locked vs learned state;
- `action.sourceEvidence` in QA/debug provenance;
- `snapshot.quickSlotCandidates` only for actually learned/runtime-bound actions.

Do not parse names to infer job/circle and do not treat null cost/cooldown as zero.
