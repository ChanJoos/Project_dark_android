# PROJECT DARK Dev History — Pass 31 / Dev 1 Attack Package

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IN_DEVELOPMENT`
- Shared main re-fetched: `472a7184f9036f9deaa5fec655ad18c4014bcfce`
- Package branch before writes: `94ef36bb1cff74443364445d6ca8a18accd1e822`

## User/device acceptance set
1. Mokdo ↔ action hand registration: `PASS_CODE / DEVICE_PENDING` — #119 is merged to main.
2. Equipped robe remains visible and BODY+robe+Mokdo share one CONTACT pose/composite: `PASS_WIRING_PREVIOUS / FINAL_COMPOSITE_PENDING`.
3. CONTACT character apparent-size growth: `IN_DEVELOPMENT`.
4. Three-stage startup -> single authored CONTACT pose -> recovery: `PASS / PRESERVE`.
5. Monster/target relation and visible attack pose direction: `PASS_CODE_CANONICAL_LOCK / DEVICE_PENDING`.

## This pass
Root cause for criterion 3 is that `CharacterRenderer.drawSourceAction()` still uses the fixed `SOURCE_PRESENTATION_SCALE` even though the detached group-02 BODY silhouettes have different visible alpha heights from the accepted idle cell. Numeric scale equality therefore does not imply apparent-size continuity.

Added `AttackCompositeTransform` at checkpoint `f357974ddb4569db6845b878f05982b890f0c6f3`. It derives one CONTACT transform from accepted idle alpha geometry versus action alpha geometry: normalized scale matches visible height, visible foot is locked to the accepted idle foot in final screen space, and alpha center is aligned without cropping or fabricating frames. The returned transform is explicitly intended to be consumed identically by BODY, robe, and weapon attachment.

Updated `AttackPresentationPackageAudit` at checkpoint `9313e899303f3d4c134db7d23b1779e72d449c53`. The package audit no longer treats equal numeric idle/action scale as proof; it now asserts final screen-space apparent-height, foot, and center tolerances for four representative directional action silhouettes.

## Remaining implementation gate
- `CharacterRenderer.drawSourceAction()` must consume `AttackCompositeTransform` using actual idle/action alpha bounds and apply its single scale/origin to BODY, robe and action-hand/weapon.
- Robe placement must remain in the same normalized composite coordinate system; no independent layer scale or second authored-offset correction.
- Weapon handle must be transformed from the action BODY local dominantHand through that same composite transform, then mirrored exactly once where required.
- Compile/package audit must pass at exact branch HEAD.
- No package PR/handoff until those runtime wiring criteria are implemented. Device/visual states remain pending until a fresh exact-APK four-direction recording.

## Evidence discipline
No temporal group-02 semantics were invented. The accepted three-stage/single-pose policy remains unchanged. This is a partial implementation checkpoint and does not complete the work package.
