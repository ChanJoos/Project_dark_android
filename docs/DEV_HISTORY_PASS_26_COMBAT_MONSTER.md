# PROJECT DARK — DEV HISTORY PASS 26 / COMBAT·MONSTER

Date: 2026-09-10

## Goal
Move the isolated Pote prototype from a detached spawn fixture into a selectable runtime boot path and verify the real defeat -> reward boundary using the canonical Monster_ID `POTE_PURPLE` without inventing unresolved reward facts.

## Changes
- Added `RuntimeState.BootMode` with `MILLES` and `POTE_01_PROTOTYPE`.
- Preserved the existing no-arg `RuntimeState()` behavior as Milles for backward compatibility.
- Added Pote boot wiring so `POTE_01_PROTOTYPE` starts the player at the explicit `[B]/[ADAPTED]` Pote prototype placement and instantiates the existing `POTE_PURPLE` runtime monster fixture.
- Added `currentMapId()` so runtime consumers can distinguish `milles_runtime_proto` from `MAP_POTE_01`.
- Added `PotePrototypeRuntimeE2EAudit` and startup enforcement.

## E2E contract verified by the audit
`POTE_01_PROTOTYPE boot -> POTE_PURPLE runtime spawn -> damage to zero -> MONSTER_DEFEATED -> RpgProgressionState.consumeCombat -> PENDING_NO_CANONICAL_MONSTER_REWARD`

The audit additionally verifies:
- exactly one POTE_PURPLE runtime monster is present in the Pote boot mode;
- prototype HP is used only as the existing `[B]` combat baseline;
- a real `MONSTER_DEFEATED` ledger event is emitted;
- unresolved POTE_PURPLE reward data does not fabricate EXP or item rewards;
- inventory stays unchanged;
- repeated consumption of the same ledger does not duplicate reward resolution.

## Evidence / design safety
- `MAP_POTE_01 -> POTE_PURPLE` identity membership remains canonical from the existing Pote manifest.
- placement and combat HP remain explicit `[B]/[ADAPTED]` prototype values, not original facts.
- POTE_PURPLE has no canonical reward mapping yet, so the correct runtime outcome is fail-closed `PENDING_NO_CANONICAL_MONSTER_REWARD`.
- Ground loot/pickup remains retired. No ground-drop path was introduced.

## Commits
- `4fafb889636615ca5bd787be697b5534bf5e8b0b` — Add selectable Milles and Pote runtime boot modes
- `f36a93d76e607e3e9d5ac1b87651d54d1d4887cf` — Add Pote runtime defeat reward E2E audit
- `d633ed058aef165f570dc9ddd8dd9c2031dd1ffa` — Enforce selectable Pote runtime E2E audit

## Validation
GitHub Actions `Validate PROJECT DARK Android` Run #191 (`34438790273`) passed all relevant steps:
- Validate Master DB: success
- Compile debug sources: success
- Build debug APK: success
- Upload debug APK: success
- Complete job: success

## Remaining bottlenecks
1. GameView still boots the default no-arg RuntimeState, therefore user-visible APK remains on Milles until UX/runtime routing explicitly selects Pote mode.
2. POTE_PURPLE canonical HP/EXP/reward probability/quantity remain unresolved; prototype values must not be promoted.
3. Canonical monster AI/action profile remains unresolved; POTE_PURPLE must not silently inherit prototype AI as original behavior.
4. Next playable bottleneck: route a deliberate UI/runtime transition into `RuntimeState(BootMode.POTE_01_PROTOTYPE)` and exercise manual combat through the presentation/input layer.
