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
