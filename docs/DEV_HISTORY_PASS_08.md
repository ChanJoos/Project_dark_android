# PROJECT DARK — DEV HISTORY PASS 08

Date: 2026-09-10
Role: Integrator · UX · QA / Design Compliance Gate

## Source-of-Truth gate executed

Read before integration:
- `master/PROJECT_DARK_V0.6_PLAN_KO.md`
- original Library workbook `LOD_Mobile_Final_Progression_World_DB_v1.0.xlsx` (all 19 parsed tabs / 487 source lines)
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `data/design/PROJECT_DARK_CANONICAL_SEED.md`
- `docs/DEV_HISTORY.md`
- `docs/DEV_HISTORY_PASS_07.md`
- latest main commits and current runtime sources

## Integration completed

### 1. Full Master DB promoted into repository

Created:
- `master/LOD_Mobile_Final_Progression_World_DB_v1.0.md`

The repository-readable master reproduces all parsed workbook tabs rather than a hand-written summary:
1. Item_Master
2. Monster_Drop
3. Economy_Loop
4. Element_Matrix
5. Element_Presets
6. Sources
7. README
8. World_Master
9. Location_Points
10. NPC_Master
11. Town_Services
12. Quest_Placement
13. World_Flow
14. World_Sources
15. Locked_Towns
16. Progression_Master
17. Skill_Milestones
18. Continuity_Audit
19. Shop_Redistribution

IDs, values, evidence/status labels, source URLs, and even stale/conflicting source rows were retained so agents cannot silently work from an abbreviated reconstruction.

### 2. Source precedence corrected

Updated `design/SOURCE_OF_TRUTH.md` so precedence is now:
1. latest explicit user canon / Constitution
2. full Master DB markdown
3. full v0.6 plan master
4. official evidence
5. verified/user evidence
6. B prototype values
7. canonical seed only as derived fallback
8. runtime implementation

The previous seed-first ambiguity is removed. Runtime code cannot override the masters for convenience.

### 3. All four autonomous agents pinned to exact master paths

World·Character, Combat·Monster, RPG·Progression, and Integrator·UX·QA now explicitly read both `master/*` files before Constitution/Contract/History. This prevents an agent from claiming to have read a Master that is not actually in its path list.

## DESIGN_CONFLICT audit

### DESIGN_CONFLICT: MASTER_ECONOMY_SPEEDBOOSTER_STALE
- Master `Economy_Loop` still lists `속도부스터` in the consumable loop.
- The same workbook README explicitly says speed booster was deleted as a recent-system item.
- Latest project canon also excludes speed booster.
- Resolution: preserve the stale row for source fidelity, but **do not implement speed booster**.

### DESIGN_CONFLICT / precedence: locked-town acquisition rows
- Item_Master contains original acquisition references such as 수오미/루어스.
- Initial runtime World_Master/Locked_Towns keeps those towns closed.
- `Shop_Redistribution` explicitly provides supply continuity through the five active towns.
- Resolution: preserve original item acquisition provenance but runtime initial-release supply follows redistribution; do not unlock a town merely because an item row names it.

### DESIGN_CONFLICT / interpretation: P00 job selection
- Progression_Master P00 mentions early `직업 선택` while the Constitution forbids job selection during character creation.
- Resolution: character creation remains gender/hair/hair color/name → 평민. P00 job choice is a later progression/exit-gate event, not creation-time selection.

### PENDING: first-advancement path details
- Master DB includes 죽음의마을/승급길 rows as historical/canonical data.
- A later explicit project no-go exists against activating 승급길 in the current runtime.
- Resolution: 1차 승급 remains in scope, but detailed 승급길 runtime expansion is **PENDING** until the higher-precedence no-go is explicitly reconciled. No agent may use lower-precedence rows to silently activate it.

## Runtime integration audit

Current runtime remains compile-safe after the prior PASS 07 CombatController split. `RuntimeState` still contains no complete canonical EXP → world drop → pickup → inventory → equipment → stat-recompute implementation, so the PLAYABLE SLICE currently terminates functionally at monster defeat/combat metrics. This is now the highest cross-module integration gap.

`GameView` still owns NPC approach/dialog orchestration and monster chase/attack loop. PASS 07 correctly identified these as remaining coupling. Do not duplicate those systems; next extraction targets are interaction orchestration and monster AI after the RPG reward chain establishes contracts.

## Validation

GitHub Actions compile validation after the Master DB import and Source-of-Truth update:
- Master DB import commit: PASS
- Source-of-Truth promotion commit: PASS

No gameplay Java was changed in this Integrator pass because the missing authoritative Master DB was a correctness blocker. Continuing RPG/world implementation before importing it would risk exactly the design drift this gate exists to prevent.

## Next integration bottleneck

Highest priority:
`MONSTER_DEFEATED → canonical reward result → world drop entity → pickup → inventory → equipment → stat recomputation`

The RPG·Progression agent owns the data/state implementation; Integrator must then wire the existing CombatLedger/monster-death event into that chain and expose evidence-safe feedback/HUD state without inventing drop rates or stats.

Secondary after that:
- extract NPC approach/dialog from `GameView` into an InteractionController,
- extract monster AI orchestration from `GameView`,
- replace hard-coded quick-slot labels/coordinates with data-driven SlotDef while preserving approved mobile HUD,
- keep individual original sprite mapping PENDING_CROP until verified.