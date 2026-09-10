# World handoff — PASS 28

- Branch: `agent/world/20260910-1556`
- Draft PR: #30
- Base lineage: `main@3b58a8d08805cf9031466fe0c50d56e17aa1344d` + World PASS 27 head `ff41396a81e7779fc808911876d4de96ca384be4`
- Master: M001 / current 92-CSV baseline; no canonical Master values changed.
- Geometry status: original Milles geometry remains unverified. Current village/collision/spatial semantics remain `[ADAPTED]/[B]` prototype data only.

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
- prototype entries remain `ADAPTED/B` and replaceable with verified Milles geometry

### Collision-connected traversal gate — PASS 28
- Added `WorldTraversalConnectivityAudit`.
- It consumes the actual `WorldDef.blockers()` collision rectangles plus the PASS 27 spatial anchors.
- Flood-fill uses the same 16 logical-unit navigation grid and the runtime player collision radius.
- Required connected anchors: `spawn`, `plaza_center`, `north_cross`, `service_npc_approach`, `west_lane`, `east_lane`, `south_gate_approach`, `south_portal`.
- The target-pending south portal must remain physically approachable even while transition remains disabled.
- West/east lower-lane anchors must both remain in the same reachable component so future collision edits cannot silently collapse the alternate village circulation route.

## Director / UX integration request

`GameView.java` remains untouched by the World agent. Do not duplicate these algorithms there.

1. Construct one `WorldCameraTransform` using active map bounds and drawable world viewport.
2. On boot/respawn/transition, snap camera to player; after logical movement, call camera follow.
3. Render TILE/OBJECT/NPC/MONSTER/PORTAL and PASS 27 spatial regions in world-space through the camera transform. HUD remains screen-space.
4. For world taps: first reject UI-owned touches, convert screen→world, then dispatch generic ground move. NPC hit-test retains priority and dispatches NPC approach instead.
5. Consume `AdaptedMillesVillageLayout.create()` for prototype road/plaza/gate semantics rather than hard-coding those positions in view code.
6. Observe portal overlap through `WorldPortalTransitionController`. `milles_south_exit_proto` must show unavailable/locked feedback while target map/spawn remains unverified.
7. Add `WorldTraversalConnectivityAudit` to the existing World build/regression audit set. Any future collision/layout replacement that disconnects a required route should fail integration unless the adapted spatial contract is intentionally revised with evidence.

## Verification

- Latest `main` governance/design/backlog files were re-read before PASS 28 coding.
- Independent collision flood-fill against current `WorldDef` geometry: all eight required anchors are in one reachable component.
- Code committed before Draft PR creation as required.
- Draft PR #30 created from per-run branch before this handoff update.
- Full Gradle/APK/runtime screenshot: Director-owned and not verified in this World branch.

## Current blockers / remaining P0

- Runtime integration is still required before camera scrolling, tap movement and spatial road/plaza presentation become visible in APK.
- Original Milles TILE/OBJECT/COLLISION geometry is still blocked on verified visual identification/calibration; current prototype must not be promoted to original geometry.
- South portal destination map + spawn ID remains evidence-blocked.
- PASS 28 now prevents future prototype collision edits from silently turning visible village routes into unreachable space.
