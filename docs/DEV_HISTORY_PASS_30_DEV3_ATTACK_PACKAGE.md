# PROJECT DARK Dev History — Pass 30 / Dev 3 Attack Package

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IN_DEVELOPMENT`
- Shared main re-fetched: `472a7184f9036f9deaa5fec655ad18c4014bcfce`
- Package branch re-fetched: `dev1/attack-presentation-003-package` at `b4c590207fdb2877b0a777e0ab3b7e056973bda5`

## Closure audit
Dev 3 reconstructed the package gate from Passes 26–29 and the exact-device regression. This package is not complete. The current branch is six commits ahead of shared main and already contains the prior robe residual-registration repair, canonical melee-facing lock, mirrored action-hand repair, and package audit. No unrelated work is allowed until the remaining screen-space criteria below are closed.

## Criterion matrix
1. Canonical adjacent target relation -> ATTACK facing: **PASS / IMPLEMENTED**. `CanonicalActorFacing.beginAttack(dx,dy)` consumes `CanonicalMeleeTileContract.facing(...)` before quadrant fallback.
2. CONTACT BODY/robe/weapon share locked facing: **PASS static contract / DEVICE_PENDING**.
3. Equipped robe present and registered during CONTACT: **PASS runtime wiring / BUILD_PENDING / DEVICE_PENDING**. Residual semantic correction no longer double-counts authored action offsets.
4. Logical foot continuity idle/startup -> CONTACT -> recovery: **PENDING**. Current renderer anchors action BODY to the logical foot, but package-level evidence does not yet prove final screen-space foot equality for all four directions after normalization.
5. CONTACT apparent height/scale continuity: **PENDING / technically actionable**. Current `SOURCE_PRESENTATION_SCALE` equality is not sufficient because detached action silhouettes have different alpha heights. Runtime needs a single composite normalization derived from authored/visible geometry and applied identically to BODY + robe + weapon, without destructive crop.
6. Mokdo handle -> action dominantHand: **PASS runtime wiring / prior BUILD_VERIFIED / DEVICE_PENDING**. Mirrored source hand is derived before the renderer mirror.
7. Startup/recovery restore equipped idle paper doll: **PASS static runtime path / DEVICE_PENDING**.
8. Source semantics: **PASS**. Group-02 remains a single-pose placeholder; no temporal frames are fabricated.
9. Package regression: **PENDING**. Existing `AttackPresentationPackageAudit` checks common numeric scale but does not yet prove final apparent-height/foot/handle tolerances after a composite normalization.
10. Exact-head build: **PENDING**. No workflow run exists for branch HEAD `b4c590207fdb2877b0a777e0ab3b7e056973bda5` at this checkpoint.
11. Device/visual acceptance: **DEVICE_PENDING / VISUAL_PENDING**. Requires a fresh APK from the exact final package SHA and four-direction attack recording.

## Execution constraint and no-false-completion decision
The available repository write path in this pass can safely replace small complete text files but does not provide a line patch primitive. `CharacterRenderer.java` is a large, concurrently evolved runtime file and the connector response is truncated before the full implementation. Replacing it from an incomplete payload would risk deleting accepted behavior. Dev 3 therefore did not fabricate a renderer commit or promote the package to IMPLEMENTED.

## Mandatory next action
Resume this SAME package. Re-fetch shared main and package HEAD, obtain the complete current `CharacterRenderer.java`, then implement one composite CONTACT geometry transform: derive normalized action scale/translation from idle-vs-action visible/source geometry; keep one logical foot anchor; apply the identical transform to BODY, robe, and action-hand/weapon attachment; extend package regression to assert all four directions' final foot, apparent-height, and handle tolerances. Compile/test exact HEAD. Only if all implementable criteria pass may status advance to `IMPLEMENTED / VERIFICATION_PENDING`; device and visual states remain pending until exact-APK evidence exists.
