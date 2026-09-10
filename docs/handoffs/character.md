# Character / Animation Handoff

## 2026-09-10 18:35 KST — agent/character/20260910-1835

### Source-of-truth / supersession gate
Started from latest main and re-read canonical design/data/source-of-truth and Character history. During the run main advanced to `ba018312ce6ca3fa68877cd60ff09606e3890273` with `design/references/CHARACTER_VISUAL_REFERENCE_20260910.md`. That newer canonical reference explicitly records the user's supplied board: scale 1.50, coherent attached anatomy, compact 2.5–3-head chibi silhouette, NW/NE/SW/SE diagonal presentation, connected shoulder→arm→hand→weapon chain, and no floating limbs. This branch was therefore rebuilt on that newer main; the earlier 18:31 branch is superseded.

### Implemented visual delta
Rebuilt `CharacterRenderer` fallback around the approved board instead of incrementally patching the old rectangular mannequin. Verified originals remain `PENDING_CROP`.

---

## 2026-09-10 18:42 KST — agent/character/20260910-1842

### Precision refinement
Refined the concept fallback into `USER_CONCEPT_20260910_CHIBI_DIAGONAL_V2`: explicit neck/pelvis continuity, shoulder→elbow→hand chains, hand-linked sword, off-hand-linked shield, stronger directional face/back-hair distinction and broader SKILL crescent. Scale remains 1.50 and logical foot anchor remains 0.

---

## 2026-09-10 18:52 KST — agent/character/20260910-1852

### New user supersession
The user supplied an actual live runtime screenshot and explicitly rejected the smooth/procedural V2 appearance. The adjacent original LOD character in that screenshot is now the immediate presentation reference: match the old sprite's pixel density, hard edges, compact proportions and palette vocabulary instead of merely matching the earlier concept board silhouette.

Latest main was re-read first (`2b66df9780142e3d84606f4ac0250dcabfa2d2b7`, PR #59 merged), together with DESIGN_CONSTITUTION, DATA_CONTRACT, SOURCE_OF_TRUTH, relevant DEV_HISTORY and the prior handoff. PR #62 remained the unfinished visual refinement line, so this run continues from its head without touching non-owned runtime files.

### Implemented visual delta — pixel-rig V3
Rebuilt the player fallback again as `USER_SCREENSHOT_20260910_PIXEL_RIG_V3`:
- renderer paint explicitly disables anti-alias, dither and bitmap filtering for the actor fallback;
- removed smooth body ellipses/rounded vector anatomy in favor of hard integer pixel-cell construction;
- reduced base rig height from 32 to 30 logical sprite pixels while preserving user-fixed render scale 1.50;
- head/body target raised to 0.36 and head remains wider than the shoulder span, matching the adjacent original's compact old-sprite read;
- head/hair use stepped rectangular pixel masses with 3-tone brown shading instead of smooth vector hair;
- torso uses dark navy/blue multi-tone armor cells, one-pixel-style belt and shoulder plate accents;
- pelvis and both legs are contiguous hard cells; WALK alternates one-pixel near/far leg displacement rather than smooth whole-body motion;
- arms are attached pixel clusters at the shoulder and terminate in hand cells; attack/skill extend the near arm by discrete pixel steps;
- sword is a diagonal pixel staircase beginning at the grip/hand and adds separate hilt/blade highlight cells;
- shield is a tight hard-edged off-hand block instead of a smooth oval;
- forward diagonals retain face pixels; rear diagonals bias the back-hair mass;
- SKILL remains a visible blue-white crescent hook, but body art itself now reads as old-game pixel art rather than vector illustration.

### Regression contract
`CharacterRendererAudit` now requires profile `USER_SCREENSHOT_20260910_PIXEL_RIG_V3`, `HARD_PIXEL_GRID=true`, scale 1.50, shadow 0.72, logical anchor 0, 28 direction/state cases and the five-layer draw contract. Original directional sprite extraction remains `PENDING_CROP`; the user screenshot was used as a measured visual reference, not falsely claimed as a clean source sprite.

### Ownership
No `GameView.java`, map/camera/collision/pathfinding/portal, combat semantics, RPG/inventory/reward/save/progression, HUD/input, NPC dialogue, quest-state or APK packaging file was modified.

### Next P0
1. Device-playtest V3 directly beside the original screenshot reference. Continue this same task if the silhouette still reads too clean/geometric.
2. Highest-value next delta is clean source-backed BODY sprite extraction or a manually traced per-pixel atlas from an unobstructed original character source; do not go back to smooth procedural anatomy.
3. Preserve the fixed 1.50 user scale unless a newer explicit user instruction supersedes it.

---

## 2026-09-10 19:24 KST — agent/character/20260910-1906

### Latest user decision
The user approved the generated white-hair/dark-pants pixel character preview and explicitly asked to cut it into frames, apply each movement frame, and make it the default character in code. This supersedes further procedural-body refinement for normal field movement.

### Implemented visual delta — atlas-backed V4
- added `app/src/main/res/drawable-nodpi/player_default_atlas.png` as an `[ADAPTED]` 120×128 atlas;
- atlas contract is 24×32 per frame, 5 columns × 4 rows;
- row order is `SW / SE / NW / NE`;
- `IDLE` binds column 0 per direction;
- `WALK` cycles columns 1..4 from the existing `walkClock`, so movement direction now selects a distinct approved sprite sequence rather than procedurally redrawing the body;
- `CharacterRenderer` now decodes and owns the default atlas internally, so the existing `new CharacterRenderer()` runtime wiring consumes it with no `GameView.java` modification;
- pixel rendering remains nearest-neighbor: anti-alias, dither and bitmap filtering disabled;
- player scale remains exactly `1.50`, shadow scale `0.72`, logical foot anchor `0`;
- new presentation profile is `USER_APPROVED_ATLAS_20260910_V4`;
- `CharacterRendererAudit` now gates atlas activation, dimensions, four directions, scale 1.50 and the IDLE/WALK atlas-state contract.

### State/evidence boundary
`IDLE` and `WALK` are now genuinely atlas-backed. `ATTACK / SKILL / CAST / HIT / DEAD` still use the connected hard-pixel directional fallback and must be replaced by separately cut approved atlas frames in the next Character pass. The active atlas is user-approved generated artwork, therefore `[ADAPTED]`; it is not claimed as original Nexon sprite pixels. Verified original frames remain `PENDING_CROP`.

### PR / verification
Draft PR #66: `Character: bind approved directional atlas to default movement`.
At handoff time GitHub had not yet reported a workflow run for the latest head, so compile/APK success is not claimed here.

### Next P0
Cut and bind the approved `ATTACK`, then `SKILL`, `HIT`, `DEAD`, and finally `CAST` frames using the same 24×32/nearest-neighbor/1.50 anchor contract. Do not regress IDLE/WALK back to procedural drawing.

---

## 2026-09-10 20:22 KST — agent/character/20260910-2022

### Latest playtest supersession
Re-read `DESIGN_CONSTITUTION`, `DATA_CONTRACT`, `SOURCE_OF_TRUTH`, `PLAYTEST_CANON_20260910_1938`, the visual-reference contract, Character history and this handoff before coding. The device-test canon supersedes the old 1.50 lock: next-test player scale is now `1.60 [ADAPTED]`, with a reduced head/body target `0.28–0.30`.

### Implemented visual delta — atlas-backed V5
- presentation profile: `USER_PLAYTEST_ATLAS_20260910_V5`;
- `PLAYER_RENDER_SCALE` changed from `1.50` to **`1.60`** while logical foot anchor remains `0`;
- rebuilt the 120×128 default atlas from the approved V4 lineage by reducing the visible head band one logical pixel per 24×32 frame; runtime target is `HEAD_TO_BODY_RATIO=0.29` rather than scaling the old head up together with the body;
- preserved four physical rows `SW / SE / NW / NE` and the existing IDLE + 4-frame WALK sequence;
- added `player_attack_atlas.png`, 96×128, 4 ATTACK frames × 4 directions;
- `ATTACK` now advances through the directional image atlas from `stateClock/stateDuration` instead of constructing detached arm/weapon rectangles at runtime;
- `CAST / SKILL / HIT / DEAD` retain the same V5 directional atlas body and anchor, with only state transform/effects until their dedicated frame atlases are cut. This intentionally eliminates the old procedural-body fallback from those states so limb cohesion cannot regress;
- `CharacterRendererAudit` now gates scale 1.60, head ratio 0.28–0.30, 24×32 frames, four directional rows, IDLE/WALK default-atlas binding and ATTACK-atlas binding;
- nearest-neighbor pixel presentation remains enforced; no anti-alias/dither/filtering was reintroduced.

### Acceptance inspection
Manual atlas preview inspection covered NW/NE/SW/SE IDLE, a WALK frame and ATTACK frame. The revised head is visibly smaller relative to the unchanged torso/leg region, all rows remain distinct, arms/legs are part of the image silhouette rather than separately rendered shapes, and ATTACK uses complete actor frames rather than floating limb components. Scale is handled only at draw time, so world/collision coordinates are unchanged.

### Provenance / ownership
The atlases remain user-approved generated `[ADAPTED]` assets and are not claimed as authenticated Nexon pixels; original source sprites remain `PENDING_CROP`. No `GameView.java`, world/camera/collision/pathfinding/portal, Combat, RPG/inventory/reward, HUD/input, NPC dialogue or quest-state file was modified.

### PR
Draft PR #77: `Character: V5 playtest scale and directional attack atlas`.

### Next P0
Continue the same atlas line in this order: dedicated `SKILL` body/effect frames → `HIT` → `DEAD` → `CAST`. Do not reduce scale below 1.60 or restore the former larger-head V4 atlas unless a newer explicit user playtest supersedes this canon.
