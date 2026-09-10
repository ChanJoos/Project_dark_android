# DEV HISTORY — PASS 32 WORLD

## Source gate
Started from latest `main@6785efb6f7504e070ee0c0aa6924d1281e444469`. PASS 30 expanded village is already on main. PASS 31 occupancy-safe anchors had not landed, so those three approach-point corrections are carried forward here.

## Defect found
`WorldMoveTargetController` used a 16-unit navigation lattice with a default ground target tolerance of 6 logical units. An arbitrary valid tap can be up to ~11.31 units from its nearest lattice node. Therefore valid off-grid ground targets could never satisfy the A* goal condition, causing search to run to `MAX_EXPANSIONS=4096` and incorrectly return `BLOCKED`.

This is a P0 tap-to-move defect, especially visible after the village footprint expansion.

## Implemented
- Kept NPC approach tolerance semantics unchanged.
- For an occupiable GROUND target, A* search now accepts a lattice node within max(request tolerance, half-cell diagonal + epsilon), then appends the exact world target.
- Final movement still uses `Walker.tryWalkStep`; no teleport and no collision bypass.
- Carried forward occupancy-safe exploration targets: west `(320,705)`, east `(1350,715)`, south gate `(790,1000)`.
- Added `WorldMoveTargetExplorationAudit` driving the real controller from spawn through north/plaza/west/east/south/portal route and asserting a blocked structure target terminates as BLOCKED.

## Verification
Independent equivalent A* run before fix: several valid anchors exhausted 4096 expansions and failed. After fix: north 24, plaza 16, west 37, plaza return 37, east 49, south-east 38, south gate 16, portal 12 expansions.

- IMPLEMENTED: yes.
- ISOLATED MODEL VERIFIED: yes.
- FULL JAVA/GRADLE BUILD VERIFIED: not run in this connector session.
- APK/RUNTIME VERIFIED: Director-owned and pending.
