# PASS 33 — RPG typed action learning

## Goal
Promote learned-action mutation from a low-level boolean setter into a typed, evidence-safe RPG acquisition contract that can be consumed by NPC/quest/skill UI integration.

## Changes
- Added `RpgActionLearningService`.
- Added typed proof boundary: `PENDING / SATISFIED`.
- Added typed outcomes: `LEARNED / ALREADY_LEARNED / UNKNOWN_ACTION / RUNTIME_PROTOTYPE / WRONG_JOB / REQUIREMENT_PENDING / MUTATION_FAILED`.
- Preserved unresolved acquisition requirements rather than inventing level/stat/item/Gold gates.
- Successful learning persists through the existing schema-v1 learned-action set.
- Learned active actions immediately become eligible through `quickSlotCandidates`.
- Added `RpgActionLearningAudit` covering job mismatch, pending proof, success, duplicate learning, quick-slot promotion, persistence and unknown action handling.

## Evidence boundary
`master/data/Skill_Requirements.csv` still leaves most acquisition semantics unresolved. `RequirementProof.SATISFIED` must only be supplied by the owning acquisition flow after it resolves its own canonical gate.

## Ownership
No changes to GameView, combat execution, MonsterAI, map/world, renderer, NPC/dialog rendering, workflow, APK packaging or main merge.
