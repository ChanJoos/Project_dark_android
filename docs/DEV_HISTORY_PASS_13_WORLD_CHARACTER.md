# PROJECT DARK — DEV HISTORY PASS 13 · WORLD / CHARACTER

Date: 2026-09-10
Role: World · Character

## Source-of-Truth gate executed

Read before coding in the requested order:
1. `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`
2. `master/PROJECT_DARK_V0.6_PLAN_KO.md`
3. `design/DESIGN_CONSTITUTION.md`
4. `design/DATA_CONTRACT.md`
5. `design/SOURCE_OF_TRUTH.md`
6. `docs/DEV_HISTORY.md` and newest available pass history

The run also incorporated the updated precedence note from `design/SOURCE_OF_TRUTH.md`: the verified v4.4 Master snapshot is higher database authority than the earlier 19-tab v1.0 readable master, and `data/design/PROJECT_DARK_CANONICAL_SEED.md` remains derived/fallback only.

During this pass, a concurrent main commit restored all 92 v4.4 Master CSVs with integrity/director governance. No implementation value in this pass was allowed to reinterpret or override those canonical rows.

## World · Character bottleneck selected

`WorldDef` already separated the visual reference URL from prototype collision/entity fixtures, but its runtime API still exposed only blockers/NPC/monster lists. The required world architecture (`Tile Map + Object + Collision + NPC + Monster Spawn + Portal`) was therefore implicit rather than contract-visible.

This was the highest dependency-free World task because it prepares Milles for Master-backed map decomposition without inventing portal/object/tile content and without using the entire screenshot as a final map texture.

## Implementation completed

Updated `WorldDef.java` with an explicit evidence-aware world-layer contract:

- `LayerKind.TILE`
- `LayerKind.OBJECT`
- `LayerKind.COLLISION`
- `LayerKind.NPC`
- `LayerKind.MONSTER_SPAWN`
- `LayerKind.PORTAL`

Added `LayerStatus` so each layer exposes evidence/readiness independently.

Preserved current runtime behavior and existing IDs/values:
- `milles_runtime_proto`
- existing `[B]` collision rectangles
- existing `milles_guide_proto`
- existing `combat_dummy_01`
- current official-hosted visual reference URL

Added typed `WorldObject` and `PortalSpawn` contracts but intentionally left their runtime lists empty. No portal, object placement, target map, canonical map geometry, NPC, or monster relationship was fabricated from the screenshot.

`TILE` and `OBJECT` remain `PENDING/PENDING_CROP`.
`PORTAL` remains `PENDING`.
Existing collision/NPC/monster-spawn fixtures remain explicitly marked prototype/`[B]`/`PENDING_CROP` as applicable.

Added `hasCompleteLayerContract()` as a static/runtime audit hook ensuring every required world layer remains represented even while canonical content is pending.

Gameplay commit:
- `801be36600949361ff41a5ec79e1ea3b4a310750` — `Expose explicit world layer contract`

## Validation

GitHub Actions run #71 (`Validate PROJECT DARK Android`) for gameplay commit `801be366...`:
- checkout/setup: PASS
- Android SDK/build-tools setup: PASS
- `Compile debug sources only`: **PASS**

No APK packaging was introduced.

A subsequent concurrent main commit `3befe0999e68a3ce6fa9d8fb614093cba316953d` restored the 92 Master CSVs and used `801be366...` as its parent, so this World change remains in the current main history rather than being overwritten.

## DESIGN_CONFLICT / PENDING

No new `DESIGN_CONFLICT` was introduced.

PENDING retained:
- authenticated Milles tile decomposition
- authenticated object-layer asset mapping
- canonical Milles collision geometry
- canonical NPC placement/dialogue mapping for the current fixture
- canonical monster placement replacing `combat_dummy_01`
- canonical portal IDs/coordinates/target maps
- character/NPC/monster visual sprites (`PENDING_CROP` until positively mapped/licensing-safe)

The official-hosted screenshot remains visual reference/prototype presentation only and is not promoted to a final monolithic map texture.

## Result

World structure is now contract-visible as six explicit layers while preserving all existing gameplay behavior and provenance. This creates a stable seam for consuming the newly restored 92-sheet v4.4 Master data without embedding canonical world facts in `GameView` or inventing screenshot-derived content.

## Next World · Character bottleneck

1. Read the restored v4.4 `World_Master`, `Location_Points`, `NPC_Master`, `Map_Instance_Master`, `Asset_Map_Mapping`, `Scene_Object_Mapping`, and related sheets directly from repository CSVs.
2. Map only positively supported Milles IDs/relationships into a runtime data loader/manifest; unresolved geometry stays PENDING instead of being guessed.
3. Extract procedural player drawing from `GameView` into an explicit character renderer with Body / Hair / Equipment / Weapon / Effect visual layers while keeping actual sprite assets `PENDING_CROP`.
4. Keep four logical movement directions projected to screen diagonals and preserve `IDLE/WALK/CAST/ATTACK/SKILL/HIT/DEAD` state contract.