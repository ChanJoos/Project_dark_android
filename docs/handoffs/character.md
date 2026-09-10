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

### Directional asset binding contract
Added `DirectionalVisualBinding.java` keyed by common state × direction. Null/empty/PENDING_CROP refs never resolve. Existing Pose constructors remain compatible.

### PR
Draft stacked PR #47: `Character: add directional asset binding and idle motion`.

---

## 2026-09-10 18:00 KST — agent/character/20260910-1800

### Source-of-Truth gate / continuity
Re-read latest `main` (`6785efb6f7504e070ee0c0aa6924d1281e444469`), `DESIGN_CONSTITUTION`, `DATA_CONTRACT`, `SOURCE_OF_TRUTH`, `docs/DEV_HISTORY.md`, `docs/DEV_HISTORY_PASS_23_WORLD_CHARACTER.md`, and the prior character handoff before coding. No newer canonical character rule supersedes the four-direction side-diagonal contract. Continued directly from PR #47 head.

### Completed layered asset contract
Added `LayeredDirectionalVisualBinding.java` so future verified assets can resolve independently by:
- layer: `BODY / HAIR / EQUIPMENT / WEAPON / EFFECT`;
- common state: `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`;
- direction: `NW / NE / SW / SE`.

This preserves a single shared state/direction/anchor basis while allowing individual layers to be replaced as positive source crops become available. Missing or `PENDING_CROP` layer refs remain unresolved and keep the procedural fallback. `WorldEntityPresentationRenderer.Pose` now optionally accepts this layered binding while retaining both older constructors for integration compatibility. `resolvedLayerVisualRef(...)` exposes the selected layer asset without inventing bitmap loading or source frames.

### User-visible visual delta
Added a renderer-only `[B]/[ADAPTED]` action envelope:
- `ATTACK` now leans along the current NW/NE/SW/SE facing axis during its action phase;
- attack shadow widens/compresses slightly at impact, making the strike read more clearly;
- `CAST` and `SKILL` receive smaller direction-preserving body emphasis;
- logical coordinates, collision radii, combat timing and damage semantics are unchanged.

### Regression gate
`WorldEntityPresentationAudit` now verifies BODY and HAIR can resolve independently for the same state/direction, unresolved WEAPON remains null, `PENDING_CROP` is rejected, and layered/whole-actor bindings coexist safely.

### Evidence / boundary
No original sprite/frame/timing was fabricated. Layer refs remain evidence-gated; unresolved source art stays `PENDING_CROP`. No `GameView.java`, map/camera/collision/pathfinding/portal, combat math/AI, inventory/reward/EXP/save/progression, HUD/input, dialogue/quest-state or APK packaging file was modified.

### Remaining Character P0
1. Integrator/UX still must wire `WorldEntityPresentationRenderer` into `GameView.drawNpcs()` / `drawMonsters()`; Character agent will not cross that ownership boundary.
2. Positive source crop identification is required before any BODY/HAIR/EQUIPMENT/WEAPON/EFFECT ref is populated as an actual source asset.
3. After source crops exist, add the bitmap/sprite resolver behind `resolvedLayerVisualRef(...)` while preserving shared anchor/facing contracts.
4. Device-playtest NPC/monster proportions and ATTACK readability once the Integrator wiring lands.

---

## 2026-09-10 18:14 KST — agent/character/20260910-1814

### Source-of-Truth gate / continuity
Re-checked latest main, canonical design/data/source-of-truth, current World/Character DEV_HISTORY, PR #50 and this handoff before coding. PR #50 remained open/mergeable and no newer canonical character rule superseded the NW/NE/SW/SE presentation contract, so this pass continues directly from PR #50 head.

### Source acquisition result
Performed a fresh Nexon-first visual sweep. Confirmed three useful Nexon-hosted community references without promoting community material to `[O]`:
- `NX_COMMUNITY_DESERT_2022`: Nexon storage preview pixel visibly contains small isometric multi-actor gameplay silhouettes; `[V]`, `PREVIEW_VERIFIED`, `REFERENCE_ONLY`.
- `NX_COMMUNITY_CHARACTERS_20040513`: historical Nexon community page titled around a gathering of game characters; `[V]`, image payload still fetch-pending, `PENDING_CROP`.
- `NX_COMMUNITY_PLAZA_20251207`: dense modern multi-character gameplay scene; `[V]`, reference-only for proportion/direction vocabulary, not legacy sprite authority.

Added `CharacterVisualSourceRegistry.java` to track subject, evidence grade, pixel inspection state, direction-identification state and crop readiness. `Source.bindable()` requires O/V evidence + verified pixels + positively identified direction + explicit crop approval + concrete asset URL. Current `bindableCount()` is intentionally zero.

### Evidence admission gate
`WorldEntityPresentationAudit` now requires `CharacterVisualSourceRegistry.preservesEvidenceGate()`. A visible screenshot cannot silently become a sprite asset: `REFERENCE_ONLY`, `PENDING_CROP`, unknown/multiple-uncropped direction sources remain non-bindable until positive identification.

### User-visible visual delta
Updated the live player `CharacterRenderer` WALK presentation with renderer-only `[B]/[ADAPTED]` diagonal weight transfer:
- subtle facing-axis lateral sway follows the walk phase;
- the shadow slightly widens/compresses on foot-contact phase;
- player scale remains `0.92`, logical foot anchor remains `0`, and world/collision coordinates are unchanged.

This makes the existing procedural walk read less like a sliding frontal cutout while preserving the four-direction silhouette. It is not claimed as original LOD animation timing.

### Ownership / evidence safety
No source pixels were copied into the repository. No community screenshot was labeled official art. No unresolved direction was guessed. No `GameView.java`, map/camera/collision/pathfinding/portal, combat math/AI, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue/quest-state or APK packaging change was made.

### Remaining Character P0
1. Acquire an individually isolatable actor crop with a positively identified NW/NE/SW/SE direction before admitting the first real BODY binding.
2. Prefer Nexon-hosted/official evidence; fan material may guide search but remains `FAN` and cannot be silently promoted.
3. Once the first crop is positively identified, create crop metadata/anchor measurements and bind only that proven state+direction+layer; keep other directions `PENDING_CROP`.
4. Integrator/UX still owns `GameView` wiring for NPC/monster renderer presentation.
