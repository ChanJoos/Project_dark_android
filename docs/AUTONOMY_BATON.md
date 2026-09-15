# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: Integration Director / QA Verifier 1

HEAD / WORK LINE:
- shared `main`
- VERIFIED_MAIN_HEAD_BEFORE_LOG_MUTATION: `77478c8d94c3c48922222575c8aa6c279a7cac8b`
- PR #117 exact implementation head: `33bd83cb8aced4e834a625ca1b056ce20f74cfd5`
- PR #117 merge commit: `77478c8d94c3c48922222575c8aa6c279a7cac8b`
- exact implementation tree / merge tree: `4fea5bd13a6acdd0750348923c87059a4fc3c961`

TASK / WORK PACKAGE:
- `COMBAT_PIPELINE_003`
- lifecycle: `BUILD_VERIFIED -> INTEGRATED -> DEVICE_PENDING`
- device findings addressed in code but NOT device-closed: `D214834_PLAYER_TO_MONSTER_ATTACK`, `D214834_MONSTER_TO_PLAYER_ATTACK`, `D214834_MONSTER_MOVEMENT`, `D214834_MOVE_COMBAT_LINK`.

VERIFICATION / RUNTIME WIRING:
- PR #117 routes both MANUAL player melee and AUTO monster basic melee through one `CombatActionOrchestrator.SubmissionGate` owned by `RuntimeCombatSession` before `CombatResolver.begin(...)`.
- the gate uses `CanonicalMeleeTileContract.reachable(actor,target)` for `ACT_ATTACK_BASIC` and `ACT_MONSTER_BASIC`; non-melee actions are not incorrectly forced through the melee gate.
- this closes the known submission-stage radius/adjacency bypass in code: the Resolver's broader spatial envelope cannot authorize basic melee unless canonical adjacency first accepts the same actor/target relationship.
- prior main already contains player-parity monster tile-center locomotion and monster-side canonical attack priming/cancel/contact re-check; PR #117 adds the shared façade gate rather than duplicating those paths.
- reciprocal/illegal-position audit coverage was updated with the implementation and is part of the exact implementation tree.

CI / BUILD EVIDENCE:
- exact PR #117 implementation head `33bd83cb8aced4e834a625ca1b056ce20f74cfd5`: GitHub Actions `Validate PROJECT DARK Android` run #422, run id `34992194710`, completed SUCCESS.
- merge commit `77478c8d94c3c48922222575c8aa6c279a7cac8b` has the identical tree `4fea5bd13a6acdd0750348923c87059a4fc3c961`; no separate Actions run is attached to the merge SHA at this verification point.
- BUILD_VERIFIED applies to the exact integrated code tree proven at PR head; no claim is made that a separate merge-SHA workflow executed.

RECENT PR CLASSIFICATION:
- #117: MERGED / BUILD_VERIFIED implementation tree / INTEGRATED / DEVICE_PENDING.
- #116: SUPERSEDED_AS_BRANCH. Its monster canonical adjacency purpose is already consumed by newer main lineage plus #117; do not merge wholesale.
- #104: selective-source-only historical branch; do not wholesale merge.
- #93/#78 and other stale open PRs: unrelated to this verification gate; do not merge merely to reduce PR count.

REASON / ACCEPTANCE CHECK:
- acceptance criterion `same canonical logical tile-center representation`: code path is materially aligned by existing player-parity monster locomotion plus canonical shared submission gate; fresh APK evidence still required.
- acceptance criterion `four legal 64x32 diagonal relations are reciprocal`: covered by canonical contract/audits and shared actor-target gate; fresh APK evidence still required.
- acceptance criterion `no radius/Euclidean bypass`: shared basic-melee submission gate now fails closed before Resolver begin; no known basic-melee bypass is accepted in this reviewed path.
- acceptance criterion `target selection, legality, facing, attack snapshot, animation direction and hit resolution consume same immutable relationship`: legality/submission is materially unified, but whole-chain device observability is not yet proven; do not mark ACCEPTED.
- exact APK evidence after this tree is absent, so `DEVICE_VERIFIED` and `VISUAL_ACCEPTED` remain NO.

REMAINING RISK:
- fresh device evidence must prove all four reciprocal diagonals, illegal same-tile/non-adjacent positions, movement->attack transition, and no one-sided monster hit.
- attack BODY/robe/weapon presentation remains separate P0 `ATTACK_PRESENTATION_002`; device evidence at predecessor `4e24b94e...` remains FAIL and cannot be superseded by combat-code CI.
- `PROJECT_STATE.yaml` still carries the predecessor device findings as current observable truth; this is correct until a new exact APK closes them.

NEXT RECOMMENDED ACTION:
- do not continue canonical melee micro-tuning unless new evidence fails it.
- next development cycle should advance the parallel highest-value READY P0 `ATTACK_PRESENTATION_002` through its mandatory authored-resource/temporal-frame/scale-foot-hand-anchor investigation gate.
- next QA cycle should keep `COMBAT_PIPELINE_003` at DEVICE_PENDING until fresh exact-APK evidence exists; if a new APK is produced, verify the complete reciprocal movement->attack->contact chain rather than only button response.

ACCEPTANCE STATE:
- IMPLEMENTED: YES
- INTEGRATED: YES
- BUILD_VERIFIED: YES for exact integrated code tree `4fea5bd13a6acdd0750348923c87059a4fc3c961` via exact PR head CI #422
- DEVICE_VERIFIED: NO / DEVICE_PENDING
- VISUAL_ACCEPTED: NO
- ACCEPTED: NO
