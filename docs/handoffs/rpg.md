# RPG Agent Handoff

Run: `20260910-1338`
Branch: `agent/rpg/20260910-1338`
Base main at branch creation: `611cd2342a8ca010211c28794c1004663db8b1ef`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## Source-of-Truth gate
Read current main canonical contracts, latest RPG history, and `master/data/Skill_Master.csv`. `docs/handoffs/rpg.md` did not exist on main at run start, so this file establishes the handoff surface.

Authoritative reward flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.
Ground drop/pickup remains retired.

## Delta in this branch
- Added `RpgSaveSnapshot` schema v1. It persists mutable RPG IDs/state only: progression node, job, normal level/EXP, nullable Gold, reward sequence checkpoint, inventory, equipment, learned action IDs.
- Added `RpgProgressionState.saveSnapshot()` and atomic fail-closed `restoreSnapshot()`.
- Restore rejects unsupported schema, invalid canonical item IDs, invalid quantities, invalid equipment references/slots, unknown action IDs, invalid progression fields, and negative known Gold.
- `lastCombatSequence` is persisted/restored so process restart cannot reset the RPG defeat/reward deduplication checkpoint.
- Added persistent `learnedActionIds` and a validated mutation API.
- Added stable `RpgActionMetadataCatalog` DTO/API exposing `actionId`, `name`, `iconKey`, `resourceCost`, `cooldown`, `learned/state`, evidence. Current runtime actions remain explicit prototype `[B]` fixtures; icon keys are `PENDING_CROP`.
- Added `RpgPersistenceAudit` covering inventory round-trip, learned-action round-trip, nullable unresolved Gold preservation, non-zero defeat sequence persistence, same-sequence replay suppression after a fresh RPG instance restore, next-sequence acceptance, and atomic rejection of an unknown item ID.

## Evidence / 92-CSV projection status
- `Skill_Master.csv` contains canonical skill IDs/names/job/circle/action classification and source/evidence fields. This branch does not silently replace the currently executing prototype `SkillDef` IDs with those canonical IDs because execution wiring is owned elsewhere and acquisition/resource/cooldown coverage is incomplete.
- Starting Gold remains `null` / PENDING; no zero value was invented.
- Starting normal EXP remains `null` / PENDING.
- No drop probability/quantity was invented.
- No EXP/Gold progression mutation was enabled.
- Save payload stores mutable IDs/state, not duplicated canonical definitions.

## Validation
Repository workflow currently triggers only for pushes to `main` or manual dispatch, so this agent branch cannot run CI without changing a non-owned workflow or merging to main. No APK was packaged by this agent. Static review was performed against current Java signatures and `CombatLedger.Event` package visibility. Director/Integrator must run compile validation before merge.

At PR creation, main had advanced beyond the branch base; the latest checked `RpgProgressionState.java` on main was still unchanged from the branch merge base, so no direct RPG-file concurrent edit was detected. PR remains draft.

## P0 next
1. Add a serialization backend / Android process-storage adapter around `RpgSaveSnapshot` without persisting canonical definitions.
2. Add schema migration entry points before schema v2 exists; unknown future schemas must continue to fail closed.
3. Project canonical `Skill_Master.csv` metadata into an RPG-owned definition catalog with provenance/nullable unknowns, then map learned state onto those IDs without modifying CombatResolver/GameView.
4. Expand item/action runtime projection incrementally from the 92 CSV baseline with source/evidence retained.
5. Keep HUD/GameView integration outside this agent; consumers should use RPG DTOs only.
