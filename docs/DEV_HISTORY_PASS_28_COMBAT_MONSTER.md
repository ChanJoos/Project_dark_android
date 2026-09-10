# PROJECT DARK — DEV HISTORY PASS 28 · COMBAT / MONSTER

Date: 2026-09-10
Role: Combat · Monster Engine
Branch: `agent/combat/20260910-1500`

## Implemented

- Added explicit `DefeatPublication` authority to `CombatResolver.EffectResult`.
  Existing ports default to `RESOLVER_OWNS`; the current legacy RuntimeState adapter can declare
  `PORT_ALREADY_PUBLISHED` so the resolver cannot emit a second `MONSTER_DEFEATED`.
- Added `MonsterAIController` with explicit IDLE, DETECT, CHASE, WINDUP, ATTACK, RECOVER,
  CANCELLED and DEAD states.
- AI movement is delegated to a World collision port and AI attacks are submitted as
  `CombatResolver.InputMode.AUTO`, preventing a second combat formula.
- Added cancel semantics for target death/loss, cancel range, LOS loss and repeated blocked movement.
- Added deterministic audits for AI timing/state/cancellation and defeat-event authority.

## Verification status

- PLANNED: complete.
- IMPLEMENTED: complete on the existing Combat draft branch.
- BUILD VERIFIED: partial. New Android-free sources compile with the resolver contract and both new
  deterministic audits pass. Full Gradle/APK is not claimed.
- RUNTIME VERIFIED: no. Director adapter and Android execution are still required.

## User-visible delta and integration blocker

Renderer/UI can now distinguish monster detection, chase, attack telegraph, attack pose, recovery,
cancel and death without inferring state from coordinates or cooldowns. The visible behavior remains
blocked until Director wires RuntimeState movement and the shared CombatResolver adapter; this worker
did not modify GameView or World collision code.
