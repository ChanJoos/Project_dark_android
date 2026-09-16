# PROJECT DARK Dev History — Pass 31 / Dev 1 Attack Package

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IN_DEVELOPMENT`
- Shared main re-fetched: `472a7184f9036f9deaa5fec655ad18c4014bcfce`
- Package branch input HEAD: `94ef36bb1cff74443364445d6ca8a18accd1e822`
- Composite contract commit: `4254a49fa8db200765c7e5a66d1102f782d20fbc`
- Regression commit: `f40211d7bb8a8bbe98d43effedf36cd90b5ee828`

## Inputs consumed
Canonical package history through Pass 30, current `CharacterRenderer`, `CharacterSemanticRig`, `CanonicalActorFacing`, `CanonicalMeleeTileContract`, `AttackCompositeTransform`, and `AttackPresentationPackageAudit` on the package branch. Existing authored group-02 BODY/robe offsets and the accepted single-pose CONTACT policy remain authoritative.

## Root cause / change
The remaining apparent-size defect cannot be closed by equal numeric scale alone because detached action BODY silhouettes have direction-dependent visible heights. `AttackCompositeTransform` now defines the single final screen-space CONTACT transform: normalize action visible height to the accepted idle visible height, align final visible foot and center, expose one mirror pivot, place detached robe from authored offset delta plus residual semantic correction, and derive the weapon handle from the same BODY-local transform. No crop or fabricated temporal frame is introduced.

`AttackPresentationPackageAudit` now exercises this final geometry and shared-layer transform contract in all four directions in addition to canonical target-facing, composition presence, single-pose policy, and single-mirror weapon policy.

## Atomic acceptance matrix
1. Mokdo ↔ action hand registration: **PASS runtime wiring from #119 / DEVICE_PENDING**.
2. Equipped robe remains in CONTACT and shares action pose: **PASS source/runtime registration logic; composite runtime wiring PENDING / DEVICE_PENDING**.
3. CONTACT apparent size continuity: **PASS transform contract + regression; renderer runtime wiring PENDING**.
4. Logical foot continuity: **PASS transform contract + regression; renderer runtime wiring PENDING**.
5. Single 3-stage / one CONTACT pose: **PASS; unchanged**.
6. Canonical adjacent target -> locked attack facing: **PASS / IMPLEMENTED; DEVICE_PENDING**.
7. BODY + robe + weapon one final transform/mirror: **PASS transform contract + regression; renderer runtime wiring PENDING**.
8. Exact-head build: **VERIFICATION_PENDING** until workflow evidence exists for final package SHA.
9. Device/visual acceptance: **DEVICE_PENDING / VISUAL_PENDING**; requires fresh exact-APK four-direction recording after final renderer wiring.

## Remaining risk / mandatory next action
This checkpoint deliberately does not claim package completion. `CharacterRenderer.drawSourceAction()` still uses `SOURCE_PRESENTATION_SCALE` directly and independently reconstructs BODY/robe/weapon screen coordinates. The next run MUST resume this package and wire `AttackCompositeTransform.Result` into `drawSourceAction()` and `drawActionWeapon()` so BODY, robe and weapon consume the same normalized scale/origin/mirror pivot. Then compile/test exact HEAD and only promote to `IMPLEMENTED / VERIFICATION_PENDING` if every implementable criterion passes. No unrelated work should be selected first.
