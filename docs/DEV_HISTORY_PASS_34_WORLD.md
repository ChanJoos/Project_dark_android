# DEV HISTORY — PASS 34 WORLD

## Goal
Stop adding isolated navigation audits and close the World→runtime integration gap while beginning actual map construction.

## Implemented
- Added `WorldMapProjection`: one read-only projection for bounds, spawn, collision, NPCs, monsters, portals, object markers, exploration anchors, map surfaces and structures.
- Added `WorldRuntimeAdapter`: implements move-target NavigationWorld + Walker against the live `RuntimeState`, owns camera transform, screen↔world conversion, empty-ground tap movement, NPC approach, movement cancellation, WALK→camera follow ordering, and portal-overlap snapshot.
- Added `AdaptedMillesMapLayer`: first renderable `[ADAPTED]/[B]` village composition with GROUND/ROAD/PLAZA/GATE surfaces and 11 structure footprints. This is the start of the actual map, not another test-only audit.
- `GameView.java` remains untouched; Director/UX can integrate through the adapter.

## Evidence safety
Original Milles geometry remains unverified. All authored surfaces/structures remain `[ADAPTED]/[B]` and replaceable zone-by-zone when Nexon/source geometry is calibrated. No screenshot is used as a monolithic final map texture.

## Verification
- IMPLEMENTED: yes, source commits present on World branch.
- BUILD VERIFIED: not claimed in this pass.
- RUNTIME VERIFIED: pending Director integration/device playtest.

## Next World work
Continue actual map production: convert the prototype surface/structure layer into visible isometric tile/object composition, then progressively replace adapted zones with source-backed Milles reconstruction as evidence becomes available.
