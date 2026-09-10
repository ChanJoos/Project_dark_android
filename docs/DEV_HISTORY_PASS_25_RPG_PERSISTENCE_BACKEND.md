# PROJECT DARK — DEV HISTORY PASS 25 · RPG PERSISTENCE BACKEND

Date: 2026-09-10
Role: RPG · Progression · Save
Continued branch: `agent/rpg/20260910-1338`
Draft PR: #5
Latest main rechecked: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`

## Source-of-Truth gate

Re-read current main, canonical design/data/source documents, Director guide/backlog, Master manifest/reconciliation, RPG histories through PASS 24, the open RPG handoff and Draft PR #5. Master validation remains `AVAILABLE_CSV_INTEGRITY_PASS` for 92 sheets and 1,786 rows including headers.

The open PR already supplied schema-v1 in-memory snapshots and atomic state validation, but had no serialization or durable process storage. This pass closes that next P0 boundary without changing Combat, World, renderer, HUD or `GameView.java`.

## IMPLEMENTED

### Deterministic save codec

`RpgSaveCodec` serializes schema version and mutable RPG state only:

- progression node/job;
- nullable normal level/EXP/Gold;
- last processed combat sequence;
- inventory and equipment IDs/quantities;
- learned action IDs.

Map/set ordering is canonicalized for deterministic bytes. SHA-256 covers the payload. Malformed, tampered and future-schema payloads fail closed with distinct outcomes.

### Crash-safe file store

`RpgFileSaveStore` accepts an app-private directory and validated slot ID. It writes a temporary file, flushes and syncs it, preserves the prior complete primary as backup, and replaces the primary atomically where the platform supports it. Load validates the primary and recovers the last-known-good backup when necessary.

### Persistence audit

`RpgFileSaveStoreAudit` verifies:

1. initial save and process-style reload;
2. replacement with a newer snapshot;
3. corruption of the newest primary;
4. fallback to the prior valid checkpoint;
5. checksum tamper rejection.

## Verification status

- PLANNED: complete.
- IMPLEMENTED: complete on the existing RPG role branch.
- Master validation: PASS.
- Isolated Java compile: PASS.
- Codec/file-store audit: PASS.
- BUILD VERIFIED: no full Gradle/APK verification in this worker image.
- RUNTIME VERIFIED: no; Android lifecycle wiring and a real process-kill restart remain Director-owned.

## Safety

- No canonical definitions are duplicated into save data.
- Unresolved EXP and Gold remain nullable.
- No reward probability, quantity or item relation was invented.
- No ground-drop or pickup path exists.
- Corrupt data is never applied to `RpgProgressionState`.

## Next RPG P0

Add explicit schema migration plumbing, then let the Director wire the file store to app-private storage and verify inventory/equipment/learned-action/reward-checkpoint restoration after a real Android process restart.
