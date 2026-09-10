# RPG Agent Handoff

Run: `20260910-1338`
Branch: `agent/rpg/20260910-1338`
Base main at branch creation: `611cd2342a8ca010211c28794c1004663db8b1ef`
Draft PR: `#5`
Role: RPG · Progression · Persistence

## PASS 25 continuation — durable file persistence

Continued this existing Draft PR after re-reading current `main` at `3b58a8d08805cf9031466fe0c50d56e17aa1344d`. The PR remains mergeable and no newer main-side RPG persistence implementation was found.

- Added `RpgSaveCodec`: deterministic schema-v1 binary encoding with SHA-256 integrity digest.
- The codec preserves nullable unknown EXP/Gold and stores only mutable IDs/state, never canonical definitions.
- Decode outcomes are explicit: `DECODED`, `CHECKSUM_MISMATCH`, `UNSUPPORTED_SCHEMA`, `MALFORMED`.
- Added `RpgFileSaveStore`: callers provide an app-private directory and safe slot ID.
- Save writes and `fsync`s a temporary file, rotates the previous complete primary to `.bak`, then atomically replaces the primary where supported.
- Load prefers the current valid save and falls back to the last-known-good backup if the newest file is torn or corrupt.
- Load outcomes are explicit: `LOADED`, `LOADED_BACKUP`, `EMPTY`, `CORRUPT`, `IO_ERROR`.
- Added `RpgFileSaveStoreAudit` covering restart load, second-save replacement, corrupt-primary backup recovery and checksum rejection.

PASS 25 validation:
- isolated Java compile: PASS via `java com.sun.tools.javac.Main` with a minimal enum stub only for the pre-existing snapshot dependency;
- `RpgFileSaveStoreAudit`: PASS;
- full Gradle/APK and Android runtime: not verified in this worker image (`gradle` executable unavailable; workflow is main-only).

Director integration request:
1. Construct `RpgFileSaveStore(context.getFilesDir(), "player-1")` or another validated profile slot.
2. Save `rpg.saveSnapshot()` at explicit checkpoints/background transitions.
3. On boot, call `load()`, then apply only a non-null snapshot via `rpg.restoreSnapshot()` and surface backup recovery/corrupt outcomes.
4. Do not silently create zero EXP/Gold when the snapshot preserves `null`.

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
1. Add schema migration entry points before schema v2 exists; unknown future schemas already fail closed.
2. Wire the durable store at the Director-owned Android lifecycle boundary and verify a real process-kill restart.
3. Project canonical `Skill_Master.csv` metadata into an RPG-owned definition catalog with provenance/nullable unknowns, then map learned state onto those IDs without modifying CombatResolver/GameView.
4. Expand item/action runtime projection incrementally from the 92 CSV baseline with source/evidence retained.
5. Keep HUD/GameView integration outside this agent; consumers should use RPG DTOs only.
