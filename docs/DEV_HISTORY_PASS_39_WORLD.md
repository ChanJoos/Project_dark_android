# DEV HISTORY — PASS 39 WORLD / PLAYTEST CANON RESPONSE

## Trigger
Latest main `d0fb8aa5a7d4093a18387c9ca8b242df651ca955` adds canonical amendment `design/PLAYTEST_CANON_20260910_1938.md` from the user's device playtest.

World-relevant superseding findings:
- live map presentation failed because GameView still stretches the screenshot referenced by `WorldDef.VISUAL_SOURCE_URL` over expanded world bounds;
- screenshot must become reference-only;
- live TILE/OBJECT renderer must be used instead, without black reachable gaps;
- empty-map tap remains runtime-failed; Director inspection attributes a major cause to broad `GameView.isHudSurface(...)` interception rectangles.

## World-owned response
Added `WorldLiveMapLayer` as the single live Milles presentation/navigation integration surface.

It owns and exposes:
- `WorldRuntimeAdapter`
- `AdaptedMillesMapRenderer`
- `draw(Canvas)` for canonical TILE+OBJECT live rendering
- `tick(dt)` for WALK then camera follow
- `requestGroundTap(screenX,screenY)` for empty visible world taps
- separate `requestNpcApproach(npcId)`
- world/screen transforms and input cancellation

The class explicitly documents that whole gameplay screenshots are reference-only and must not be stretched as live map textures.

## Ownership boundary / blocker
The remaining root runtime integration fixes are in `GameView.java`, which World is explicitly forbidden to edit:
1. remove stretched screenshot rendering from the live world path;
2. instantiate/use `WorldLiveMapLayer` before dynamic entities;
3. replace broad HUD interception rectangles with precise visible-control/modal hit regions so valid empty-map taps reach `requestGroundTap`.

These are Director/UX integration actions, not duplicated inside World.

## Verification
- IMPLEMENTED: yes.
- BUILD VERIFIED: not claimed in this World pass.
- RUNTIME VERIFIED: no; latest user playtest explicitly says map and empty-map tap are failed until a new APK confirms them.
- No GameView/Character/Combat/RPG/HUD/quest-owned files modified.
