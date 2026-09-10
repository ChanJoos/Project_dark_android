# PROJECT DARK — DEV HISTORY PASS 27 · RPG SKILL BOOK + QUICK SLOT

Date: 2026-09-10
Role: RPG · Progression · Persistence

## Goal

Move RPG work from persistence internals toward immediately visible gameplay data while preserving strict ownership boundaries.

## Source-of-Truth gate

Re-read current main RPG/runtime contracts and `master/data/Skill_Master.csv`.

Confirmed:
- character starts as Commoner Lv1;
- Warrior skills must not be silently granted at boot;
- unresolved numeric values remain unknown rather than being fabricated;
- action execution remains Combat ownership;
- GameView/HUD rendering remains Integrator ownership.

## Implemented

### Canonical Warrior skill projection

Expanded the RPG action catalog to `SK_전사_001` through `SK_전사_015`, covering the verified basic Warrior 1~5 circle slice.

The stable action DTO now exposes:
- actionId
- name
- iconKey
- jobCode
- circle
- actionClass
- resourceLabel
- nullable resourceCost
- nullable cooldown
- targetLabel
- rangeLabel
- effectSummary
- runtimeBound
- quickSlotEligible
- passive
- learned state
- visibility state
- evidence

### Skill-book state

Added explicit UI states:
- `RUNTIME_PROTOTYPE`
- `LEARNED`
- `CURRENT_JOB_LOCKED`
- `OTHER_JOB`

Added `forJob(rpg, jobCode)` so Integrator can render a canonical job skill book without mutating player progression.

### Quick-slot projection

Added `quickSlotCandidates(rpg)`.

Rules:
- passive actions never appear as active quick-slot candidates;
- canonical actions must be actually learned;
- existing prototype combat actions stay visible only as explicit `[B]` runtime bindings until Combat migrates execution to canonical IDs.

### Visible RPG snapshot

`RpgVisibleProgressionPresentation.Snapshot` now includes quick-slot candidates and provides read-only skill-book/UI label helpers.

Unresolved resource cost/cooldown remains `PENDING`; no null-to-zero conversion is allowed.

### Regression audit

Added `RpgActionPresentationAudit` to enforce:
- exactly 15 basic Warrior canonical actions in this slice;
- passive/quick-slot separation;
- nullable unresolved numeric preservation;
- no unlearned canonical action leaking into quick slots;
- learned canonical action becomes eligible;
- unknown action IDs cannot enter learned persistent state.

## Integration request

Integrator/UX should consume RPG DTOs only and map them into:
- SK/MAGIC panel;
- quick slots;
- action detail panel;
- reward toast/chat.

No `GameView.java` change is made in this RPG pass.

## Next

1. Project canonical magic metadata into the same contract.
2. Expose failed direct-grant outcomes to visible reward presentation.
3. Add evidence-safe job/progression transition surfaces where canonical gates are resolved.
4. Keep EXP mutation disabled until start/threshold rules are evidence-safe.
