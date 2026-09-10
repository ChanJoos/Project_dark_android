# DEV HISTORY — PASS 31 WORLD

## Base
Fresh branch from latest main after Director integrated PASS 30 expanded village. Geometry remains `[ADAPTED]/[B]`.

## Runtime defect found and fixed
Three stable exploration targets overlapped runtime entities and were invalid generic ground-move targets:
- west district overlapped `milles_west_proto` at `(285,705)` → approach target moved to `(320,705)`.
- east district overlapped `combat_dummy_01` at `(1315,715)` → approach target moved to `(1350,715)`.
- south gate overlapped `milles_gate_proto` at `(790,1035)` → approach target moved to `(790,1000)`.

Added `WorldExplorationOccupancyAudit` using current `WorldDef` blockers and `RuntimeState` player/NPC/monster radii. It flood-fills a 16-unit, four-neighbour navigation lattice and requires every exploration anchor to be occupiable and reachable from the spawn-connected component within 20 logical units.

## Verification
Independent traversal-model run for the exact fixed coordinates: 4,602 reachable lattice cells; all 9 anchors directly occupiable and reachable.

BUILD VERIFIED: pending Director Gradle/APK gate.
RUNTIME VERIFIED: pending Android device playtest/screenshot.
South portal remains `PENDING_TARGET_MAP`; no destination fabricated.
