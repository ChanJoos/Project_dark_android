# World handoff — PASS 31

- Branch: `agent/world/20260910-1747`
- Base: latest main after PASS 30 village expansion integration.
- Geometry status: `[ADAPTED]/[B]`; original Milles geometry remains unverified and replaceable.

## PASS 31 delta
PASS 30 expanded the village, but three stable exploration anchors were placed directly on occupied runtime entity coordinates. They are now approach points:
- west district `(320,705)` clear of `milles_west_proto`.
- east district `(1350,715)` clear of `combat_dummy_01`.
- south gate `(790,1000)` clear of `milles_gate_proto`.

Added `WorldExplorationOccupancyAudit`:
- consumes current `WorldDef` blockers/NPC/monster fixtures,
- mirrors current `RuntimeState` player/NPC/monster collision radii,
- flood-fills a 16-unit four-neighbour navigation lattice,
- requires all stable exploration anchors to be occupiable and spawn-connected within 20 logical units.

Independent model verification in this run: 4,602 reachable lattice cells; all 9 anchors occupiable and reachable.

## Director / UX integration request
`GameView.java` remains untouched. Use `WorldExplorationContract` approach anchors for generic ground navigation/debug destinations rather than entity positions. Empty eligible map taps must use screen→world→ground move; NPC taps remain the separate NPC approach path. Keep camera/world projection and portal logic in existing World contracts rather than duplicating them in the view.

## Verification state
- IMPLEMENTED: PASS 31.
- TRAVERSAL MODEL VERIFIED: 9/9 anchors reachable/occupiable.
- BUILD VERIFIED: pending Director Gradle/APK gate.
- RUNTIME VERIFIED: pending Android device playtest/screenshot.
- South portal remains `PENDING_TARGET_MAP`.
