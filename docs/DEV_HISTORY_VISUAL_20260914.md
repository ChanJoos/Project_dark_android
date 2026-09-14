# DEV HISTORY — Visual 2026-09-14

Base main: `cbe9268f2d873d2b52160ff56a7c2de29aa6273e`

User device feedback reopened the Visual gate: attack rendered static BODY/robe with only mw001 motion, and SW/SE robe WALK remained misregistered. This pass changes actual runtime renderer code, not only documentation/audits.

Changes:
- normalize actual mm001 group-02 attack source with translation + vertical source crop while retaining common 1.70 presentation scale;
- render matching group-02 mu0000058 in the same source attack composition;
- prevent weapon-only ATTACK animation on source-action failure;
- preserve prior canonical target-facing direction mapping;
- preserve NW/NE robe registration;
- calculate SW/SE robe X/Y registration per selected frame from packaged source alpha center/foot deltas;
- add source evidence probe for 20 robe frames, four attack sources, four attack layer rows, and four mw001 attack transforms;
- keep HIT/HURT/FLINCH pose disabled, WALK 0.12 s/frame / 0.48 s/cycle, and zero attack ghost/trail.

Worker status: IMPLEMENTED. BUILD/DEVICE/VISUAL acceptance not claimed here.
