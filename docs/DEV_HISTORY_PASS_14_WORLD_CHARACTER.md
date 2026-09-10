# PROJECT DARK — DEV HISTORY PASS 14 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read from GitHub `main` before coding in the required order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and latest World PASS (`docs/DEV_HISTORY_PASS_13_WORLD_CHARACTER.md`)

`data/design/PROJECT_DARK_CANONICAL_SEED.md` was not used to override Master data.

Per current `design/SOURCE_OF_TRUTH.md`, the v4.4 `master/data/*.csv` set is the active DB baseline for overlapping structured data. Existing IDs/values were not rewritten.

## Master sheets read for this pass

Directly inspected:
- `master/data/World_Master.csv`
- `master/data/Location_Points.csv`
- `master/data/NPC_Master.csv`
- `master/data/Map_Instance_Master.csv`
- `master/data/Asset_Map_Mapping.csv`
- `master/data/Scene_Object_Mapping.csv`

Positive Milles identity/data confirmed without screenshot-derived reinterpretation:
- World: `TOWN_MILLES` / `밀레스` / `ORIGINAL`
- Runtime map definition: `MAP_MILLES` / `밀레스` / `O+B`
- Location IDs: `LOC_MILLES_CASTLE`, `LOC_MILLES_SEA`, `LOC_MILLES_ALTAR`, `LOC_MILLES_ROSEMARY`, `LOC_MILLES_MODIA`, `LOC_MILLES_TELLIDOAH`
- NPC IDs: `NPC_LANSEL`, `NPC_ROSEMARY`, `NPC_MODIA`, `NPC_TELLIDOAH`, `NPC_MERLIN_MILLES`, `NPC_GILLENOA_MILLES`
- Map asset mapping status: `SOURCE_FOUND`; the old-town reference still requires positive visual identification/decomposition.
- Scene object mapping for `MAP_MILLES`: terrain is `READY_FOR_TRACE`; this does not mean canonical collision/tile geometry is already available.

## World · Character bottleneck selected

PASS 13 exposed six world layers but the runtime still had no explicit bridge from the restored v4.4 Master IDs into `WorldDef`.

The safe dependency-free implementation was therefore to add a read-only Milles Master projection while deliberately NOT converting canonical tile coordinates into current screenshot-space prototype coordinates.

This avoids fabricating tile/collision geometry and preserves the rule that a whole screenshot cannot become the final map texture.

## Implementation completed

Added `app/src/main/java/com/projectdark/mobile/MillesMasterManifest.java`.

It exposes only positively supported Master identity/content fields needed by World runtime:
- `WORLD_ID = TOWN_MILLES`
- `MAP_ID = MAP_MILLES`
- canonical name/status/evidence
- six confirmed Milles location records with their Master coordinates
- six confirmed Milles NPC records
- map/terrain readiness flags preserving `SOURCE_FOUND`, `READY_FOR_TRACE`, `PENDING`, `PENDING_CROP`

No Master coordinate was translated into prototype `GameView` screen geometry.
No portal target/coordinate was invented.
No NPC visual was promoted beyond `PENDING_CROP`.
No existing Monster/Skill/Magic/Item/Equipment/NPC/Quest/Map/Town/Progression ID/value was changed.

Updated `WorldDef.java` to expose the Master projection through `masterManifest()` and `hasMasterBackedMillesIdentity()` while preserving all existing prototype fixture IDs/coordinates and six-layer contract.

Gameplay commits:
- `9a3c6ec3e473356f1544e8c9e7375e29065efd8f` — `Add Master-backed Milles runtime manifest`
- `5c4e4cab3c7421efc7cd1c268bde9526db4c3a8c` — `Wire Milles Master manifest into WorldDef`

## Validation

Local static Java syntax check of the new manifest and the `WorldDef` integration: PASS.

GitHub Actions run #81 for commit `5c4e4cab...` completed SUCCESS:
- Validate Master DB: PASS
- Compile debug sources: PASS
- Build debug APK: PASS
- Upload debug APK: PASS

Only successful gameplay changes remain on `main`.

## DESIGN_CONFLICT / PENDING

No canonical value conflict was resolved by invention in this pass.

PENDING retained:
- `WorldDef.ID = milles_runtime_proto` remains the prototype runtime scene identity while canonical `TOWN_MILLES` / `MAP_MILLES` are now exposed separately. Replacing the prototype scene ID must wait until map geometry/transition migration is complete; do not alias silently.
- canonical Milles tile decomposition
- canonical Milles collision geometry
- object asset decomposition / visual mapping
- canonical portal IDs, coordinates and target-map connections
- canonical runtime placement of Master NPCs into the current screen-space scene
- player/NPC/monster authenticated sprite crops (`PENDING_CROP`)

`LOC_MILLES_ALTAR` keeps the source note `103,11 또는 104,11`; this pass stores the structured X/Y row value `103,11` plus the ambiguity note and does not choose 104,11 as a replacement.

The current official-hosted screenshot remains reference/prototype presentation only and is not promoted to a final monolithic map texture.

## Result

World runtime now has a contract-visible bridge to the restored v4.4 Milles IDs and supported Master coordinates while keeping unverified geometry isolated from the current `[B]` screenshot-space prototype.

This enables later map tracing / object / collision work to consume stable Master identity instead of introducing new runtime IDs.

## Next World · Character bottleneck

1. Extract procedural player drawing from `GameView` into an explicit CharacterRenderer with `Body / Hair / Equipment / Weapon / Effect` layers.
2. Preserve four logical directions projected to `↖ ↗ ↙ ↘` and minimum `IDLE / WALK / CAST / ATTACK / SKILL / HIT / DEAD` presentation contract.
3. Keep actual character sprite frames `PENDING_CROP` until positively identified; prototype layer renderer must remain `[B]` rather than being called original art.
4. In parallel, prepare an evidence-backed transform/trace contract for `MAP_MILLES`; do not convert Master X/Y to screenshot-space by guesswork.
