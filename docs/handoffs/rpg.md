# RPG Agent Handoff

Run: `20260910-1554`
Branch: `agent/rpg/20260910-1338`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 29 — live early EXP + level progression

Direction: make verified monster EXP affect the actual player progression state and become visible in play, while preserving evidence labels and RPG ownership boundaries.

### Source-of-Truth gate

Re-read the current Master and found the previously missed `master/data/Level_EXP_Curve.csv` runtime balance table.

Early curve rows are explicit `[B]` project balance-model values:
- Lv1→2: 10,000 EXP
- Lv2→3: 12,800 EXP / cumulative 22,800
- Lv3→4: 16,500 / cumulative 39,300
- Lv4→5: 21,200 / cumulative 60,500
- Lv5→6: 27,300 / cumulative 87,800
- Lv6→7: 35,000 / cumulative 122,800
- Lv7→8: 45,000 / cumulative 167,800
- Lv8→9: 57,800 / cumulative 225,600
- Lv9→10: 74,400 / cumulative 300,000

`Progression_Math_Audit.csv` also records the full Lv1→99 model as monotonic and totaling 150,000,000 cumulative EXP. These are project balance facts, not claimed original constants.

### Implemented this pass

1. Added `LevelExpCurveCatalog` as the RPG-owned runtime projection for the first playable vertical slice Lv1→10.
   - cumulative EXP thresholds come directly from `Level_EXP_Curve.csv`;
   - evidence remains `[B]`;
   - no level threshold beyond Lv10 is guessed by this class;
   - built-in audit verifies monotonic thresholds and the 10,000 / 300,000 anchors.

2. Activated new-game cumulative EXP baseline at `0` under this `[B]` curve.
   - legacy/restored snapshots that explicitly carry `normalExp=null` continue to preserve null;
   - restore does not silently convert null EXP to zero.

3. Added `applyNormalExp()` with typed outcomes:
   - `APPLIED`
   - `INVALID_AMOUNT`
   - `UNINITIALIZED`
   - `PROJECTED_LEVEL_LIMIT`

4. Monster reward resolution now applies verified monster EXP directly to `normalExp` and recalculates `normalLevel` within the projected Lv1→10 range.
   - defeat/reward idempotency remains controlled by `lastCombatSequence`;
   - duplicate/stale combat events therefore cannot re-grant EXP.

5. Added `expToNextLevel()` for HUD progress display.

6. Expanded reward presentation:
   - `PlayerSummary.expToNextLevel`
   - `RewardLineKind.LEVEL_UP`
   - successful EXP reward line now reflects actual mutation outcome;
   - level-up line can render `Lv1 → LvN`;
   - old/legacy null EXP surfaces an evidence-safe pending message instead of inventing state;
   - reaching the current runtime projection boundary surfaces a pending notice rather than guessing Lv11+ behavior.

7. Added `RpgEarlyExpProgressionAudit`.
   - new player starts Lv1 / cumulative EXP 0;
   - 10,000 EXP reaches Lv2;
   - next-level remainder becomes 12,800;
   - cumulative 300,000 reaches Lv10;
   - EXP beyond current Lv10 runtime projection fails closed;
   - legacy null EXP restore remains null and cannot be silently mutated.

### Commits this pass

- `7c9d794f2912d8592e41f6ffacad4a6fe80e6b4b` — add early Level_EXP_Curve projection
- `9513cd2c48526b43717f46043d453508fd60f732` — activate cumulative EXP and level mutation
- `77bda023fbdd196cb8e4e90ad5c257484753c760` — expose EXP progress and level-up lines
- `1bed72897c3ed533ee9e05c735bf4ad34e08670e` — carry EXP outcome through inventory/reward presentation
- `0048212e721a98b4841364f47ec3b48a6c82f4c4` — add early progression regression audit

### Visible integration contract

Integrator/UX should continue consuming:
`new RpgVisibleProgressionPresentation().snapshot(state.rpg())`

Visible mapping:
- level text: `snapshot.player.level`
- accumulated EXP: `snapshot.player.exp`
- remaining EXP: `snapshot.player.expToNextLevel`
- reward toast/chat: `snapshot.latestRewardLines`
- `RewardLineKind.LEVEL_UP` should produce level-up feedback distinct from plain EXP gain.

### Important evidence boundary

The full Lv1→99 CSV exists, but PASS 29 intentionally activates only Lv1→10 for the early vertical slice. The values are `[B]` project balancing data. Do not relabel them as `[O]` or `[V]` original-game values.

The known POTE_SPIRIT reward fact (308,950 EXP `[V]`) can now drive early progression. Starting from a new Lv1 state it crosses the current early projection through Lv10. Item-drop probability/quantity is still unresolved and must remain non-emitting.

### Ownership preserved

No `GameView.java`, combat effect execution, monster AI, map/camera/pathfinding, character renderer, NPC presentation, workflow, APK packaging, or main merge changes were made.

## Next RPG P0

1. Extend `LevelExpCurveCatalog` from Lv10 through Lv99 directly from the existing CSV, retaining `[B]` provenance.
2. Add progression percentage DTO (`current-level earned / required`) so the green EXP HUD bar can stop using a hard-coded ratio.
3. Add evidence-safe job-selection transition at the Lv10/commoner gate only when gate conditions are explicit enough for mutation.
4. Project Magic data after core level/job flow is visible.
5. Director/Integrator should wire actual EXP bar + `LEVEL_UP` feedback into GameView and run compile/APK/runtime validation.


---

## PASS 29 — full Lv1–99 EXP progression

### Implemented
- `LevelExpCurveCatalog` now projects all 98 rows from `master/data/Level_EXP_Curve.csv`, not only Lv1–10.
- Every threshold remains explicitly `Evidence.B`; source path and segment/hunting metadata remain available.
- Verified monster EXP is applied after idempotent defeat consumption and can advance multiple levels.
- Progression caps at Lv99 / cumulative EXP 150,000,000.
- `ExpApplyOutcome` exposes before/after EXP and level, levels gained, reward evidence and separate curve evidence.
- `expToNextLevel()` exposes the remaining cumulative amount without inventing a separate formula.
- Fresh state is Lv1 / EXP 0 under the [B] curve. Restored null EXP remains `UNINITIALIZED` and is not silently migrated.
- Save/restore validates that level and cumulative EXP occupy the same curve interval.
- Both visible projections now provide a distinct LEVEL_UP event in addition to EXP gain.

### Verified scenario
`POTE_SPIRIT` reward EXP 308,950 from a fresh state:
- before: Lv1 / 0
- after: Lv10 / 308,950
- remaining to Lv11: 69,050
- replaying the same CombatLedger sequence: no additional EXP or history entry

### Director / UX integration request
- Render `PlayerSummary.level / exp / expToNextLevel` directly.
- Render `RewardLineKind.LEVEL_UP` or `RpgRewardFeedPresentation.Semantic.LEVEL_UP` as a distinct level-up notification.
- Preserve the `B` curve evidence in QA/debug details; do not label the curve as verified original balance.
- Do not recalculate levels in GameView.

### Verification
- Full 98-row cumulative audit: PASS.
- Reward application, multi-level gain, duplicate suppression, Lv99 cap and save/restore audit: PASS.
- Full Gradle/APK and Android runtime: pending Director integration.
