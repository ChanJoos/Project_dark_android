# World handoff — PASS 41 TILE-LOCKED MOVEMENT

- Branch: `agent/world/20260911-0101`
- Base main: `9639a7004c55cc5b5e2e770ee6cfa6fe16dae0c9`
- Authority: `design/PLAYTEST_CANON_20260910_2149.md`
- Map expansion remains paused until this movement contract is integrated and device-verified.

## Superseding movement implementation

The old 16-unit/free-pixel `WorldMoveTargetController` no longer defines the accepted movement path.

World now provides `IsometricTileMovementController`:
- gameplay directions only: `NW / NE / SW / SE`;
- exact adjacent deltas for the current 64×32 projection: `(-32,-16) / (+32,-16) / (-32,+16) / (+32,+16)`;
- arbitrary tap resolves to nearest traversable authored tile center;
- A* path contains only adjacent authored tile centers;
- no arbitrary exact tap-point final leg;
- one logical runtime operation consumes exactly one adjacent tile;
- `lastStepDirection()` exposes World direction directly for Character-facing integration.

`WorldRuntimeAdapter` now uses this controller and preflights both axis intermediates plus destination before calling the existing axis-resolved runtime move, preventing partial half-tile drift. Legacy off-grid prototype spawn is normalized once at adapter creation only.

## Verification encoded

`IsometricTileMovementAudit` checks:
1. NW -> SE exact round trip;
2. NE -> SW exact round trip;
3. 10-step repeated opposite-pair travel returns to exact origin;
4. arbitrary tap path consists only of canonical `(±32,±16)` tile segments;
5. final path position equals resolved target tile center.

BUILD VERIFIED and device RUNTIME VERIFIED are not claimed by World yet.

## Director / UX P0 wiring

`GameView.java` remains outside World ownership. It currently still has a direct joystick/free-pixel path and therefore can bypass the corrected World contract.

Required integration:
1. stop direct `state.tryMove(vx*speed*dt, vy*speed*dt)` player movement for joystick/touch travel;
2. map joystick intent to exactly one of `IsometricTileMovementController.Direction.NW/NE/SW/SE`;
3. issue movement through `WorldLiveMapLayer.step(direction)`;
4. for tap travel call `requestGroundTap(...)`, then consume one World path step at a time;
5. Character facing must consume `lastStepDirection()` rather than infer a separate cardinal/free vector;
6. after integration run the canonical one-step, opposite round-trip, 10-step zero-drift, camera-invariance and tap-path-adjacency device checks.

## Next World priority

Do not expand bounds or add more debug buildings. After Director confirms tile-locked movement on device, rebuild one coherent village slice where ground, buildings, props, entrances and collision share one isometric projection language.
