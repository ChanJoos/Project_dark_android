# World handoff — PLAYABLE VILLAGE M2

- Active branch / PR: `agent/world/playable-village` / `#82`
- Base main: `e04b758745c41ecef4cdc2a810846af7bcda3762`
- Current World code head before this handoff: `765c64e74f881cef85102c0d62a1e0823bddc44b`
- Canon: `design/PLAYTEST_CANON_20260910_2149.md` plus latest user direction.
- Evidence: all new village geometry/presentation remains `ADAPTED/B`; source art remains `PENDING_CROP`.

## M1 movement disposition

Tile-locked movement is already integrated on main through `b8f36e6787267faca86a66bfe863713afb6c4b9e`. Do not reimplement it in this branch. Joystick/tap/NPC/monster approach share the World tile-step path and World supplies NW/NE/SW/SE facing.

## M2 visible result in PR #82

The opening-camera village now has three structures on the coherent isometric renderer path instead of one:

- `west_house`: 2:1 footprint `(256,528)–(384,592)`, center `(320,560)`.
- `plaza_landmark_a`: SHOP footprint `(672,480)–(800,544)`, center `(736,512)`.
- `plaza_landmark_b`: HOUSE footprint `(800,608)–(928,672)`, center `(864,640)`.

All three are supplied by `AdaptedMillesIsoBuildingLayer` and consumed by the existing `AdaptedMillesMapRenderer.drawObjects(...) -> drawIsometricBuilding(...)` branch. No new `GameView` wiring is required. Each renders through the same projected diamond footprint, two wall faces, hip-roof faces, face-aligned door/window, shadow and threshold path.

`AdaptedMillesMapLayer` structure bounds and `WorldDef.blockers()` were updated to the same footprints, so visual footprint and runtime collision remain aligned. `AdaptedMillesEntranceLayer` automatically consumes each iso building's door foot and approach tile.

## Verification contract

`AdaptedMillesIsoBuildingAudit` now requires exactly three coherent iso buildings and verifies for every building:

- 2:1 `halfWidth == halfDepth * 2` projection.
- map-structure bounds == iso collision bounds.
- non-WALL/non-LANDMARK structure kind.
- entrance door/approach values == iso building values.
- approach resolves to an authored tile and lies outside the building footprint.
- center is inside and the collision-corner is outside the diamond footprint.

BUILD VERIFIED and RUNTIME VERIFIED are not claimed until GitHub Actions / Director APK-device gate completes.

## Exact integration surface

- Data: `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsoBuildingLayer.java`
- Structure geometry: `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapLayer.java`
- Runtime collision: `app/src/main/java/com/projectdark/mobile/WorldDef.java`
- Entrance adapter: `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesEntranceLayer.java`
- Renderer: `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesMapRenderer.java`
- Audit: `app/src/main/java/com/projectdark/mobile/world/AdaptedMillesIsoBuildingAudit.java`

## Next one World result

Do not expand the map or revisit movement. Add collision-aware tree/fence/prop grouping around these three opening-screen buildings and road/plaza edges so the first screen reads as one finished village composition. Then hand the branch to Director for APK/device visual acceptance.
