# PROJECT DARK Dev History — Pass 27 / Dev 1 Attack Package

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IN_DEVELOPMENT`
- Shared main consumed: `472a7184f9036f9deaa5fec655ad18c4014bcfce`
- Work branch: `dev1/attack-presentation-003-package`
- Checkpoint SHA: `6ca34ede1472d75e21580cc53fbbdedf74594a40`
- PR: NOT OPENED — package handoff gate is not yet satisfied.

## Inputs consumed
- `docs/PROJECT_STATE.yaml`
- `docs/AUTONOMY_BATON.md`
- `docs/DEV_HISTORY_PASS_26_DEV1_ATTACK.md`
- `app/src/main/java/com/projectdark/mobile/CharacterRenderer.java`
- `CharacterSemanticRigEvidenceProbe.java`
- `CharacterVisualEvidenceProbe.java`
- `GameView.java`
- `CanonicalActorFacing.java`
- `CanonicalMeleeTileContract.java`
- fresh device findings recorded for the exact-main #118 APK.

## Package acceptance matrix
1. Target canonical tile delta -> exact ATTACK facing: **PENDING runtime repair**. Current `GameView.snapshotAttackFacingTarget()` quantizes raw target delta through `CanonicalActorFacing`; package requires the accepted canonical melee relationship itself to be the source of the attack-facing snapshot.
2. CONTACT BODY facing == robe facing == weapon facing: **STATIC PASS / DEVICE PENDING**. One `Pose.direction` is consumed by the three layers; new package audit covers all four directions.
3. Equipped robe visible/composited during CONTACT: **PENDING runtime repair**. Renderer selects the robe, but current action robe registration derives local BODY/robe semantic translation and then also applies independent authored source offsets. Final global source-space registration must be unified before claiming pass.
4. Idle/startup vs CONTACT logical foot alignment: **PENDING final screen-space evidence**. Existing geometry normalizes BODY semantic foot to the logical viewport, but package-level final composite evidence is not yet sufficient.
5. CONTACT scale/height continuity: **PENDING final screen-space evidence/repair**. BODY uses the common source presentation scale, but current proof does not establish final composite silhouette continuity against equipped IDLE.
6. Mokdo handle -> action dominantHand: **IMPLEMENTED by #119 / BUILD_VERIFIED / DEVICE PENDING**. Mirrored source hand is now derived before the single final render mirror.
7. Recovery returns to equipped IDLE: **STATIC PASS / DEVICE PENDING**. Existing accepted startup/contact/recovery timing is preserved.
8. No fabricated temporal frames/replacement art: **PASS**. `SINGLE_POSE_PLACEHOLDER` remains unresolved-source truth and no art was added.

## This checkpoint
Added `AttackPresentationPackageAudit` as a package-level regression contract rather than another isolated symptom test. It covers all four canonical melee directions, locked attack facing semantics, coherent BODY/robe/weapon composition flags, shared presentation scale, the accepted one-contact-pose timing policy, and single-mirror weapon behavior.

This checkpoint deliberately does **not** claim package implementation complete and does **not** open a PR. CI success on this checkpoint would not satisfy the visual package.

## Root causes still being completed
- Facing source: presentation currently derives from a raw target delta snapshot instead of directly consuming `CanonicalMeleeTileContract.direction/facing` for the accepted melee relationship.
- Robe registration: action BODY and robe have separate source-global offsets, while runtime semantic translation is currently derived in local bitmap coordinates. This can double-count/mis-register the robe in final screen space.
- Scale/composite evidence: existing probes reason mostly in source/alpha space and do not yet prove the final rendered BODY+robe+weapon composite against equipped IDLE within explicit screen-space tolerances.

## Next action — mandatory same package
Resume `ATTACK_PRESENTATION_003_DEVICE_REPAIR` before any unrelated work. Implement the canonical-melee-facing wiring and global action BODY/robe registration, then add final screen-space geometry evidence for foot, silhouette scale and handle/hand alignment across NW/NE/SW/SE. Only after every implementable criterion is PASS may this branch be promoted to one package-level verification PR. Fresh exact-APK device evidence remains mandatory for DEVICE_VERIFIED/VISUAL_ACCEPTED.
