# World handoff — PASS 32

- Branch: `agent/world/20260910-1752`
- Base: `main@6785efb6f7504e070ee0c0aa6924d1281e444469`
- Geometry: current Milles expansion remains `[ADAPTED]/[B]`; original geometry still unverified.

## P0 defect fixed
`WorldMoveTargetController` previously searched a 16-unit lattice but required the lattice node itself to enter the 6-unit default ground tolerance. Valid off-grid taps can be ~11.31 units from the nearest lattice node, so reachable taps could exhaust 4096 A* expansions and incorrectly become `BLOCKED`.

GROUND search now uses a lattice-entry tolerance of `max(requestTolerance, half-cell diagonal + epsilon)` only when the exact target is occupiable, then appends that exact target as the final waypoint. The final leg is still executed through `Walker.tryWalkStep`, so collision remains authoritative and there is no teleport. NPC approach semantics are unchanged.

## Exploration route regression
`WorldMoveTargetExplorationAudit` drives the real controller through:
`spawn → north_cross → central_plaza → west_district → central_plaza → east_district → south_east_lane → south_gate → south_portal`.
It also asserts that a tap inside a blocker returns `BLOCKED` without moving the walker.

Occupancy-safe approach anchors carried forward:
- west district `(320,705)`
- east district `(1350,715)`
- south gate `(790,1000)`
These avoid direct overlap with the west NPC, combat dummy and gate NPC respectively.

## Verification
Equivalent A* model before fix exhausted 4096 expansions for several valid anchors. After fix representative searches converge in 12–49 expansions. Full Gradle/APK/runtime remains Director-owned and unverified in this World pass.

## Director / UX integration request
- Empty eligible map tap: UI ownership reject → `screenToWorld` → `requestGroundMove`.
- NPC taps must continue using `requestNpcApproach`; do not collapse both semantics.
- Integrate this controller/session rather than reimplementing path logic in `GameView.java`.
- Keep south portal target pending/fail-closed until verified map + spawn identity exists.

No `GameView.java`, CharacterRenderer, Combat, RPG, HUD or quest files were modified.
