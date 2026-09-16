# PROJECT DARK Dev History — Pass 29 / Dev 1 Attack Package

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IN_DEVELOPMENT`
- Shared main re-fetched: `472a7184f9036f9deaa5fec655ad18c4014bcfce`
- Package branch consumed: `dev1/attack-presentation-003-package` at `4270e0ae6dcf8e065d93b501b74ebd5eeeca1348`
- Runtime checkpoint SHA: `c895879c76b66b3a8164dd221574a890da1cfc5f`

## Inputs consumed
- `docs/AUTONOMY_BATON.md`
- `docs/DEV_HISTORY_PASS_26_DEV1_ATTACK.md`
- `docs/DEV_HISTORY_PASS_27_DEV1_ATTACK_PACKAGE.md`
- `docs/DEV_HISTORY_PASS_28_DEV3_ATTACK_PACKAGE.md`
- `CanonicalMeleeTileContract`, `CanonicalActorFacing`, `GameView`, `CharacterRenderer`, `CharacterSemanticRig`, package audit.

## Root cause / change this pass
The accepted melee gate already requires an exact canonical adjacent-tile relation, but `GameView.snapshotAttackFacingTarget()` forwarded only raw target delta into `CanonicalActorFacing.beginAttack(dx,dy)`, which always re-quantized by quadrant. `CanonicalActorFacing.beginAttack(dx,dy)` now first resolves the delta through `CanonicalMeleeTileContract.facing(0,0,dx,dy)` and locks that canonical relation when present; non-canonical callers retain the existing quadrant fallback. This changes no melee legality, target selection, damage, timing, AI, assets, or temporal animation semantics.

## Atomic acceptance matrix
1. canonical target tile delta -> ATTACK facing: **IMPLEMENTED / STATIC CONTRACT PASS / BUILD PENDING**. Accepted canonical deltas now consume `CanonicalMeleeTileContract` directly before presentation lock.
2. CONTACT BODY/robe/weapon facing agreement: **STATIC PASS / DEVICE PENDING**. One locked `Pose.direction` remains shared by all layers.
3. equipped robe during CONTACT: **RUNTIME REPAIR IMPLEMENTED / BUILD PENDING / DEVICE PENDING** from Pass 28 residual-local semantic registration repair.
4. idle/startup vs CONTACT logical foot: **PENDING final screen-space evidence/runtime normalization**.
5. CONTACT scale/height continuity: **PENDING runtime normalization/evidence**. Equal numeric source scale is explicitly not accepted as proof because action and idle alpha silhouettes differ.
6. mokdo handle -> action dominantHand: **IMPLEMENTED / prior merged BUILD_VERIFIED / DEVICE PENDING**.
7. recovery -> equipped idle: **STATIC PASS / DEVICE PENDING**.
8. no fabricated temporal frames/art: **PASS**.

## Verification state
- Package remains `IN_DEVELOPMENT`; no package PR is opened/promoted.
- This checkpoint is not `BUILD_VERIFIED` until exact-head Actions/build evidence exists.
- `DEVICE_VERIFIED=false`, `VISUAL_ACCEPTED=false`.

## Mandatory continuation
Remain on this package. Next pass must complete the remaining technically actionable screen-space geometry work: derive one action composite scale/translation from final idle-vs-CONTACT visible geometry, apply that same transform to BODY + robe + mokdo, prove logical-foot alignment and explicit height/handle tolerances for all four directions, and preserve startup/contact/recovery plus source-only single-pose policy. Only then may the package move to `IMPLEMENTED / VERIFICATION_PENDING` and open a package-level verification PR.
