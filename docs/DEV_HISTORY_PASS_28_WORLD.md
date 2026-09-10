# PROJECT DARK — DEV HISTORY PASS 28 · WORLD

Date: 2026-09-10
Role: World / traversal-connectivity regression gate
Branch: `agent/world/20260910-1556`
Draft PR: #30

## Source-of-Truth gate

Re-read latest `main` governance/design/backlog material before coding:
- `AGENTS.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `docs/DIRECTOR_BACKLOG.md`
- current World DEV_HISTORY lineage on main
- latest branch `docs/handoffs/world.md`

Current canon remains unchanged: original Milles geometry is not yet verified; adapted prototype geometry must remain explicitly tagged and replaceable. Tap movement must WALK through world collision/pathfinding and the target-pending south portal must not fabricate a destination.

## Problem closed

PASS 27 introduced named road/plaza/gate areas and navigation anchors, but those semantic locations were not mechanically checked against the actual `WorldDef.blockers()` collision layer. A later collision edit could therefore leave a road or portal visually represented while making it physically unreachable.

## Implementation

Added `WorldTraversalConnectivityAudit.java`.

The audit:
- instantiates the actual current `WorldDef` and consumes its blocker rectangles;
- consumes `AdaptedMillesVillageLayout` navigation anchors;
- uses the runtime player collision radius;
- flood-fills on the same 16 logical-unit grid used by world tap navigation;
- requires `spawn`, `plaza_center`, `north_cross`, `service_npc_approach`, `west_lane`, `east_lane`, `south_gate_approach`, and `south_portal` to remain in one reachable component;
- verifies the target-pending portal is physically approachable without enabling transition;
- preserves the east/west lower-lane alternate circulation route.

No `GameView.java`, renderer, combat, RPG, HUD, dialogue, quest, inventory or save file was modified.

## Verification

Independent flood-fill against the committed current geometry confirmed all eight required anchors are reachable with player-radius clearance.

Repository-wide Gradle/APK/runtime verification was not claimed. Director/Integrator should add this audit to the existing build/regression gate after integration.

## User-visible consequence

Future edits to village blockers can no longer silently convert a visually open road, lower-lane route, south gate, or portal approach into inaccessible space without failing the World regression gate. This protects the single-village exploration requirement while the prototype geometry remains evidence-gated.

## Remaining P0

- Director/UX runtime wiring of camera + tap movement + spatial presentation.
- Android-device verification of scrolling and touch/world coordinate correctness.
- Verified Milles TILE/OBJECT/COLLISION reconstruction and incremental replacement of adapted geometry.
- Evidence-backed target map/spawn for south portal transition.
