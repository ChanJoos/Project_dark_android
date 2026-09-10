# World handoff — PASS 30

- Branch: `agent/world/20260910-1720`
- Base: `main@405bd764dd38146304fb3939709cf0def17f6958`
- Geometry: `[ADAPTED]/[B]`; original Milles geometry remains unverified and replaceable.

## This pass
Latest canonical constitution states the previous expanded prototype is still too small. `WorldDef` is therefore expanded again to 1600×1120 logical units with west/east districts, longer south traversal, additional structures, a fourth NPC placement and a farther pending south portal. `WorldExplorationContract` exposes stable world-space anchors and its audit prevents silent collapse back to a tiny test room.

## Existing World contracts to integrate
- `WorldMoveTargetController`: generic ground tap vs NPC approach, A*, incremental WALK, blocked/replaced/cancelled/tolerance semantics.
- `WorldCameraTransform`: dead-zone follow, world↔screen conversion, clamp.
- `WorldPortalTransitionController`: fail-closed pending destination and exactly-once transition request semantics.
- Current World PR lineage also contains `WorldNavigationSession`, which should be preferred when integrating movement + camera + portal as one frame-level adapter.

## Director / UX request
`GameView.java` remains World-non-owned. Director/UX should integrate the latest World contracts rather than copy algorithms into the view:
1. Empty eligible map taps: reject UI touches → screenToWorld → generic ground move. NPC tap retains approach priority.
2. Tick WALK first, then camera follow; render all world layers through camera projection while HUD remains screen-space.
3. Use the expanded `WorldDef` bounds and `WorldExplorationContract` anchors. Camera must visibly scroll across the larger village rather than keeping a one-screen-room presentation.
4. Keep `milles_south_exit_proto` blocked until a verified destination map/spawn exists.

## Verification state
- IMPLEMENTED: PASS 30 code committed.
- BUILD VERIFIED: pending Director Gradle/APK gate.
- RUNTIME VERIFIED: pending Android device screenshot/playtest.
- No `GameView.java`, CharacterRenderer, Combat, RPG, HUD or quest files modified.
