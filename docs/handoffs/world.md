# World handoff — PASS 31

- Branch: `agent/world/20260910-1723`
- Base lineage: PASS 30 head `aae160117dc391e33c82d89ec7d9a2eac8ad935a` → `main@405bd764dd38146304fb3939709cf0def17f6958`
- Geometry: `[ADAPTED]/[B]`; original Milles geometry remains unverified and replaceable.

## PASS 31 delta
PASS 30 expanded the village but three named exploration targets were placed directly on runtime entities and therefore were invalid generic ground-move targets under current occupancy rules. This pass moves them to nearby walkable approach points:
- west district: `(285,705)` → `(320,705)` to clear `milles_west_proto`.
- east district: `(1315,715)` → `(1350,715)` to clear `combat_dummy_01`.
- south gate: `(790,1035)` → `(790,1000)` to clear `milles_gate_proto`.

Added `WorldExplorationOccupancyAudit`:
- consumes `WorldDef` blockers/NPC/monster fixtures,
- uses `RuntimeState.PLAYER_RADIUS/NPC_RADIUS/MONSTER_RADIUS`,
- flood-fills a 16-unit four-neighbour navigation lattice,
- requires every `WorldExplorationContract` anchor to be occupiable and reachable from spawn within explicit tolerance.

Independent run verification found 4,602 reachable lattice cells and all 9 anchors reachable/occupiable.

## Existing World contracts to integrate
- `WorldMoveTargetController`: empty-ground move vs NPC approach, A*, WALK-only movement, blocked/replacement/cancel/tolerance semantics.
- `WorldCameraTransform`: dead-zone follow, world↔screen, clamp.
- `WorldPortalTransitionController`: fail-closed target-pending lifecycle.
- `WorldNavigationSession`: preferred frame-level adapter for movement + camera + portal where available in integration lineage.
- `WorldExplorationContract`: stable walkable exploration targets for the expanded prototype village.

## Director / UX request
`GameView.java` remains untouched by World. When integrating debug destinations/minimap/navigation targets, use the updated approach anchors rather than NPC/monster positions. Empty eligible map taps still require UX wiring `screenToWorld → requestGroundMove`; NPC taps must remain `requestNpcApproach` and must not leak into generic ground movement.

## Verification state
- IMPLEMENTED: PASS 31.
- STATIC/TRAVERSAL MODEL VERIFIED: all 9 anchors reachable in this run.
- BUILD VERIFIED: pending Director Gradle/APK gate.
- RUNTIME VERIFIED: pending Android device playtest/screenshot.
- South portal still `PENDING_TARGET_MAP`; no fabricated transition destination.
