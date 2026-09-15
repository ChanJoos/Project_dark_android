# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: Senior Game Developer 3

HEAD / WORK LINE:
- shared `main`
- START_HEAD: `c89a825ccbf36ddf8bdfc0a2c56a253a0de78bf1`
- latest integrated combat tree remains `4fea5bd13a6acdd0750348923c87059a4fc3c961` via PR #117.

TASK / WORK PACKAGE:
- `ATTACK_PRESENTATION_002`
- lifecycle: `READY -> IN_DEVELOPMENT`
- predecessor `COMBAT_PIPELINE_003` remains `BUILD_VERIFIED / INTEGRATED / DEVICE_PENDING`; do not resume melee micro-tuning without new failing evidence.
- device findings now under active development: `D214834_ATTACK_BODY`, `D214834_ATTACK_WEAPON`, `D214834_ATTACK_SCALE`.

MANDATORY INVESTIGATION GATE — COMPLETED THIS CYCLE:
- `master/data/Asset_Animation_Semantics.csv` explicitly marks group `02` as `UNRESOLVED` with `no explicit semantic mapping captured`; therefore the four group-02 resources MUST NOT be interpreted as four temporal attack frames.
- `master/data/Asset_Animation_Frame_Master.csv` proves four source-backed `mm001` group-02 BODY resources and four matching `mu0000058` robe resources exist. Their dimensions/pivots differ by source index and correspond to the four runtime directional source selections already encoded in `CharacterRenderer.actionSourceIndex(...)`.
- current renderer packages all four BODY and robe group-02 source bitmaps and uses a common `SOURCE_PRESENTATION_SCALE = PLAYER_RENDER_SCALE / SOURCE_BAKED_SCALE`.
- current source-action geometry computes placement from the authored per-source BODY offsets/pivot/foot values into the common logical `SOURCE_FOOT_ANCHOR_X/Y`; it does not require inventing a second attack scale.
- current `drawSourceAction(...)` derives BODY/robe semantic registration with `CharacterSemanticRig` and preserves equipped robe/weapon composition.
- current `CharacterVisualEvidenceProbe` already measures idle-vs-attack composite height/foot/center error and weapon hand/tip geometry; this is the correct evidence surface to extend/use rather than asserting bitmap loadability alone.

ROOT CAUSE FOUND:
- `CharacterRenderer.draw(...)` currently evaluates `if (pose.state==ATTACK && sourcePaperDollReadyFor(pose)) { drawAdaptedAttack(...); return; }` BEFORE the existing `usesSourceActionPose(...) && sourceActionReadyFor(...)` branch.
- for the normal equipped source paper doll this early return is true, so `drawSourceAction(...)` is unreachable during the very ATTACK/SWING state it was built to render.
- `drawAdaptedAttack(...)` simply calls `drawSourcePaperDoll(..., true)`: stationary idle BODY + robe with only the weapon phase changing. This exactly matches `DEVICE_20260915_214834` where the person does not visibly attack and mokdo appears to move independently.
- therefore this is a control-flow regression, not evidence that authored BODY resources are absent and not a reason to invent more offsets.

SOURCE / TEMPORAL DECISION:
- group-02 remains `UNRESOLVED_SOURCE_SEQUENCE` temporally.
- valid runtime use is ONE source-backed directional attack pose during the contact window, with idle paper doll at startup/recovery. That visibly changes BODY pose without falsely claiming the four directions are temporal frames.
- do NOT fabricate interpolation or count direction variants as temporal animation.

REQUIRED MINIMAL RUNTIME DELTA:
- reorder ATTACK rendering so ATTACK/SWING first consumes `drawSourceAction(...)` only inside the existing `ATTACK_ACTION_BEGIN..ATTACK_ACTION_END` window when `sourceActionReadyFor(pose)` is true;
- outside that window use the equipped source paper doll, preserving startup/recovery and current equipment;
- keep adapted stationary weapon-only rendering only as a fail-safe fallback when source action resources/geometry are genuinely unavailable, never as the primary normal ATTACK path;
- preserve common presentation scale and logical foot anchor; do not introduce new screen-space magic offsets;
- weapon during source action must ultimately attach to the action BODY dominant-hand semantic anchor, not the idle BODY hand anchor. Current `drawSourceAction` still calls `drawWeapon`, whose hand anchor is derived from `idleWalkAtlas`; this remains a known acceptance risk and must be checked/fixed coherently with the branch activation rather than hidden.

WHY NO RUNTIME COMMIT WAS CLAIMED THIS CYCLE:
- the investigation gate was mandatory before renderer mutation because two previous attempts produced scale/anchor regression and then suppressed BODY attack entirely.
- the gate found a second coupling: merely making `drawSourceAction` reachable would still leave weapon attachment derived from the idle BODY hand anchor. Activating that half-fix would risk recreating the floating mokdo failure.
- therefore lifecycle is honestly `IN_DEVELOPMENT`, not IMPLEMENTED. No BUILD_VERIFIED, DEVICE_VERIFIED or VISUAL_ACCEPTED claim is made for attack presentation.

NEXT RECOMMENDED ACTION:
- implement the renderer change as one coherent delta: source-action branch priority + action-BODY dominant-hand weapon attachment + existing common scale/foot anchor preservation.
- extend/consume evidence probe so all four directions prove source-action resource readiness, idle->attack foot/scale continuity, and handle-to-action-hand attachment.
- compile/build exact resulting head. If CI passes, mark `VERIFICATION_PENDING` / `BUILD_VERIFIED` as evidence allows, but keep `DEVICE_PENDING`/`VISUAL_PENDING` until a fresh exact APK demonstrates the attack.
- after this acceptance gate is met, advance to `HIT_READABILITY_001` / other READY player-facing work rather than continuing minor offsets.

ACCEPTANCE STATE:
- ATTACK_PRESENTATION_002 IMPLEMENTED: NO
- INVESTIGATION_GATE: YES / source temporal semantics remain unresolved by design
- BUILD_VERIFIED for attack delta: NO
- DEVICE_VERIFIED: NO
- VISUAL_ACCEPTED: NO
- COMBAT_PIPELINE_003: BUILD_VERIFIED / INTEGRATED / DEVICE_PENDING
