# PROJECT DARK Visual handoff

## 2026-09-14 — ATTACK body/robe + SW/SE robe registration repair

Authoritative base main: `cbe9268f2d873d2b52160ff56a7c2de29aa6273e`
Worker branch: `agent/visual/20260914-0910`

### Latest device failures addressed
- ATTACK: static source-IDLE BODY/robe with only `mw001` swing is forbidden.
- WALK: SW/SE `mu0000058` registration remains broken; NW/NE is device-natural and must be preserved.

### Implemented runtime delta
- Actual mm001 group-02 ATTACK source is normalized through translation + vertical source crop, not rejected because its raw alpha is taller than the accepted IDLE geometry.
- One common presentation scale only: `PLAYER_RENDER_SCALE=1.70`, `SOURCE_PRESENTATION_SCALE=1.70/1.50`; no action-only scale.
- Equipped FULL_BODY `mu0000058` is drawn from the matching group-02 source in the same source-action composition as BODY; equipped `mw001` stays synchronized in that route.
- Static IDLE fail-safe cannot run weapon SWING (`attackFallbackWeaponSwingReachable() == false`), eliminating the reachable weapon-only attack animation path.
- Previous canonical target-facing mapping and body mirror mapping are unchanged.
- NW/NE robe X registration table is unchanged.
- SW/SE robe uses actual selected-frame BODY/robe alpha bounds to derive X center correction and Y foot-bottom correction. Both layers use the exact same direction, atlas column, and presentation clock.
- HIT/HURT/FLINCH pose remains disabled; WALK remains 0.12 s/frame and 0.48 s/cycle; attack trail ghosts remain zero.

### Evidence contract
`CharacterVisualEvidenceProbeV2.collect(Resources)` produces actual packaged-source evidence:
- 4 directions × 5 robe IDLE/WALK rows: BODY bounds, robe bounds, correction X/Y, registered center delta, registered foot delta.
- 4 mm001 group-02 attack geometry rows: IDLE bounds, raw action alpha, crop, translation, resulting height/foot/center errors.
- 4 BODY + FULL_BODY robe + mw001 readiness/atomicity rows and no-weapon-only-fallback assertion.
- 4 mw001 attack transform rows: independent mirror, hand anchor, transformed blade tip, angle and tip distance.

Thresholds: normalized BODY height error <=1 px, foot <=1 px, center <=2 px.

### Status
IMPLEMENTED on worker branch. BUILD VERIFIED / DEVICE VERIFIED / VISUAL ACCEPTED are not claimed by Visual. Fresh APK/device validation remains required before the reopened gate can close.
