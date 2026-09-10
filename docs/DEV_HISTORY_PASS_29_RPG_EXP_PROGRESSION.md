# PROJECT DARK — DEV HISTORY PASS 29 · RPG EXP PROGRESSION

Date: 2026-09-10
Role: RPG · Progression · Persistence
Branch: `agent/rpg/20260910-1338`

## Source and evidence

`master/data/Level_EXP_Curve.csv` contains all 98 transitions from Lv1 to Lv99. Every row is
`Evidence.B`, so this is an active PROJECT DARK balance prototype and is not represented as an
original-game fact.

## Implemented

- Expanded `LevelExpCurveCatalog` from the previous Lv1–10 slice to all Lv1–99 rows.
- Preserved per-row current/next level, segment, required EXP, cumulative EXP, primary hunting label,
  source path and `Evidence.B`.
- Verified cumulative arithmetic and the Lv99 total of 150,000,000 EXP.
- Verified monster EXP now mutates cumulative player EXP exactly once after `MONSTER_DEFEATED`.
- Supports multi-level gains in one reward, next-level remaining EXP and a hard Lv99 cap.
- New games start at Lv1 / 0 cumulative EXP under the explicit `[B]` curve.
- Legacy saves retaining null EXP fail with `UNINITIALIZED`; they are not silently rewritten.
- Save/restore retains the resulting level and cumulative EXP.
- Visible reward projections now emit distinct EXP and LEVEL_UP lines.

## Verification status

- PLANNED: complete.
- IMPLEMENTED: complete on the RPG role branch.
- BUILD VERIFIED: partial. Android-free compile and `RpgExpProgressionAudit` pass; full Gradle/APK
  build is not claimed.
- RUNTIME VERIFIED: no. Director must wire the existing reward presentation into the live screen.

## Visible result

With the current balanced curve, one verified `POTE_SPIRIT` EXP reward of 308,950 moves a fresh
Lv1 state to Lv10 with 69,050 EXP remaining to Lv11. Replaying the same combat sequence does not
grant EXP or show a second progression event.
