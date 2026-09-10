# DEV HISTORY — PASS 31 WORLD

## Lineage
- Base: PASS 30 head `aae160117dc391e33c82d89ec7d9a2eac8ad935a`, itself based on latest `main@405bd764dd38146304fb3939709cf0def17f6958`.
- Geometry remains `[ADAPTED]/[B]`; no original Milles geometry or portal destination is asserted.

## Runtime gap found
The PASS 30 exploration contract exposed three named navigation targets directly on occupied runtime entity coordinates:
- `west_district` overlapped `milles_west_proto` NPC `(285,705)`.
- `east_district` overlapped `combat_dummy_01` monster `(1315,715)`.
- `south_gate` overlapped `milles_gate_proto` NPC `(790,1035)`.
These points were meaningful labels but invalid generic ground move targets under current RuntimeState occupancy rules.

## Implemented
- Moved the three exploration anchors to nearby walkable approach points: west `(320,705)`, east `(1350,715)`, south gate `(790,1000)`.
- Added `WorldExplorationOccupancyAudit`.
- Audit mirrors current player collision footprint and static runtime entity occupancy, then flood-fills the expanded map on the same 16-unit / 4-neighbour navigation lattice used by the world path contract.
- Requires every stable exploration anchor to be directly occupiable and to have a reachable lattice point within 20 logical units from spawn.

## Verification
- Independent coordinate/flood-fill verification in this run: 4,602 reachable lattice cells; all 9 anchors occupiable and reachable.
- Repository Gradle/APK: not run by World agent.
- Android runtime: not verified; Director integration/playtest required.

## User-visible delta
Tap/world navigation targets for the west district, east district and south gate no longer resolve to an NPC/monster-occupied point, preventing immediate blocked-target behavior when these anchors are used by runtime navigation or debug traversal.
