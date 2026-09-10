# World handoff — PASS 40 LIVE TILE MAP INTEGRATION

- Branch: `agent/world/20260910-2006`
- Base: `main@200edd0aa1a3fcbd3eacbe32c8056bc8357b2137`
- Trigger: user explicitly prioritized actual APK map visibility over further hidden map production.
- Latest authority: `design/PLAYTEST_CANON_20260910_1938.md`.

## Canon delta addressed
The latest device playtest marks live map presentation and empty-map tap as P0 runtime failures. The tested `GameView` still downloaded `WorldDef.VISUAL_SOURCE_URL` and stretched that screenshot over the whole expanded world, while broad HUD interception rectangles swallowed visible-world taps.

## Runtime-visible delta
- `GameView` no longer downloads or draws the source screenshot as the live world texture.
- `WorldLiveMapLayer` is instantiated in the actual runtime and `liveMap.draw(canvas)` executes before dynamic NPC/monster/player rendering.
- Live map camera follows the same player and bounds as the existing dynamic-entity camera.
- The existing TILE/OBJECT renderer is therefore on the actual frame path, not only present as unused World code.
- Empty map/NPC/monster world hit conversion uses the live-map screen→world transform.
- `isHudSurface()` now covers visible panels and actual utility icons instead of the previous large blanket rectangles. Existing joystick/combat control hit tests remain first-priority.

## Preserved semantics
- Tap movement still walks through `WorldMoveTargetController` → `RuntimeState.tryMove`; no teleport.
- NPC and monster touch priority remains above generic empty-world movement.
- Collision/world coordinates remain independent from render scale/camera.
- Unverified Milles tile/object presentation remains `[ADAPTED]/[B]` / `PENDING_CROP`.

## Integration exception
The normal World ownership rule forbids direct `GameView.java` edits. In this pass the user explicitly instructed us to perform the missing live runtime integration after identifying that this ownership boundary had left the map invisible all day. The edit is intentionally minimal and limited to World presentation/touch plumbing; Combat/RPG/Character behavior was not redesigned.

## Verification
- IMPLEMENTED: yes.
- BUILD VERIFIED: pending CI/Gradle result.
- RUNTIME VERIFIED: no.

## Device acceptance required
1. First frame shows renderer-built tile/object village, not stretched screenshot/black reachable gaps.
2. Joystick movement keeps map/NPC/monster/player aligned while camera scrolls.
3. 10 representative empty-world taps across safe screen regions show marker and WALK unless genuinely blocked.
4. NPC/monster/control/modal touches retain priority.
5. Capture actual Android screenshots from central village, east market and south gate.

## Next World work
Do not expand hidden map content until this pass is built and visually confirmed. If the runtime map is visible, continue source-backed/adapted district production from that visible baseline.
