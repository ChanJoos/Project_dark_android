# Combat / Monster handoff

## 2026-09-10 17:54 KST — PASS 32 / agent/combat/20260910-1754

### Source state
- Latest main at run start remains `6785efb6f7504e070ee0c0aa6924d1281e444469`.
- Re-read canonical `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, current `RuntimeState.java`, current `MonsterAIController.java`, and previous PASS 31 handoff.
- PASS 31 branch/PR #46 is still open Draft and mergeable; this run continues from its head because latest main has not advanced beyond its base.
- Latest main MonsterAI still calls `RuntimeState.resolveMonsterAttack()` directly after legacy windup.
- Ground item/pickup gameplay remains retired. Combat emits monster defeat only; RPG owns reward/inventory/EXP/Gold/progression/save.

### Implemented
- Added `MonsterAutoCombatBridge`, a combat-owned adapter that can only submit `InputMode.AUTO` actions through `CombatActionOrchestrator.submitAuto()`.
- Bridge never calls damage APIs and cannot own a second combat formula.
- Refactored `MonsterAIController` attack execution behind injected `AttackRouter`.
- Default no-arg construction retains `LegacyAttackRouter` solely for current GameView compatibility; no GameView change was made.
- Added `SharedResolverAttackRouter`: when the legacy windup marker becomes ready it clears that marker without damage, submits the stable actionId through `MonsterAutoCombatBridge`, and exposes the submission result.
- Shared route therefore cannot call `RuntimeState.resolveMonsterAttack()` or `damagePlayer()`; actual hit timing/effect remains `CombatResolver` authority.
- Added renderer/QA-safe `AttackSubmissionSnapshot` with route, actor/target/action identity, accepted/rejected/unresolved outcome and reject reason.
- PASS 31 actor-scoped action concurrency, death-frame interruption, hit semantics and exactly-once monster defeat remain unchanged.

### User-visible combat delta
Once Director injects `SharedResolverAttackRouter`, monster windup no longer immediately commits legacy damage at ready-time. The attack becomes a real AUTO resolver action, so presentation can follow the same delayed action snapshot/hit event stream as player MANUAL attacks. Rejected/unresolved AUTO attacks are also externally visible through `AttackSubmissionSnapshot` instead of silently applying legacy damage.

### Verification contract
Added `MonsterAutoCombatBridgeAudit` covering:
- MANUAL player action + monster AUTO bridge action coexisting in one resolver;
- no damage mutation at bridge submission time;
- player manual hit resolving at its own hitTime;
- monster AUTO hit resolving only at its resolver hitTime;
- AUTO event identity preserved on `EFFECT_APPLIED`;
- unknown monster actionId fails closed as `ACTION_UNRESOLVED` without effect mutation.

### Director integration request
1. Merge/port PASS 31 actor-scoped resolver + orchestrator first.
2. Build the runtime CombatResolver.Port adapter that maps monster/player IDs to current RuntimeState facts and keeps a single MONSTER_DEFEATED authority.
3. Construct `MonsterAutoCombatBridge(sharedOrchestrator, monsterActionId)` and inject `new MonsterAIController(new MonsterAIController.SharedResolverAttackRouter(bridge))` in Director-owned wiring.
4. Tick the shared orchestrator exactly once per combat frame; drain resolver events exactly once into feedback/presentation.
5. After Director migration succeeds, remove the compatibility `LegacyAttackRouter`; Combat should then have no direct `resolveMonsterAttack()` execution path.

### Boundaries preserved
No map/camera/collision/pathfinding/portal implementation, renderer drawing, reward/inventory/EXP/Gold/progression/save, HUD/touch/GameView, NPC/dialogue/quest, APK packaging, main push or merge changes.

### Next Combat P0
- Provide/finish the RuntimeState-backed CombatResolver.Port adapter without changing RuntimeState itself, including player-vs-monster and monster-vs-player effect typing and single defeat authority.
- Add resolver cancellation/reset contract for player revive and monster respawn so stale actor slots cannot survive lifecycle resets.
- After Director removes legacy routing, delete `LegacyAttackRouter` and make shared resolver routing mandatory.

---

## 2026-09-10 17:47 KST — PASS 31 / agent/combat/20260910-1747

### Implemented
- Replaced the global pending action with one deterministic action slot per actor.
- MANUAL player action and multiple AUTO monster actions can coexist in the same resolver.
- `ACTION_BUSY` blocks only a second unfinished action from the same actor.
- Added `CombatActionOrchestrator` using stable actionId and explicit `ACTION_UNRESOLVED` failure.
- Added `ActionSnapshot` telegraph progress and immediate death interruption (`ACTOR_DEAD` / `TARGET_DEAD`).
- Added `CombatConcurrentInterruptionAudit` for concurrent actors, death-frame interruption, exactly-one defeat and surviving action resolution.
