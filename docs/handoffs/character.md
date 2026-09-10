# Character / Animation Handoff

## 2026-09-10 17:26 KST — agent/character/20260910-1726

### Source-of-Truth gate
Read latest `main` first:
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `docs/DEV_HISTORY.md`
- latest relevant World/Character history `docs/DEV_HISTORY_PASS_23_WORLD_CHARACTER.md`

### Completed visual delta
Updated `CharacterRenderer` with the user-approved reduced player scale (`0.92`), independent logical foot anchor, four side-diagonal NW/NE/SW/SE silhouettes, direction-preserving WALK/ATTACK/CAST/SKILL/HIT/DEAD presentation, and separate future directional asset slots. Procedural geometry/timing remains `[B]/[ADAPTED]`; authenticated original frames remain `PENDING_CROP`.

### PR
Draft PR #40 was later integrated by Director into main.

---

## 2026-09-10 17:47 KST — agent/character/20260910-1747

### Source-of-Truth gate / continuity
Re-read latest canonical design/data/source-of-truth and history before coding. Director integrated PR #40 and aligned its audit on main, so player directional presentation is now canonical runtime code.

### Completed renderer-owned delta
Added `WorldEntityPresentationRenderer.java` and `WorldEntityPresentationAudit.java`:
- NPC scale `0.84`, monster scale `0.88`, independent shadow scales, logical foot anchor `0`;
- NW/NE/SW/SE side-diagonal silhouettes and far/near overlap;
- monster CHASE/WANDER→WALK, ATTACK→ATTACK, hitFlash→HIT, death/respawn→DEAD presentation mapping;
- directional attack lunge, hit recoil/flash, directional fall, selection ring and effect hooks;
- all source sprites remain `PENDING_CROP`.

### Integration request — owner: Integrator/UX
`GameView.drawNpcs()` / `drawMonsters()` still own legacy primitive drawing. Replace those calls with `WorldEntityPresentationRenderer.draw(...)` and retain per-entity last-known facing so IDLE/HIT/DEAD preserve direction. Character agent must not modify `GameView.java`.

### PR
Draft PR #44: `Character: add directional NPC and monster renderer seam`.

---

## 2026-09-10 17:54 KST — agent/character/20260910-1754

### Source-of-Truth gate / continuity
Started by re-reading latest main `6785efb6f7504e070ee0c0aa6924d1281e444469`, `DESIGN_CONSTITUTION`, `DATA_CONTRACT`, `SOURCE_OF_TRUTH`, and current DEV_HISTORY. No newer character visual canon supersedes the NW/NE/SW/SE requirement. PR #44 remains open/mergeable, so this run deliberately continues its lineage instead of choosing a new topic.

### Completed visual delta
Extended `WorldEntityPresentationRenderer` with renderer-only `[B]` IDLE motion while preserving current facing:
- NPC receives a small breathing vertical motion and facing-axis sway;
- monster receives a slightly stronger low-body breathing/sway cycle;
- shadow width/height subtly counter-pulse during IDLE;
- WALK/ATTACK/HIT/DEAD behavior remains unchanged and logical coordinates/anchors are untouched.

This is intentionally a prototype animation cue, not claimed as original LOD timing/frame data.

### Directional asset binding contract
Added `DirectionalVisualBinding.java`:
- binds independently by `CharacterRenderer.State × CharacterRenderer.Direction`;
- supports all common states (`IDLE/WALK/CAST/ATTACK/SKILL/HIT/DEAD`) and all four directions;
- `null`, empty refs, and `PENDING_CROP` never resolve as valid source assets;
- `resolvedVisualRef(Pose)` in `WorldEntityPresentationRenderer` prefers the state+direction binding and safely falls back to the legacy single `visualRef` only when it is actually resolved;
- existing Pose constructor remains available, so Integrator wiring is not broken by the new contract.

`WorldEntityPresentationAudit` now verifies unresolved fallback safety, partial directional binding, PENDING_CROP rejection, and resolved NW binding behavior.

### Evidence / boundary
No original sprite/frame/timing was fabricated. All procedural IDLE motion remains `[B]/[ADAPTED]`, original sprites remain `PENDING_CROP`. No `GameView.java`, map/camera/collision/pathfinding/portal, combat math/AI, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue or quest-state file was modified.

### Remaining Character P0
1. Integrator must wire PR #44 renderer seam into `GameView` before NPC/monster deltas are visible in the integrated APK.
2. After positive identification of real directional source crops, populate `DirectionalVisualBinding` by state/direction and add bitmap/sprite drawing behind `resolvedVisualRef(...)`; do not guess missing directions.
3. Device-playtest NPC 0.84 / monster 0.88 against approved player 0.92 and adjust presentation-only constants only if needed.
4. Keep acquiring source-backed directional evidence; `PENDING_CROP` remains authoritative until positively identified.
