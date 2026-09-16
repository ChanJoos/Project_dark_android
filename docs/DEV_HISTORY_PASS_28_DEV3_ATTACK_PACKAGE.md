# PROJECT DARK Dev History — Pass 28 / Dev 3 Package Closure

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IN_DEVELOPMENT`
- Shared main consumed: `472a7184f9036f9deaa5fec655ad18c4014bcfce`
- Existing package branch consumed: `dev1/attack-presentation-003-package` at `41464ab0008539a8129d8fdb9792206dcbccb488`
- Runtime repair commit: `e3bf635d8bd64a72ab1512445e9a5ae8d4cbee3f`

## Closure audit
Dev 3 reconstructed the five fresh-device failures from `AUTONOMY_BATON` and the Pass 27 acceptance matrix. The package is not closed by #119: #119 only repairs mirrored action-hand derivation. The package-level audit added by Dev 1 is useful as a contract check but does not prove final screen-space robe/scale/foot/hand behavior and therefore cannot be used as false visual completion evidence.

## Criterion matrix
1. canonical target tile delta -> ATTACK facing: **PENDING**. `GameView.snapshotAttackFacingTarget()` still snapshots raw target presentation delta rather than consuming the accepted `CanonicalMeleeTileContract` relationship directly.
2. CONTACT BODY/robe/weapon facing agreement: **STATIC PASS / DEVICE PENDING**. One `Pose.direction` feeds all layers.
3. equipped robe during CONTACT: **RUNTIME REPAIR IMPLEMENTED / VERIFICATION PENDING**. `CharacterRenderer` already applies independent authored BODY/robe group-02 offsets; `CharacterSemanticRig.garmentTranslation()` was also using global authored offsets, double-counting registration. It now returns only residual local semantic correction.
4. idle/startup vs CONTACT logical foot: **PENDING final screen-space evidence**.
5. CONTACT scale/height continuity: **PENDING runtime/evidence**. Equal numeric source scale is not sufficient proof because action and idle silhouettes have different source alpha heights.
6. mokdo handle -> action dominantHand: **IMPLEMENTED by #119 / BUILD_VERIFIED on merged main / DEVICE PENDING**.
7. recovery -> equipped idle: **STATIC PASS / DEVICE PENDING**.
8. no fabricated temporal frames/art: **PASS**.

## Root cause fixed this pass
The renderer computes action robe placement as `ROBE_ACTION_OFFSET + geometry.offset + semanticTranslation`. Because BODY and robe authored offsets are already added independently there, a semantic translation based on `globalPelvis/globalFoot` applies the authored offset difference a second time. The repair changes detached-action garment translation to local BODY-vs-robe semantic residuals while preserving zero translation for the shared idle/walk atlas.

## Evidence / risk
- Exact runtime SHA: `e3bf635d8bd64a72ab1512445e9a5ae8d4cbee3f`.
- No canonical melee legality, attack timing, source assets, temporal policy, reward flow, monster AI, or Milles behavior changed.
- Build status for the new branch head is not promoted until exact-head Actions succeeds.
- DEVICE_VERIFIED and VISUAL_ACCEPTED remain false; fresh exact APK evidence is mandatory.

## Mandatory continuation
Remain on `ATTACK_PRESENTATION_003_DEVICE_REPAIR`. Next development pass must fix canonical-melee-facing wiring and final screen-space scale/foot normalization/evidence on this same package branch. Do not start unrelated vertical-slice work until all technically actionable criteria are PASS and the package reaches IMPLEMENTED / VERIFICATION_PENDING.