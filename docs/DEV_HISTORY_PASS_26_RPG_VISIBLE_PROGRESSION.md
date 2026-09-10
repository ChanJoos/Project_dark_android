# PROJECT DARK — DEV HISTORY PASS 26 · RPG VISIBLE PROGRESSION

Date: 2026-09-10
Role: RPG · Progression · Persistence

## Goal

Shift RPG work from persistence internals toward data surfaces that the live HUD can consume without moving RPG rules into `GameView.java`.

## Implemented

- Added canonical-aware `RpgActionMetadataCatalog` projection.
- Added initial Warrior action slice from `Skill_Master.csv`.
- Added `RpgVisibleProgressionPresentation` as one read-only snapshot for player progression, inventory, actions and latest reward lines.
- Preserved unresolved EXP/Gold/resource-cost/cooldown as nullable/PENDING rather than inventing zero values.
- Kept current executable CAST/SKILL/KICK bindings explicit prototype `[B]` fixtures.

## Integration boundary

Current main already routes combat ledger events into `RpgProgressionState.consumeCombat()` and already exposes visible inventory/equipment UI. Integrator can consume RPG-owned DTOs to broaden the visible HUD without duplicating mutation rules.

## Superseded by PASS 27

PASS 27 expands the initial Warrior projection to the verified basic `SK_전사_001`~`SK_전사_015` slice and adds explicit skill-book/quick-slot presentation semantics.
