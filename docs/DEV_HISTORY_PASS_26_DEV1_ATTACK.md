# PROJECT DARK Dev History — Pass 26 / Dev 1 Attack

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IMPLEMENTED / VERIFICATION_PENDING`
- Runtime commit: `fca99853a137561ca40b3cce48a72b34aa372fc3`
- Base shared main consumed: `b3ab8d391f7a33c56d3f4623576892d355b8d919`
- PR: `#119`

## Inputs consumed
- `docs/PROJECT_STATE.yaml`
- `docs/AUTONOMY_BATON.md`
- current `CharacterRenderer.java`
- current `CharacterSemanticRig.java`
- current `CharacterSemanticRigEvidenceProbe.java`
- `CanonicalActorFacing.java`, `CanonicalMeleeTileContract.java`, and current `GameView.java` attack-facing path
- device regression recorded for exact-main PR #118 APK (`Screen_Recording_20260916_151528.mp4`)

## Change
The detached group-02 attack BODY is mirrored by `CharacterRenderer` for NW/SW after semantic anchors are derived. `CharacterSemanticRig` previously chose the west alpha extreme for NW/SW before that mirror, then `drawActionWeapon` mirrored the resulting handle again. This derives the dominant-hand semantic from the wrong source side for mirrored attack directions.

`CharacterSemanticRig` now derives the dominant hand in the unmirrored source orientation for detached action BODY bitmaps that the renderer will mirror. Idle/walk authored hand anchors remain unchanged. Renderer still performs the single final BODY/handle mirror.

## Reason
Fresh device evidence reports the mokdo slightly offset from the action hand. This repair removes a transform-order mismatch without adding arbitrary screen-space offsets, generated art, or fabricated temporal frames.

## Verification / evidence
- Static source trace: group-02 BODY uses `bodyMirrorX(NW/SW)` in `CharacterRenderer.drawSourceAction`; action weapon derives `dominantHand` before final mirror and mirrors the handle around `pose.x`.
- Scope is one runtime semantic-rig file; no combat legality, attack timing, robe registration, or source asset changes.
- Exact-head Android CI has not yet passed at the time of this record. Do not promote to `BUILD_VERIFIED`.
- No fresh APK/device evidence exists for this commit. `DEVICE_VERIFIED` and `VISUAL_ACCEPTED` remain false.

## Remaining risk
This commit addresses the mirrored-direction hand/weapon registration defect only. The same device gate still requires independent proof/fix for CONTACT scale continuity, robe visibility/composition, and canonical target-facing propagation. Do not hide those findings behind this partial repair.

## Next recommended action
Verify PR #119 exact HEAD through Android CI and inspect the semantic-rig probe. If it passes, retain `VERIFICATION_PENDING/BUILD_VERIFIED` only as evidence allows. Continue the same work package with the next evidence-backed CONTACT composition defect rather than reopening combat legality or inventing animation frames. Fresh exact-APK device recording remains mandatory for visual acceptance.
