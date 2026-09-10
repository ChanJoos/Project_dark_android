# Character / Animation Handoff — CURRENT

Updated: 2026-09-11 05:45 KST
Branch: `agent/character/20260911-0545`
Base main verified at sync: `77132528a29d073080e5966844d033dcefb62fef`
Head after resync: `ce8f6c4aa9cb5f186b487375473ac860fff4f446`

## Current canon

Latest explicit user direction is authoritative. Obsolete V2/V3/V4/V5/R3 redraw history is not a visual source. Current Character canon is: source sprite 24x32, runtime scale 1.50, exact diagonal facing NW / NE / SW / SE, martial artist weaponless except shield, and user-provided martial-artist sprite imagery used directly rather than re-authored interpretation.

## Current implemented line — IDLE / WALK

`app/src/main/res/drawable-nodpi/player_martial_idle_walk.png` is the direct source-cropped user martial-artist atlas from commit `223565b98bda7e561dfea9dee2f7a6b0fa993ae7`, now resynced onto the latest M2 main line.

- source rows were explicitly interpreted as `SW / SE / NW / NE` and normalized to runtime physical rows `NW=0 / NE=1 / SW=2 / SE=3`;
- frame size is 24x32, atlas is 120x128 = 5 columns x 4 rows;
- column 0 is IDLE, columns 1..4 are WALK;
- no runtime mirroring is used;
- all frames share the same frame-bottom foot anchor;
- runtime scale is exactly 1.50 with nearest-neighbor bitmap drawing.

## World -> Character direction audit on current main

Current main `GameView` consumes `WorldMoveTargetController.Direction` as:
- SW -> dir 0 -> Character SW
- SE -> dir 1 -> Character SE
- NW -> dir 2 -> Character NW
- NE -> dir 3 -> Character NE

The renderer independently maps Character direction to atlas rows:
- NW -> row 0
- NE -> row 1
- SW -> row 2
- SE -> row 3

`visualFacingForRow(row)` is reciprocal to `atlasRow(Direction)`. The current code therefore has no enum/index mismatch between the M1/M2 World step direction and Character atlas selection.

## Existing ATTACK line

`player_martial_attack.png` is present and runtime-bound as a four-direction, weaponless attack atlas. It remains the previous adapted attack asset, not yet rebuilt from the latest direct-source lineage. Per the user-ordered gate, do not advance ATTACK replacement or SKILL/HIT/DIE until IDLE/WALK is accepted on device.

## Crash-safe resource gate

- IDLE/WALK expected shape: 120x128;
- ATTACK expected shape: 96x128;
- resource loading disables density scaling;
- missing/decode-invalid/shape-invalid resources do not throw from `CharacterRenderer()`;
- safe fallback preserves app startup;
- no giant inline/base64 atlas.

## Verification status

IMPLEMENTED: latest-main resync complete; direct-source IDLE/WALK, renderer, audit, and existing attack resource are all on `agent/character/20260911-0545`.
STATIC CONTRACT REVIEW: PASS for explicit four-direction mapping, frame/atlas sizes, 1.50 scale, weaponless flag, no-mirror mapping, and crash-safe constructor path.
BUILD VERIFIED: NOT CLAIMED in this run. The execution environment could not resolve github.com for a local clone, and the branch currently has no GitHub commit status checks.
RUNTIME VERIFIED: NOT CLAIMED. Director/device test still must verify true SE down-right presentation and non-corrupt SW/NW left-facing travel.

## Next gate

Do not start SKILL/HIT/DIE. First obtain device acceptance for the current direct-source IDLE/WALK on the integrated M2 village. If accepted, the next Character visual delta is one task only: replace ATTACK with the same direct-source lineage while preserving 24x32, 1.50, explicit four-row mapping, common foot anchor, weaponless martial-artist silhouette, and crash-safe fallback.
