# PROJECT DARK — DEV HISTORY PASS 19 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_18_WORLD_CHARACTER.md`)

Current user canon was preserved: monster item rewards use direct inventory auto-loot; no ground-drop/pickup path was introduced.

## Backlog selected

PASS 18 identified the next World/Character bottleneck as `MAP_MILLES` transform/trace preparation without guessing canonical tile-to-screen geometry.

The current runtime still uses screenshot-space prototype bounds/collision/entity fixtures, while Master provides canonical Milles location coordinates. Treating those two coordinate spaces as interchangeable would silently fabricate map geometry.

## Master evidence checked

`master/data/Location_Points.csv` confirms six current Milles anchors:
- `LOC_MILLES_CASTLE` (118,50)
- `LOC_MILLES_SEA` (118,82)
- `LOC_MILLES_ALTAR` (103,11), with note `103,11 또는 104,11`
- `LOC_MILLES_ROSEMARY` (81,43)
- `LOC_MILLES_MODIA` (78,28)
- `LOC_MILLES_TELLIDOAH` (58,105)

`master/data/Asset_Map_Mapping.csv` marks `MAP_MILLES` as `REFERENCE / SOURCE_FOUND` and explicitly states that the Milles image among the nine old-town images still requires visual identification.

`master/data/Scene_Object_Mapping.csv` marks `MAP_MILLES` terrain as evidence `V`, status `READY_FOR_TRACE`, with old-town maps + real screens intended for geometry/tile tracing and original traversable areas prioritized.

These rows support trace preparation but do not establish a canonical tile-to-screen transform.

## Implementation completed

### 1. Added `MillesTraceContract`

Created:
`app/src/main/java/com/projectdark/mobile/MillesTraceContract.java`

The contract explicitly separates:
- `MASTER_TILE`
- `PROTOTYPE_SCREEN`

It exposes:
- canonical map identity `MAP_MILLES`;
- source status `SOURCE_FOUND`;
- trace status `READY_FOR_TRACE`;
- evidence `V`;
- unresolved image-identification state `PENDING`;
- six Master coordinate anchors copied from `MillesMasterManifest`;
- the six required world trace layers: `TILE / OBJECT / COLLISION / NPC / MONSTER_SPAWN / PORTAL`.

The transform state is currently `PENDING_ANCHORS`.

`projectMasterToScreen()` deliberately throws while calibration is unresolved. The runtime therefore cannot accidentally infer a transform from the screenshot, prototype blockers, player spawn, or arbitrary fitting.

Commit:
- `e09d27f6e87537f95723ad237cf8e6b8555475a6` — `Add Milles trace coordinate contract`

### 2. Wired trace contract into `WorldDef`

Updated `WorldDef.java` with a single `MillesTraceContract` bound to its existing `MillesMasterManifest`.

Added:
- `traceContract()`
- `hasSafeMillesTraceContract()`

The existing runtime prototype remains unchanged:
- `WorldDef.ID = milles_runtime_proto`
- screenshot reference remains prototype/reference presentation only;
- prototype screen bounds/collision/NPC/monster fixtures remain `[B]`;
- canonical Master location coordinates remain separate and are not projected into those fixtures.

Commit:
- `d12ae07cc197b7c49af879ea4a90e556079e3a9d` — `Wire Milles trace contract into world manifest`

## Validation

GitHub Actions Run #145 (ID `34436719634`) for commit `d12ae07c...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS
- Complete job: PASS

Only successfully compiling/building changes are treated as PASS 19.

## DESIGN_CONFLICT / PENDING

No canonical Map/NPC/Location/Monster/Item/Skill/Progression ID, value, or relationship was changed.
No coordinate transform was invented.
No screenshot was promoted to a final map texture.

PENDING retained:
- positive visual identification of the Milles image among the nine old-town map references;
- authenticated trace anchors linking Master tile coordinates to a verified visual/map coordinate system;
- tile decomposition;
- object decomposition;
- canonical collision/walkability geometry;
- canonical NPC screen/runtime placements derived from a verified transform;
- canonical portal IDs, positions, and target-map connections;
- `LOC_MILLES_ALTAR` coordinate ambiguity (`103,11` structured row vs note `103,11 또는 104,11`);
- migration of `WorldDef.ID=milles_runtime_proto` to canonical `MAP_MILLES` runtime identity after geometry/transition readiness.

## Result

`MAP_MILLES` now has an explicit evidence-safe trace boundary. Master tile coordinates and current prototype screen coordinates cannot be silently conflated, and projection remains blocked until evidence-backed calibration anchors are supplied.

## Next World · Character bottleneck

1. Positively identify the Milles old-town reference image and record the evidence before any trace geometry is promoted.
2. Define a calibration-anchor schema that requires independently verified tile and visual coordinates before enabling `MASTER_TILE -> screen/map` projection.
3. Once anchors exist, trace tile/object/collision layers in that order and validate known Master locations against the transform.
4. Keep portal placement and `WorldDef.ID` migration PENDING until the map transform and transition graph are evidence-backed.
