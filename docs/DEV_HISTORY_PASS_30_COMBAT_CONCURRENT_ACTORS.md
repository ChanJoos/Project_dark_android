# DEV HISTORY — PASS 30 COMBAT CONCURRENT ACTORS

Date: 2026-09-10
Owner: Combat / Monster Engine
Branch: `agent/combat/20260910-1500`
Draft PR: #20

## Implemented

- Replaced the resolver-wide single pending action with deterministic actor-scoped action slots.
- The player and multiple monsters may now wind up and resolve actions concurrently.
- `ACTION_BUSY` applies only when the same actor already owns an unfinished slot.
- Preserved one effect per accepted action and exact-once monster defeat publication.
- Added actor-specific inspection: `actionActive(actorId)`, `activeActionSequence(actorId)`, and `activeActionCount()`.
- Kept legacy aggregate accessors source-compatible.
- Added `CombatActionOrchestrator`:
  - indexes injected definitions by stable actionId,
  - routes MANUAL commands and `MonsterAIController.AttackRequest` AUTO commands into the same resolver,
  - returns explicit `ACTION_UNRESOLVED` without fabricating a definition,
  - exposes the shared event drain and actor-slot state.
- Added `CombatConcurrentActionAudit`.

## User-visible combat delta

After Director wiring, multiple monsters can independently telegraph and hit while the player performs an
action. Monster A no longer causes Monster B or the player to fail with a global `ACTION_BUSY`.
Each hit retains its own actor, target, actionId, input mode and delayed hit-frame feedback.

## Verification

Isolated Java compilation passed through the JDK compiler module.

- `CombatResolverAudit`: PASS (silent success / exit 0)
- `MonsterAIControllerAudit`: PASS
- `CombatDefeatAuthorityAudit`: PASS
- `CombatConcurrentActionAudit`: PASS
- Concurrent fixture: player MANUAL + two monster AUTO actions accepted together; three effects resolved once.
- Same-actor duplicate rejected as `ACTION_BUSY`.
- MonsterAI windup generated its private AttackRequest and reached the shared resolver through the orchestrator.
- Unknown actionId failed closed as `ACTION_UNRESOLVED`.

Full Gradle/APK build and Android runtime presentation remain Director-owned and were not run.

## Boundaries

No `GameView.java`, renderer, World, RPG/reward/inventory/progression/save, NPC/dialogue/quest or APK
packaging file was changed. No crit/miss/drop probability or original timing was invented.
