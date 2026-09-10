# PASS 41 — WORLD TILE-LOCKED MOVEMENT

Authority: `design/PLAYTEST_CANON_20260910_2149.md`.

## Why
The old `WorldMoveTargetController` used a 16-unit cardinal lattice plus free-pixel interpolation/final arbitrary point. That conflicts with the corrected canon: exactly four gameplay directions `NW/NE/SW/SE`, one logical step equals one adjacent 64×32 isometric tile, no accumulated drift.

## Implemented
- Added `IsometricTileMovementController` using the authored `AdaptedMillesIsometricTileLayer` tile centers as graph nodes.
- Canonical adjacent deltas are exactly `NW(-32,-16)`, `NE(+32,-16)`, `SW(-32,+16)`, `SE(+32,+16)`.
- Arbitrary tap resolves to nearest traversable authored tile center.
- A* returns only adjacent tile-center nodes; no exact arbitrary tap point is appended.
- Logical runtime step consumes one whole adjacent tile; no `walkSpeed*dt` free-pixel accumulation exists in this controller.
- `WorldRuntimeAdapter` now exposes tile movement and direct step direction to Character/UX consumers.
- Adapter preflights X intermediate, Y intermediate and destination before calling axis-resolved `RuntimeState.tryMove`, preventing half-step drift.
- Legacy off-grid prototype spawn is normalized once when the World adapter is created; gameplay commands themselves remain tile-only.
- Added deterministic contract audit for opposite-direction round trips, 10-step zero drift and every tap path segment being a canonical adjacent delta.

## Integration blocker remaining
`GameView`/UX direct joystick movement must stop bypassing World with arbitrary `state.tryMove(v*speed*dt)` vectors. UX should issue `WorldLiveMapLayer.step(Direction)` discrete commands and use `lastStepDirection()` for Character facing. This file is outside World ownership.

## Verification status
- IMPLEMENTED: yes.
- BUILD VERIFIED: not claimed in this pass.
- RUNTIME VERIFIED: no; requires Director integration + device test.
