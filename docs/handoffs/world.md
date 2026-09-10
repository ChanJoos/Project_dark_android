# World handoff — PASS 39 PLAYTEST CANON RESPONSE

- Branch: `agent/world/20260910-1956`
- Base: latest `main@d0fb8aa5a7d4093a18387c9ca8b242df651ca955`
- New authority: `design/PLAYTEST_CANON_20260910_1938.md` (USER-CONFIRMED / CANONICAL AMENDMENT)
- Geometry/visual status remains `ADAPTED/B`; exact original Milles geometry/art remain unverified/`PENDING_CROP`.

## Superseding World findings
The latest device playtest overrides prior assumptions that the live map path was effectively complete:
- live map is runtime FAILED because `GameView.drawWorld()` still stretches the screenshot referenced by `WorldDef.VISUAL_SOURCE_URL` across the expanded bounds;
- the screenshot must be reference-only, not a live texture;
- live village rendering must come from the World TILE/OBJECT renderer with no unexplained black reachable regions;
- empty-map tap-to-move is runtime FAILED; current Director inspection identifies broad `GameView.isHudSurface(...)` rectangles as a major interception cause.

## World-owned implementation
Added `WorldLiveMapLayer` as the canonical one-object integration surface.

It owns:
- `WorldRuntimeAdapter`
- `AdaptedMillesMapRenderer`

It exposes:
- `draw(Canvas)` — draw TILE + decoration + collision-aligned OBJECT village content;
- `tick(dt)` — WALK first, camera follow second;
- `requestGroundTap(screenX,screenY)` — empty visible world tap path;
- `requestNpcApproach(npcId)` — distinct NPC approach semantics;
- direct/action cancel plus world↔screen transforms.

The live-map surface intentionally has no screenshot-texture API.

## Director / UX REQUIRED integration
`GameView.java` is not World-owned, so the following must be done by Director/UX rather than duplicated here:
1. remove/disable stretched `WorldDef.VISUAL_SOURCE_URL` drawing from the live world path; keep the image only as reconstruction/reference evidence;
2. instantiate one `WorldLiveMapLayer(runtime, viewportWidth, viewportHeight)` for MILLES;
3. call `liveMap.draw(canvas)` before dynamic player/NPC/monster/portal rendering and do not overwrite it with the old screenshot/background path;
4. per frame call `liveMap.tick(dt)` and use the same `liveMap.world()` transform for every dynamic world entity;
5. eligible empty visible-world touch after precise UI/modal rejection → `liveMap.requestGroundTap(x,y)`;
6. NPC touch remains higher priority and routes to `liveMap.requestNpcApproach(id)`;
7. replace broad invisible HUD-blocking rectangles with actual visible control/modal hit regions;
8. device acceptance: 10 representative empty-map taps across center/left/right/top/bottom safe world areas, plus screenshots/traversal through central village/east market/south gate with no stretched screenshot or black reachable gaps.

## Existing map content preserved
The current World renderer already contains the expanded 2240×1552 `[ADAPTED]/[B]` village, 3,312 diamond tiles, roads/plazas/gate transitions, 20 structures, layered building silhouettes, 14 visual entrances and 38 decorations. This pass does not roll those back; it changes which path is authoritative for live presentation.

## Verification
- IMPLEMENTED: yes — canonical live-map integration surface added.
- BUILD VERIFIED: not claimed in this World pass.
- RUNTIME VERIFIED: NO. Latest user device playtest explicitly marks map presentation and empty-map tap as failed until a new APK passes the acceptance checks above.
- No `GameView.java`, CharacterRenderer, Combat, RPG, HUD or quest-owned file was modified.

## Next World work after Director wiring
Once the live renderer is actually visible on device, continue source-backed Milles reconstruction and authored-gap replacement. Do not spend another World pass polishing hidden renderer content before the live-path integration is confirmed.
