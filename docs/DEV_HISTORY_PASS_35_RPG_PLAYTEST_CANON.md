# PASS 35 — RPG latest playtest-canon alignment

Date: 2026-09-10
Branch: `agent/rpg/20260910-1338`

## Trigger
Latest main canonical amendment: `design/PLAYTEST_CANON_20260910_1938.md` from commit `d0fb8aa5a7d4093a18387c9ca8b242df651ca955`.

The device test showed reward feedback but no actual item in inventory after killing the live `combat_dummy_01`.

## RPG correction
Added a deliberately isolated vertical-slice QA reward fixture:
- monster: `combat_dummy_01`
- item: `IT_GLOVE_LEATHER`
- quantity: `1`
- probability: `1.0` by test design
- EXP: none
- evidence: `ADAPTED`
- source label: `[B]/[ADAPTED] TEST REWARD — PLAYTEST_CANON_20260910_1938`

This is not original LOD reward data and does not resolve any canonical monster drop probabilities/quantities.

## Idempotency
`RpgPlaytestDummyRewardAudit` verifies:
- first defeat event grants exactly one item;
- inventory quantity becomes exactly 1;
- one explicit GRANTED outcome is recorded;
- replaying the same event is DUPLICATE_OR_STALE;
- quantity remains exactly 1;
- reward history remains one entry.

## Canonical safety
`CanonicalMonsterRewardCatalog.hasNoInventedDropEmission()` continues auditing canonical/original entries while intentionally excluding explicitly labeled QA fixtures.

## Ownership
No world-drop/pickup flow was reintroduced. No GameView, map, combat effect execution, renderer, HUD, or main-branch merge changes were made.
