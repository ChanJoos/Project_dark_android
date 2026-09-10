# DEV HISTORY — PASS 40 WORLD LIVE INTEGRATION

## Trigger
User explicitly prioritized actual on-device tile-map integration over further hidden map production after confirming that the tested APK still showed no live tile map.

Latest canonical amendment: `design/PLAYTEST_CANON_20260910_1938.md`.

## Implemented
- Removed the live stretched-screenshot path from `GameView` startup/draw flow.
- `GameView` no longer downloads `WorldDef.VISUAL_SOURCE_URL` for runtime map presentation.
- Added `WorldLiveMapLayer` and wired `AdaptedMillesMapRenderer` into the actual `onDraw()` path before NPC/monster/player rendering.
- Kept dynamic entities on the existing world-space camera transform while synchronizing the live-map camera to the same player follow/clamp behavior.
- Empty-map/NPC/monster world hit conversion now uses the live-map screen→world transform.
- Replaced broad `isHudSurface()` interception rectangles with the actual visible party/quest/target/minimap/chat/player-status panels plus individual utility icons. Combat/joystick controls remain handled by their existing precise hit tests.
- Tap movement still uses the existing authoritative `WorldMoveTargetController` and `RuntimeState.tryMove`; no teleport path was introduced.

## Canon safety
- Whole gameplay screenshot remains reference-only and is no longer used as the live map texture in this branch.
- Tile/object renderer remains `[ADAPTED]/[B]` where original Milles geometry/art is unverified.
- No new original-game geometry or asset claim was invented.

## Verification state
- IMPLEMENTED: yes.
- BUILD VERIFIED: pending CI/Gradle check.
- RUNTIME VERIFIED: no; requires new APK/device screenshot and canonical 10-point empty-map tap test.

## Acceptance for Director
1. Build this branch/PR.
2. Confirm the first frame shows renderer-built tile/object village content rather than the stretched source screenshot or black reachable canvas.
3. Walk with joystick and verify map scroll/entity alignment.
4. Tap at least 10 empty-world points across center/left/right/top/bottom safe regions; marker + WALK expected unless blocked/unreachable.
5. Verify NPC/monster/control/modal touch priority remains intact.
