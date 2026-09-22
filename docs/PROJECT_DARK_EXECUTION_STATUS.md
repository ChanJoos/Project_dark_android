# PROJECT DARK execution status — Bundle A

Updated: 2026-09-23 KST
Scope: A0–A6 only. B–D intentionally deferred.

## Baseline
- User-approved candidate SHA: `11836dc428d74e3d3525da88352c229c476f6f9b`.
- Repository default `main` at audit start: `bf9d83f79ab0a604c807ef4d30c657604154fad6`.
- Compare result: approved candidate is 30 commits ahead of main and 0 behind; do not reset to main.
- Working branch: `director/bundle-a-20260923`, created directly from approved candidate.
- Repository rules: `AGENTS.md` LOOP-4-V1; preserve approved movement/appearance, direct auto-loot, domain ownership, and separate build/runtime/visual acceptance.
- Save slot: `project_dark_f5m_v1`, schema 2.
- Android CI: `.github/workflows/android.yml`; Java 17 / Gradle 8.11.1 / Android 35; master validation, compile, focused audits/tests, assembleDebug, artifact upload.

## A0 — VERIFIED (source mapping)
Quest 1:
- `F5mAdaptedPrologueQuest`: AVAILABLE → ACTIVE → RETURN_READY → COMPLETED; combat sequence watermark.
- `F5mTurnInCoordinator` + `F5mSaveStore.commitTurnInActive`: atomic completion + one-time reward.
- `F5mQuestUiFlow`, `GameView`: accept/decline/turn-in UI wiring.

Quest 2:
- `GrowthQuest2`: LOCKED → AVAILABLE → ACTIVE → RETURN_READY → COMPLETED; 3 target defeats; turn-in.
- `ActiveQuestTracker`, `GameView.autoNavigateQuest2`: Quick Quest target projection/routing.

Growth:
- `RpgProgressionState`, `LevelExpCurve`, `LevelGrowthPolicy`, `FinalStats`.
- Canonical SSOT: `design/CHARACTER_STAT_SYSTEM_V1.md`.
- Current runtime already uses 2 points/level, does not auto-increment primary stats, stores durable base MaxHP/MaxMP, and keeps equipment modifiers separate.
- Exact original HP/MP growth functions remain PENDING by SSOT; current `BalancedV1` is explicitly [B], replaceable, and is not reported as original canon.

Save/restart:
- `F5mSaveStore`: schema 2, quest/reward/progression/inventory/equipment/location/resource checkpoint.
- `F5mRestartPersistenceMatrixTest`, `RuntimeCheckpointTest`: restart/idempotency/legacy-save coverage.

Protected approved behavior:
- `CanonicalActorFacing` / audit, `PlayerFourWayVisualContractAudit`, `CharacterRenderer`.
- inventory overlay remains non-pausing; UI touch interception remains in `GameView`.
- auto-loot remains in `RpgProgressionState`; no ground-drop restoration.

## A1 — IMPLEMENTED, CI verification pending
Existing implementation already provides one-way quest transitions and atomic one-time prologue/Quest2 turn-in. No runtime rewrite made.

## A2 — IMPLEMENTED, CI verification pending
Existing tracker projects Quest1/Quest2 state to NPC/monster/NPC targets and production route audit exists. No navigation redesign made.

## A3 — IMPLEMENTED with explicit PENDING boundary, CI verification pending
Canonical SSOT is respected for +2 points/level and no automatic primary-stat growth. Exact original HP/MP formula is unresolved; current [B] replaceable policy remains rather than inventing original values.

## A4 — IMPLEMENTED, CI verification pending
Existing schema-2 checkpoint and atomic turn-in transactions reused. Unsupported future schema/unknown saved items preserve the source save and disable writes rather than overwriting it.

## A5 — CI verification pending
Run existing Android workflow from this exact branch/SHA. Device-only screen/touch claims remain DEVICE_PENDING unless the user tests the produced APK.

## A6 — pending CI artifact
After exact-head CI passes, record final SHA, KST build time, artifact, SHA-256 if downloadable bytes are available, and device checklist (max 5).

## Deferred
B0 is the next ticket after Bundle A. B–D are not part of this execution.
