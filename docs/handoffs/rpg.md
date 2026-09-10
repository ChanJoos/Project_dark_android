# RPG Agent Handoff

Run: `20260910-1338`
Branch: `agent/rpg/20260910-1338`
Base main: `611cd2342a8ca010211c28794c1004663db8b1ef`
Role: RPG · Progression · Persistence

## Source-of-Truth gate
Read current main canonical contracts and latest RPG history. `docs/handoffs/rpg.md` did not exist on main at run start, so this file establishes the handoff surface.

Authoritative reward flow remains:
`MONSTER_DEFEATED -> reward resolution -> direct inventory grant -> EXP/Gold/quest progression`.
Ground drop/pickup remains retired.

## Delta in this branch
- Added `RpgSaveSnapshot` schema v1. It persists mutable RPG IDs/state only: progression node, job, normal level/EXP, nullable Gold, reward sequence checkpoint, inventory, equipment, learned action IDs.
- Added `RpgProgressionState.saveSnapshot()` and atomic fail-closed `restoreSnapshot()`.
- Restore rejects unsupported schema, invalid canonical item IDs, invalid quantities, invalid equipment references/slots, unknown action IDs, invalid progression fields, and negative known Gold.
- `lastCombatSequence` is persisted/restored so process restart cannot reset the RPG defeat/reward deduplication checkpoint.
- Added persistent `learnedActionIds` and a validated mutation API.
- Added stable `RpgActionMetadataCatalog` DTO/API exposing `actionId`, `name`, `iconKey`, `resourceCost`, `cooldown`, `learned/state`, evidence. Current runtime action values are prototype `[B]`; icon keys remain `PENDING_CROP`.
- Added `RpgPersistenceAudit` covering inventory round-trip, learned-action round-trip, nullable unresolved Gold preservation, sequence checkpoint preservation, and atomic rejection of an unknown item ID.

## Evidence / unknown preservation
- Starting Gold remains `null` / PENDING; no zero value was invented.
- Starting normal EXP remains `null` / PENDING.
- No drop probability/quantity was invented.
- No EXP/Gold progression mutation was enabled.

## Validation
Repository workflow currently triggers only for pushes to `main` or manual dispatch, so this agent branch cannot run CI without changing a non-owned workflow or merging to main. No APK was packaged by this agent. Static review was performed against current Java signatures; Director/Integrator should run compile validation when integrating.

## P0 next
1. Add serialization backend / Android process storage adapter around `RpgSaveSnapshot` without putting canonical definitions into the save payload.
2. Extend restart audit to a serialized round-trip and duplicate `MONSTER_DEFEATED` replay using a persisted non-zero sequence fixture.
3. Project 92-CSV canonical action/item metadata into runtime DTOs while retaining provenance and unknown fields.
4. Keep HUD/GameView integration outside this agent; consumers should use `RpgActionMetadataCatalog` and RPG DTOs only.
