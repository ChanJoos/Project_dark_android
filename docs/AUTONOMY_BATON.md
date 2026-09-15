# PROJECT DARK Autonomy Baton

This file carries only the immediate handoff between scheduled autonomous developers. Long-lived project truth belongs in `docs/PROJECT_STATE.yaml`; development rules belong in `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`.

## Current baton

AGENT: Senior Game Developer 3

HEAD / WORK LINE:
- shared `main`
- START_HEAD: `25dc5b042b3858d0780fae36f088948ee7ec82fe`
- RUNTIME_DELTA_COMMIT: `2aa821c8bddae8484758dcc08801195c32ad9521`

TASK / WORK PACKAGE:
- `COMBAT_PIPELINE_003`
- lifecycle: `IMPLEMENTED -> VERIFICATION_PENDING`
- device finding addressed: `D214834_MONSTER_TO_PLAYER_ATTACK`, with supporting coverage for `D214834_MOVE_COMBAT_LINK`.

SOURCES USED / RESOLVED:
- `docs/PROJECT_DARK_DEVELOPMENT_CONSTITUTION.md`
- `docs/PROJECT_STATE.yaml`
- `docs/AUTONOMY_BATON.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `master/MASTER_MANIFEST.md`
- `app/src/main/java/com/projectdark/mobile/CanonicalMeleeTileContract.java`
- `app/src/main/java/com/projectdark/mobile/CanonicalMeleeTileContractAudit.java`
- `app/src/main/java/com/projectdark/mobile/MonsterPlayerTileContractAudit.java`
- `app/src/main/java/com/projectdark/mobile/MonsterAIController.java`

WHAT CHANGED / RUNTIME DELTA:
- removed monster melee start/cancel legality based on Euclidean `42/48` pixel radii;
- monster attack priming now requires `CanonicalMeleeTileContract.reachable(monster, player)`, the same authored ±32/±16 diagonal adjacency contract consumed by player melee;
- a primed monster attack is cancelled immediately if that canonical adjacency is lost;
- both legacy and shared-resolver monster submission routes re-check canonical adjacency at contact submission, preventing a stale windup from landing after the relationship changes;
- shared-resolver attack facing is locked from the same canonical attacker->target relationship used for legality;
- pursuit continues while within chase radius until canonical melee adjacency is reached rather than stopping merely because Euclidean distance falls below a radius.

REASON / EVIDENCE:
- `DEVICE_20260915_214834` records monster damage in relationships where the player cannot reciprocally attack and explicitly diagnoses movement/combat linkage as a P0 failure;
- current `MonsterAIController` still used `d <= 42` to begin and `d > 48` to cancel monster attacks even though `CombatController` player melee already uses `CanonicalMeleeTileContract`;
- this was a deterministic upstream contract split and therefore higher value than visual offset polishing.

TEST / VERIFICATION STATE:
- source contract inspection confirms canonical player melee and authored monster tile locomotion both use the same `WorldMoveTargetController.Direction` ±32/±16 basis;
- code mutation committed as `2aa821c8bddae8484758dcc08801195c32ad9521`;
- exact-head CI had not started at first poll; do NOT promote to BUILD_VERIFIED until Actions proves the current resulting HEAD.
- no device/visual status promoted.

RISK:
- pursuit direction selection can still choose a blocked tile and wait/retry; this change intentionally does not redesign pathfinding;
- this repairs monster legality/facing, but `COMBAT_PIPELINE_003` remains open until verifier proves reciprocal player/monster legality through runtime audits and fresh device evidence;
- attack BODY/robe/weapon presentation is a separate P0 package and remains unresolved.

NEXT RECOMMENDED ACTION:
- verifier: compile/build exact resulting HEAD and execute/inspect reciprocal four-diagonal + illegal-near-position audit coverage; if green, mark BUILD_VERIFIED/DEVICE_PENDING rather than DEVICE_VERIFIED;
- developer: if combat reciprocity static/runtime gate passes, advance to parallel `ATTACK_PRESENTATION_002` investigation gate instead of continuing melee micro-tuning.

ACCEPTANCE STATE:
- IMPLEMENTED: YES
- INTEGRATED: YES on shared main
- BUILD_VERIFIED: PENDING exact-head CI
- DEVICE_VERIFIED: NO; latest exact APK evidence remains FAIL on the predecessor build
- VISUAL_ACCEPTED: NO
