# DEV HISTORY — PASS 34 Combat Runtime Session

Date: 2026-09-10
Branch: `agent/combat/20260910-1935`

## Canonical/design review

- Re-read latest `DESIGN_CONSTITUTION`, `DATA_CONTRACT`, `SOURCE_OF_TRUTH`, Director backlog, Combat history, and Combat handoff before implementation.
- No newer canonical rule conflicts with the shared MANUAL/AUTO resolver, delayed hit timing, exact-once `MONSTER_DEFEATED`, or no-ground-drop rules.
- Continued the explicit PASS 33 blocker: frame ordering and respawn synchronization around the RuntimeState-backed resolver.

## Implemented

- Added `CombatRuntimeSession`, the Combat-owned frame façade over `RuntimeCombatPortAdapter`, `CombatResolver`, `CombatActionOrchestrator`, and `CombatFeedbackStream`.
- A monotonically increasing Director `frameId` now gates each combat advance. Duplicate or stale frame calls return `STALE_FRAME_IGNORED` without re-submission, cooldown tick, effect application, event drain, or HP/resource mutation.
- MANUAL and AUTO requests are accepted in one typed request list and pass through the same action orchestrator/resolver.
- Frame order is fixed as cooldown advancement → new RuntimeState respawn-ledger synchronization → action admission → delayed resolver hit tick → single resolver-event drain → feedback/damage-number projection.
- New `MONSTER_RESPAWNED` ledger sequences call `resolver.onTargetRespawned(monsterId)` once. Retained ledger history cannot reset the same life gate again.
- Added an immutable frame snapshot containing submissions, resolver events, presentation events, active damage numbers, active action progress, and target IDs reset by respawn.

## Verification

`CombatRuntimeSessionAudit` uses the real RuntimeState surface and verifies:

1. first-life hit kills the monster only at the shared hit frame and produces one ledger `MONSTER_DEFEATED`;
2. replaying the same `frameId` causes no second submission, effect, ledger event, or HP mutation;
3. RuntimeState respawns the monster and publishes one `MONSTER_RESPAWNED`;
4. the session consumes that respawn once before admitting the next-life action;
5. the second life can be defeated and total defeat publication is exactly two, one per life;
6. the retained respawn event is not consumed again on later frames.

Android-free isolated Java compilation and audit execution: **PASS** (`CombatRuntimeSessionAudit PASS`).
`git diff --check`: **PASS**.
Full Gradle/APK and Android runtime wiring: not performed by this role.

## User-visible delta

Once Director routes a combat frame through this façade, duplicate render/update callbacks can no longer double-hit or double-publish feedback, and a respawned monster can be fought again without carrying stale defeat state from its previous life. The same frame snapshot exposes hit feedback and damage-number state for renderer consumption.

## Boundaries

- No `GameView.java`, HUD/input layout, renderer drawing, World, RPG/reward/save, NPC/dialogue, or quest code changed.
- No ground drop or pickup behavior was introduced.
- No canonical crit/miss/resource/drop values were invented.
- No APK packaging or main merge was performed.
