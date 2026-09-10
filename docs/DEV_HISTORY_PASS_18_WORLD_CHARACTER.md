# PROJECT DARK — DEV HISTORY PASS 18 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_17_WORLD_CHARACTER.md`)

Current user canon was preserved: monster item rewards use direct inventory auto-loot; no ground-drop/pickup path was added or restored.

## Concurrent main audit

Before renderer-audit integration, `GameView` had already advanced concurrently to v0.66 with RPG equipment interaction presentation. To avoid overwriting Integrator-owned work, this pass did not replace the large `GameView` file. The regression guard was attached directly to `CharacterRenderer`, the World/Character-owned presentation boundary.

## Backlog selected

PASS 17 identified the next dependency-free World/Character bottleneck as exhaustive renderer regression/audit coverage for:

`NW / NE / SW / SE × IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD`

with the fixed five-layer order:

`BODY → HAIR → EQUIPMENT → WEAPON → EFFECT`

The purpose is structural regression detection only. It does not claim authenticated original sprite frames, timings, effect frames, or item visual mappings.

## Implementation completed

### 1. Added `CharacterRendererAudit`

Created:
`app/src/main/java/com/projectdark/mobile/CharacterRendererAudit.java`

The audit explicitly defines and checks:
- expected direction count: 4;
- expected state count: 7;
- expected visual layer count: 5;
- exhaustive matrix size: 28 presentation cases;
- exact layer order `BODY, HAIR, EQUIPMENT, WEAPON, EFFECT`;
- `PENDING_CROP` unresolved-asset policy;
- baseline effect-family sanity for common states (`CAST -> CAST`, `HIT -> HIT`, `IDLE/WALK/DEAD -> NONE`).

`matrix()` generates every direction/state combination rather than relying only on raw enum counts.

Commit:
- `9f998d9e4a62cc9e856429ddd2ebd0c3fd5c3eb8` — `Add exhaustive character renderer contract audit`

### 2. Enforced the audit inside `CharacterRenderer`

Updated:
`app/src/main/java/com/projectdark/mobile/CharacterRenderer.java`

Changes:
- renderer class initialization evaluates `CharacterRendererAudit.passes()`;
- constructor fails fast with a diagnostic summary if the structural contract is broken;
- `draw()` rejects null pose/direction/state inputs instead of silently rendering undefined presentation;
- `hasRequiredStateContract()` now delegates to the exhaustive audit;
- `contractAuditSummary()` exposes direction/state/layer/matrix/asset-state diagnostics.

This keeps regression ownership inside World/Character presentation and avoids touching concurrent `GameView` changes.

Commit:
- `7016f9a76e4e01673939a80ddf53edada429904b` — `Enforce exhaustive renderer contract audit`

## Validation

GitHub Actions Run #135 (ID `34436459420`) for commit `7016f9a7...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

Only successfully compiling/building changes are treated as PASS 18.

## DESIGN_CONFLICT / PENDING

No canonical Item/Monster/Skill/NPC/Map/Progression ID, value, or relationship was changed.
No original animation timing/frame count was invented.
No sprite asset was fabricated.
No monster ground-drop/pickup behavior was introduced.

PENDING retained:
- authenticated original player body/hair/equipment/weapon/effect frames;
- item-specific visual asset registry entries;
- verified original frame counts and animation/effect timings;
- evidence-backed per-item layering/occlusion rules;
- canonical `MAP_MILLES` tile/object/collision/portal trace geometry;
- visual golden-image comparison against authenticated original frames once suitable evidence/assets are available.

## Result

The character renderer now has an explicit executable structural audit for all 28 common direction/state combinations and its five ordered presentation layers. Structural changes that remove a required direction/state/layer or violate the unresolved-asset policy now fail fast at renderer initialization rather than silently degrading the presentation contract.

## Next World · Character bottleneck

1. Continue `MAP_MILLES` transform/trace preparation without guessing canonical tile-to-screen geometry.
2. Add evidence-safe visual registry entries only when authenticated item/player/effect sprite references become available; unresolved entries remain `PENDING_CROP`.
3. Prepare a visual golden-comparison harness for future authenticated crops without embedding unverified screenshots as final map/character assets.
4. Preserve the direct-inventory monster reward canon and do not reintroduce world loot entities.
