# PROJECT DARK — DEV HISTORY PASS 23 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_22_WORLD_CHARACTER.md`)

Current user canon was preserved: Nexon-first visual reconstruction, source-covered areas reconstructed before authored fill, adapted fill only for genuinely missing coverage, whole screenshots prohibited as final map textures, and direct-inventory monster rewards with no ground pickup path.

## Web evidence research

Re-checked the Nexon-hosted 2021 old-town map post:
- `https://lod.nexon.com/Community/screenshot/136184?Category2=2`
- title explicitly includes Milles and other old towns;
- nine attached images are exposed by the page;
- current retrieval still does not provide trustworthy attachment pixels, so no attachment was identified as Milles by guess.

Additional Nexon-hosted Milles references were confirmed in search/indexed pages:
- `https://lod.nexon.com/Community/screenshot/121256?Category2=2` — 2004-05-27, 길레노아/로즈마리 relation text.
- `https://lod.nexon.com/community/screenshot/137555` — 2024-04-18, title/body explicitly identify the scene as `밀레스마을`.
- 2024 screenshot index pages also contain multiple Milles-labeled scenes; these remain modern references only and are not legacy geometry authority.

No source was falsely promoted to `PIXEL_VERIFIED`.

## Implementation completed

### 1. Added `MillesReconstructionCoverage`

Created:
`app/src/main/java/com/projectdark/mobile/MillesReconstructionCoverage.java`

The new coverage gate separates reconstruction scope from source-exhaustion status.

Scopes:
- `GROUND_TILE`
- `BUILDING_OBJECT`
- `WATER_EDGE`
- `LANDMARK_OBJECT`
- `COLLISION_WALKABILITY`
- `NPC_PLACEMENT`
- `MONSTER_SPAWN`
- `PORTAL`

Statuses:
- source sweep: `IN_PROGRESS / SOURCE_SWEEP_COMPLETE`
- coverage: `UNKNOWN / SOURCE_COVERED / PARTIAL / UNCOVERED`

`adaptedFillAllowed()` returns true only when BOTH:
1. the source sweep for that scope is explicitly complete; and
2. the scope is positively classified `UNCOVERED`.

PASS 23 initializes every scope as `IN_PROGRESS / UNKNOWN`, therefore no PROJECT DARK-authored `[ADAPTED]` fill is currently legal. This mechanically enforces the user's Nexon-first policy instead of relying only on documentation.

Commit:
- `ad83bd5930680a234ee5f64415b6343b390240b1` — `Add Milles reconstruction coverage gate`

### 2. Bound trace contract to reconstruction coverage

Updated:
`app/src/main/java/com/projectdark/mobile/MillesTraceContract.java`

Added:
- `MillesReconstructionCoverage reconstructionCoverage`
- `reconstructionCoverage()` getter
- `canAuthorAdaptedFill(scope)`
- audit requirement `reconstructionCoverage.preservesNexonFirstGate()`

This keeps the two independent safety gates explicit:
- calibration requires pixel-verified, version-compatible legacy evidence;
- authored fill requires an explicitly completed source sweep plus proven missing coverage.

Commit:
- `866dede56690527b11a29db354f8c7757797a39a` — `Bind Milles trace to reconstruction coverage gate`

### 3. Expanded Milles visual evidence registry

Updated:
`app/src/main/java/com/projectdark/mobile/MillesVisualEvidenceRegistry.java`

Added:
- `NX_MILLES_MODERN_SCENE_20240418_137555`
- page `https://lod.nexon.com/community/screenshot/137555`
- evidence `[V]`
- `PIXEL_FETCH_PENDING`
- `REFERENCE_ONLY`

The source is useful for modern Milles visual vocabulary but is intentionally excluded from legacy calibration.

Commit:
- `5daac90d3eefe162088a712c84ec52dd0bc542a9` — `Expand Milles visual evidence registry`

## Validation

GitHub Actions Run #187 (ID `34438673026`) for commit `5daac90d...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

## DESIGN_CONFLICT / PENDING

No canonical map/location/NPC/item/monster/skill/progression ID or value was changed.
No 2021 attachment was labeled as Milles without pixel inspection.
No Master coordinate was projected into screenshot space.
No modern 2024 coordinate/scene was treated as legacy map geometry authority.
No screenshot was promoted to final map texture.
No `[ADAPTED]` fill was authorized prematurely.

PENDING:
- retrieve/inspect the nine 2021 old-town attachment binaries in an environment that can access the actual image payloads;
- positively identify the Milles attachment(s);
- promote only truly inspected sources to `PIXEL_VERIFIED`;
- create at least two version-compatible `CalibrationAnchor` records before transform activation;
- complete source sweep by reconstruction scope before any scope is allowed to become `UNCOVERED` and eligible for `[ADAPTED]` fill;
- begin TILE -> OBJECT -> COLLISION trace only after calibration is evidence-backed.

## Result

PROJECT DARK now has two mechanical gates around Milles reconstruction: an evidence gate for geometry calibration and a source-exhaustion gate for authored fill. This prevents both coordinate guessing and premature invention while allowing missing areas to be authored later once the source search is demonstrably exhausted.

## Next World · Character bottleneck

1. Continue Nexon-hosted source mining and obtain actual pixels for the 2021 nine-image old-town set.
2. Positively identify Milles and record visible landmark correspondences.
3. Promote qualifying sources to `PIXEL_VERIFIED` and add the first real legacy calibration anchors.
4. Trace source-covered `TILE -> OBJECT -> COLLISION` regions.
5. Only after per-scope source sweeps are complete, mark genuinely missing regions `UNCOVERED` and produce clearly tagged `[ADAPTED]` fill.
