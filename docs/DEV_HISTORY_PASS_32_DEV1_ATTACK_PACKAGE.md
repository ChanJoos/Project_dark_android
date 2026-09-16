# PROJECT DARK Dev History — Pass 32 / Dev 1 Attack Package

## Work package
- ID: `ATTACK_PRESENTATION_003_DEVICE_REPAIR`
- Status: `IMPLEMENTED / VERIFICATION_PENDING`
- Runtime wiring commit: `9282c6623d5459e611bec7ceb518e4c3b589b737`
- Shared base consumed: `472a7184f9036f9deaa5fec655ad18c4014bcfce`

## Inputs consumed
- Canonical package branch and prior Pass 27–31 development/verification history.
- Existing authored mm001 group-02 BODY action bitmaps, mu0000058 group-02 robe bitmaps, mw001 source sprite.
- `AttackCompositeTransform`, `CharacterSemanticRig`, `CanonicalActorFacing`, `AttackPresentationPackageAudit`.
- Device-failed acceptance set recorded for `ATTACK_PRESENTATION_003_DEVICE_REPAIR`.

## Root cause / implementation
The final missing runtime gap was confirmed: `AttackCompositeTransform` existed and was audited but `CharacterRenderer.drawSourceAction()` still used the legacy fixed `SOURCE_PRESENTATION_SCALE` and independent BODY/robe/weapon placement.

`drawSourceAction()` now measures the accepted idle BODY alpha and authored action BODY alpha, creates one `AttackCompositeTransform.Result`, and uses that immutable final transform for BODY, robe and mw001. CONTACT apparent BODY height is normalized to idle visible height; visible foot and center are retained. Robe placement uses authored BODY/robe source-global offset delta plus residual semantic registration through the same scale/origin. Action-hand/mw001 attachment uses the same transform and the existing single-mirror contract. No crop or temporal attack frames were invented; startup / one CONTACT pose / recovery remains unchanged.

## Atomic acceptance matrix
- `목도 ↔ 손 위치`: **PASS (code/regression)** — #119 hand semantic fix preserved; CONTACT weapon now consumes the same normalized transform. Fresh device evidence still required.
- `공격 CONTACT에서 로브 유지 + BODY/robe/mw001 동일 포즈 합성`: **PASS (code/regression)** — all three consume one final composite transform. Fresh device evidence still required.
- `공격 순간 캐릭터 크기 확대 제거`: **PASS (code/regression)** — CONTACT scale is derived from idle/action visible-alpha height and foot/center are normalized in final screen space. Fresh device evidence still required.
- `3단계 / 단일 공격 포즈`: **PASS / PRESERVED** — no temporal source facts invented.
- `몬스터 방향 ↔ 실제 공격 모션 방향`: **PASS (code/regression)** — canonical adjacent target facing lock from prior package commits preserved and all CONTACT layers consume `Pose.direction` unchanged. Fresh device evidence still required.

## Regression evidence
- `AttackPresentationPackageAudit` covers four-way canonical facing, coherent BODY/robe/weapon composition, single-pose policy, final screen-space normalization, coherent layer transform and single-mirror weapon contract.
- Runtime renderer now directly invokes `AttackCompositeTransform.normalize()`; this closes the prior audit/runtime disconnect.
- Exact-head compile/CI is not yet available at this checkpoint, therefore this is **not** `BUILD_VERIFIED`.

## Remaining risk / next action
Open one package-level verification PR, run exact-head CI/build, and only after CI success promote to `BUILD_VERIFIED / DEVICE_PENDING / VISUAL_PENDING`. Device and visual acceptance require a fresh exact-APK recording confirming all atomic findings together; CI/static evidence must not promote `DEVICE_VERIFIED` or `ACCEPTED`.