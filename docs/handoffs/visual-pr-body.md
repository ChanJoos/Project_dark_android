## Visual runtime repair — attack body/robe + SW/SE robe WALK

Base: `cbe9268f2d873d2b52160ff56a7c2de29aa6273e`

### Runtime fixes
- Replace geometry-rejection path that produced static IDLE BODY/robe + mw001-only swing.
- Normalize actual mm001 group-02 BODY by translation + vertical source crop at common scale 1.70.
- Keep matching mu0000058 FULL_BODY group-02 source atomic with BODY and synchronize mw001 in the same source-action route.
- Disable weapon SWING on static paper-doll fail-safe; weapon-only attack animation is unreachable.
- Preserve previously device-passed player target-facing attack direction mapping.
- Preserve NW/NE robe registration.
- SW/SE robe registration now derives per-frame X center + Y foot-bottom corrections from actual runtime WebP alpha bounds while sharing BODY direction/frame/walk clock.
- Preserve HIT/HURT/FLINCH disabled, WALK 0.12 s/frame / 0.48 s/cycle, zero attack trail/ghost.

### Evidence
Adds `CharacterVisualEvidenceProbeV2` to measure packaged runtime sources and emit:
- robe registration: 4 dirs x 5 frames,
- attack geometry: 4 actual group-02 sources after normalization,
- robe/body/weapon attack atomic readiness: 4 dirs,
- mw001 hand/blade-tip transform: 4 dirs.

Thresholds: height <=1 px, foot <=1 px, center <=2 px. No action-only scale.

### Status discipline
IMPLEMENTED on worker branch. CI/build and fresh APK/device validation remain separate. This PR does not claim DEVICE VERIFIED or VISUAL ACCEPTED.
