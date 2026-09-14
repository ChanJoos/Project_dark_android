# Visual handoff — 2026-09-14 attack + robe repair

Base: `cbe9268f2d873d2b52160ff56a7c2de29aa6273e`
Branch: `agent/visual/20260914-0910`

## Device feedback addressed
- ATTACK must not render a static IDLE BODY/robe while only `mw001` swings.
- SW/SE `mu0000058` WALK registration must be corrected in X and Y from actual source alpha while NW/NE remains unchanged.
- Previous player target-facing four-direction attack semantics must remain unchanged.

## Runtime changes
- `CharacterRenderer` now admits group-02 ATTACK after translation + vertical source crop normalization instead of rejecting the source because its raw alpha is taller than IDLE.
- Presentation scale remains common `1.70`; no attack-only scale was introduced.
- Equipped `mu0000058` uses the matching group-02 source in the same attack composition as BODY; `mw001` is drawn in the same source-action route.
- ATTACK fail-safe no longer calls weapon SWING over a static source paper doll. `attackFallbackWeaponSwingReachable()` is hard false.
- NW/NE robe registration retains the existing measured X table exactly.
- SW/SE robe registration now measures actual BODY/robe alpha for the selected frame and computes per-frame X center registration plus Y foot-bottom registration. BODY and robe still consume the exact same direction, atlas column, and presentation walk clock.
- HIT/HURT/FLINCH character pose remains disabled. WALK cadence remains 0.12 sec/frame, 0.48 sec/cycle. Ghost/trail count remains zero.

## Source evidence
`CharacterVisualEvidenceProbeV2.collect(Resources)` reads the packaged runtime WebPs and emits:
1. 4 directions × 5 IDLE/WALK robe registration rows including BODY bounds, robe bounds, X/Y correction, registered center delta and registered foot delta.
2. Four actual mm001 group-02 BODY rows including raw alpha bounds, source crop, translation, and resulting height/foot/center errors.
3. Four BODY+FULL_BODY robe+mw001 readiness/atomicity rows and explicit no-weapon-only-fallback status.
4. Four mw001 attack transforms at peak phase including independent weapon mirror, hand anchor, transformed blade tip, angle and tip distance.

Acceptance thresholds remain: height error <=1 px, foot error <=1 px, center error <=2 px, common presentation scale exactly 1.70.

## Status
IMPLEMENTED on worker branch. Static contract audit updated. GitHub CI/build and device validation are separate gates. Do not call DEVICE VERIFIED or VISUAL ACCEPTED until a fresh APK is tested by the user.
