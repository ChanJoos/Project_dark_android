# World handoff — PASS 27

- Branch: `agent/world/20260910-1455`
- Base lineage: `main@3b58a8d08805cf9031466fe0c50d56e17aa1344d`
- Master: M001 / current 92-CSV baseline; no canonical Master values changed.
- Geometry status: original Milles geometry remains unverified. Current expanded village and all new spatial semantics are `[ADAPTED]/[B]` prototype data only.

## World contracts now available

### Tap movement — PASS 24
- `WorldMoveTargetController`
- separate generic ground move vs NPC approach APIs
- four-neighbour A* + incremental WALK only; no teleport
- explicit reach tolerance, blocked, replacement and cancellation semantics

### Camera / expanded village — PASS 25
- `WorldCameraTransform`
- dead-zone follow, world→screen, screen→world, map clamp
- expanded prototype village footprint with structures, NPC placements and south-gate route

### Portal transition — PASS 26
- `WorldPortalTransitionController`
- readiness / target-pending / disabled distinction
- one transition request per overlap, stale-completion protection
- active move target is cancelled only after transition sink accepts the request
- current south exit remains `PENDING_TARGET_MAP`; it must not teleport anywhere

### Spatial exploration semantics — PASS 27
- `WorldSpatialLayout`
- `AdaptedMillesVillageLayout`
- explicit `ROAD / PLAZA / GATE / OPEN_SPACE` regions
- stable anchors for spawn, plaza, road junction, service approach, west/east lower lanes, south-gate approach and portal
- all prototype entries remain `ADAPTED/B` and replaceable with verified Milles geometry
- `WorldSpatialLayoutAudit` verifies unique IDs, meaningful traversal distance (>900 logical units), separated lower-lane route choice and gate/portal spatial containment

## Director / UX integration request

`GameView.java` remains untouched by the World agent. Do not duplicate these algorithms there.

1. Construct one `WorldCameraTransform` using active map bounds and drawable world viewport.
2. On boot/respawn/transition, snap camera to player; after logical movement, call camera follow.
3. Render TILE/OBJECT/NPC/MONSTER/PORTAL and PASS 27 spatial regions in world-space through the camera transform. HUD remains screen-space.
4. For world taps: first reject UI-owned touches, convert screen→world, then dispatch generic ground move. NPC hit-test retains priority and dispatches NPC approach instead.
5. Consume `AdaptedMillesVillageLayout.create()` for prototype road/plaza/gate semantics rather than hard-coding those positions in view code. These areas can drive visible road/plaza presentation, minimap semantics and stable tap destinations.
6. Observe portal overlap through `WorldPortalTransitionController`. `milles_south_exit_proto` must show unavailable/locked feedback while target map/spawn remains unverified.
7. When a verified portal target is added, destination map owns arrival coordinates; world transition contract passes stable `targetMapId` + `targetSpawnId` only.

## Verification

- PASS 24 Android-free move-target audit: PASS (previous run).
- PASS 25 deterministic camera contract source present; runtime wiring still pending.
- PASS 26 Android-free portal compile/audit: PASS (previous run).
- PASS 27 Android-free compile: PASS.
- PASS 27 `WorldSpatialLayoutAudit`: PASS.
- Full Gradle/APK/runtime screenshot: Director-owned and not verified in this World branch.

## Current blockers / remaining P0

- Runtime integration is still required before camera scrolling, tap movement and spatial road/plaza presentation become visible in APK.
- Original Milles TILE/OBJECT/COLLISION geometry is still blocked on verified visual identification/calibration; current prototype must not be promoted to original geometry.
- South portal destination map + spawn ID remains evidence-blocked.
