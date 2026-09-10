# World handoff — PASS 25

- Base lineage: `main@3b58a8d08805cf9031466fe0c50d56e17aa1344d` + World PASS 24 head `dfd58b0abe5b45d076377d37fe6b46c559d9d5d9`
- Master: M001 / current 92-CSV baseline (no Master values changed)
- Branch: `agent/world/20260910-1455`
- Canonical geometry status: original Milles geometry still unverified; this pass uses explicitly tagged `[ADAPTED]/[B]` prototype village geometry only.

## Implemented in PASS 25

### 1. Explorable village footprint
- Expanded `WorldDef` logical bounds from the previous small field to `96..1184 x 64..864`.
- Re-authored collision fixtures as an `[ADAPTED]/[B]` village prototype with:
  - north building row,
  - central plaza,
  - east/west structure framing,
  - lower lanes,
  - south gate route,
  - small plaza landmark blockers.
- Added three prototype NPC placement points for traversal/approach testing while keeping names/content explicitly `[B]`.
- Moved the combat dummy away from the central plaza so village traversal can be judged independently from combat pressure.
- Added explicit object/spatial anchors and one south-exit portal anchor. The portal destination remains `PENDING_TARGET_MAP` and is marked `PROTOTYPE_DISABLED_TARGET_PENDING`; no original destination is fabricated.
- All TILE / OBJECT / COLLISION / NPC / MONSTER_SPAWN / PORTAL layer statuses remain explicit and replaceable by source-backed data later.

### 2. Camera/world coordinate contract
Added `world/WorldCameraTransform.java`:
- camera follow with configurable anchor/dead-zone,
- world-to-screen projection,
- screen-to-world inverse conversion for tap targeting,
- camera clamp against map bounds,
- camera offset affects presentation/input projection only and never mutates logical collision coordinates.

Added `world/WorldCameraTransformAudit.java`:
- world→screen→world round-trip,
- no camera jitter inside dead-zone,
- camera movement/world-scroll direction outside dead-zone,
- min/max map clamp.

## Integration request (Director / UX owner)

`GameView.java` was intentionally not modified.

1. Construct one `WorldCameraTransform` from `WorldDef` bounds and current drawable world viewport (exclude HUD-only regions only if the Director chooses a reduced viewport contract).
2. On boot/respawn/map transition call `snapTo(player.x, player.y)`; after logical player movement call `follow(player.x, player.y)`.
3. Project TILE/OBJECT/NPC/MONSTER/PORTAL world positions through `worldToScreen`. Keep HUD/joystick/action buttons in screen-space.
4. Before generic world tap movement, reject UI-owned touches, then call `screenToWorld` and pass the resulting world point to `WorldMoveTargetController.requestGroundMove`.
5. NPC hit-testing must retain priority and use `requestNpcApproach`; do not leak NPC taps into generic ground move.
6. Do not activate `milles_south_exit_proto` transition until Director supplies an evidence-safe target-map contract. The portal anchor is currently a traversal landmark/contract fixture only.

## Validation

- GitHub writes completed successfully.
- Android-free camera code includes deterministic `WorldCameraTransformAudit`.
- Local clone/compile could not be executed in this worker because the execution container cannot resolve `github.com`; this is an environment blocker, not a compile failure.
- Full Gradle/APK/runtime verification remains Director-owned and pending integration.

## Remaining P0

- Director/UX runtime wiring of PASS 24 move-target + PASS 25 camera transform.
- Real Android runtime screenshot/device verification that the player remains near the dead-zone while the expanded world scrolls beneath it.
- Portal transition callback/DTO once an evidence-safe destination definition exists.
- Source-backed Milles TILE/OBJECT/COLLISION reconstruction remains pending visual identification/calibration; this adapted village must be replaced incrementally as verified geometry becomes available.


---

# World handoff — PASS 26 portal transition lifecycle

- Branch: `agent/world/20260910-1455`
- Code commit: `55e0846ca19b1abd2fcfda6e777b0cb70d9f91e7`
- Canonical safety: `milles_south_exit_proto` remains target-pending and must not change maps.

## New world API

`WorldPortalTransitionController` owns activation and transition request semantics:

1. UX/runtime identifies the closest portal and calls `observe(portal, playerWorldX, playerWorldY)` once per world tick.
2. `BLOCKED_TARGET_PENDING` and `BLOCKED_DISABLED` are terminal presentation outcomes for the current overlap; they emit no transition request.
3. On `REQUESTED`, the injected `TransitionSink` owns map loading. The injected move-target canceller is called only after that sink accepts the request.
4. Repeated ticks inside the same portal return `PENDING` or `LATCHED`; they never emit duplicate requests.
5. The runtime calls `complete(requestId, success, detail)`. Unknown/late request IDs return `STALE_COMPLETION` and cannot mutate controller state.
6. Call `observeOutside()` only when no portal is in activation range. Leaving and re-entering is required before a rejected/completed/blocked portal can retry.

## Director / UX integration request

- Adapt `WorldDef.PortalSpawn` to the new immutable `Portal` DTO without duplicating range/readiness logic in `GameView.java`.
- Map current status `PROTOTYPE_DISABLED_TARGET_PENDING` to `PortalReadiness.TARGET_PENDING`.
- Show brief locked/unavailable feedback for `BLOCKED_TARGET_PENDING`; do not teleport or silently choose another map.
- When a verified destination exists, register both stable `targetMapId` and stable `targetSpawnId`. Arrival coordinates remain destination-map-owned.
- Transition acceptance must cancel active tap/NPC approach movement through the supplied canceller, clear transient target selection as appropriate, load the destination, then complete the same request ID.
- After map load, create/snap the destination camera transform before accepting new screen-to-world taps.

## Validation and blocker

- Android-free compile: PASS.
- `WorldPortalTransitionAudit`: PASS.
- Full Gradle/APK: not verified in this worker.
- Android runtime: not verified.
- Concrete content blocker: no verified target map + arrival spawn is available for the adapted Milles south gate, so the on-screen transition intentionally remains disabled.
