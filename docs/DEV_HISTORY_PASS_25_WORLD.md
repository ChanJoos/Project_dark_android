# PROJECT DARK — DEV HISTORY PASS 25 · WORLD / MAP ENGINE

Date: 2026-09-10
Role: World · Map Engine
Branch: `agent/world/20260910-1455`

## Source-of-Truth gate

Read before coding:
- `AGENTS.md`
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `docs/DIRECTOR_BACKLOG.md`
- current World DEV_HISTORY lineage
- World PASS 24 draft-PR handoff

Current constraints preserved:
- World owns map geometry, collision, camera transform, path/portal contracts.
- `GameView.java` is not modified by this worker.
- original Milles geometry remains unverified, so prototype geometry is explicitly `[ADAPTED]/[B]` and replaceable.
- whole screenshots remain reference evidence only, never final map textures.
- monster rewards remain direct inventory with no ground-pickup subsystem.

## Implemented

### Expanded adapted prototype village
`WorldDef` was expanded from a small test field to a larger traversal footprint (`96..1184 x 64..864`) with authored roads/openings around building-sized collision fixtures, central plaza space, south gate route, three prototype NPC placement points, one separated combat dummy spawn, object/spatial anchors and a disabled-destination portal anchor.

No original Milles geometry fact is asserted by these coordinates. `EVIDENCE_GEOMETRY=ADAPTED/B` and `GEOMETRY_STATUS=ADAPTED_PROTOTYPE_VILLAGE` make the temporary nature explicit.

### Stable camera transform API
Added `world/WorldCameraTransform` with:
- configurable player anchor,
- dead-zone follow,
- world-to-screen and screen-to-world transforms,
- map-bound camera clamp,
- renderer/input projection separated from logical movement/collision coordinates.

This is the world-side contract required for the Director/UX owner to wire map scrolling and tap movement without duplicating camera/path logic inside `GameView.java`.

### Deterministic camera audit
Added `WorldCameraTransformAudit` covering:
- projection round-trip,
- dead-zone stability,
- opposite-direction world scroll effect when camera follows movement,
- min/max clamp.

## Validation status

- IMPLEMENTED: yes.
- BUILD VERIFIED: not in this worker. Container network cannot resolve `github.com`, so local clone/compile could not be performed.
- RUNTIME VERIFIED: no. Director integration and Android runtime screenshot/device evidence are required.

## Integration handoff

See `docs/handoffs/world.md` PASS 25. Director/UX should wire the camera transform and PASS 24 move-target controller, while retaining NPC-tap priority and screen-space HUD ownership.

## Next World priority

1. Add portal transition callback/DTO semantics once a safe destination contract exists.
2. Continue source-backed Milles visual identification/calibration.
3. Replace adapted prototype geometry incrementally with verified TILE/OBJECT/COLLISION data while preserving the public world APIs.
