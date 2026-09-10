# Combat / Monster handoff

## 2026-09-10 18:35 KST — PASS 33 / agent/combat/20260910-1835

### Source state
- Latest main at run start: `ba018312ce6ca3fa68877cd60ff09606e3890273`.
- Previous PASS 32 PR #48 was closed after Director manually integrated the MonsterAI shared-resolver bridge onto main.
- Main already contains actor-scoped `CombatResolver`, `CombatActionOrchestrator`, `MonsterAutoCombatBridge`, and injected MonsterAI shared route.
- `docs/handoffs/combat.md` was absent on latest main, so this branch restores the current Combat handoff.
- Ground item/pickup remains retired. Combat never creates ground rewards; RPG owns reward/inventory/progression mutation.

### Implemented this run
- Added `RuntimeCombatPortAdapter`, a concrete `CombatResolver.Port` over the current `RuntimeState` surface.
- Actor/target alive and positions are resolved from `player` or stable monster IDs.
- Player MP is the only currently exposed runtime resource pool. Resource-costing monster actions fail closed rather than inventing monster MP/resource semantics.
- Per-actor/per-action cooldown clocks are owned by this combat adapter and exposed through `cooldownRemaining()`; Director must tick the adapter once per combat frame.
- LOS truth is injected through `LineOfSightPort`; Combat does not implement map/geometry LOS.
- Learned-action truth is injected through `LearnedActionPort`; Combat does not fabricate learned skills/magic from missing runtime state.
- Player -> monster damage delegates to `RuntimeState.damage(monster, amount)`.
- Monster -> player damage delegates to `RuntimeState.damagePlayer(amount)`.
- Actual applied damage is calculated from before/after runtime HP so feedback reflects clamping at zero.
- Because current RuntimeState already publishes `MONSTER_DEFEATED` / `PLAYER_DEFEATED` into `CombatLedger`, adapter results use `PORT_ALREADY_PUBLISHED`; Resolver therefore never mirrors a second defeat event.
- Target kind remains explicit (`MONSTER / PLAYER / OTHER`) so player death cannot be misclassified as monster defeat.

### Verification contract
Added `RuntimeCombatPortAdapterAudit` against the actual RuntimeState API surface. It covers:
1. player MAGIC accepts only when learned/resource/range/LOS gates pass;
2. MP is consumed once on accepted action start;
3. monster HP is unchanged before hitTime and mutates only at the Resolver hit frame;
4. action cooldown blocks immediate resubmission and expires only through adapter tick;
5. monster AUTO attack mutates player HP only at the shared Resolver hit frame and keeps `InputMode.AUTO` on `EFFECT_APPLIED`;
6. monster death publishes exactly one RuntimeState `CombatLedger.MONSTER_DEFEATED` and zero duplicate Resolver `MONSTER_DEFEATED` while legacy ledger authority remains;
7. monster kill of player publishes exactly one `PLAYER_DEFEATED` and zero `MONSTER_DEFEATED`.

### User-visible combat delta
After Director wires this adapter, MANUAL and AUTO actions no longer stop at an abstract resolver contract: the same delayed hit frame mutates the real RuntimeState HP/MP/cooldown surface. Renderer feedback can therefore line up with actual runtime HP changes instead of a parallel prototype formula.

### Director integration request
- Construct one `RuntimeCombatPortAdapter(state, worldLosPort, learnedActionPort)` per combat session.
- Construct one shared `CombatResolver(adapter)` and `CombatActionOrchestrator` using stable action definitions.
- Inject `MonsterAIController.SharedResolverAttackRouter(new MonsterAutoCombatBridge(orchestrator, monsterActionId))`.
- Each combat frame: tick adapter cooldowns, tick MonsterAI/input submission, tick orchestrator once, drain resolver events once, then project feedback.
- While current `RuntimeState.damage()` / `damagePlayer()` keep ledger defeat publication, consume defeat only from `CombatLedger`; do not mirror Resolver defeat events.
- A later ownership migration may move defeat publication fully into Resolver, but only after RuntimeState ledger emission is removed atomically by Director/Integrator.

### Boundaries preserved
No `GameView.java`, HUD/touch layout, renderer drawing, map/camera/collision/pathfinding/portal implementation, reward/inventory/EXP/Gold/progression/save, NPC/dialogue/quest, APK packaging, main push or merge changes.

### Next Combat P0
1. Provide a small combat-session façade that guarantees adapter cooldown tick + Resolver tick/event drain ordering so Director cannot accidentally double-tick or double-drain.
2. Add explicit respawn synchronization: RuntimeState `MONSTER_RESPAWNED` must call `resolver.onTargetRespawned(monsterId)` exactly once before the next life can publish a defeat.
3. Keep crit/miss/heal production policy PENDING until a canonical or explicitly approved runtime source exists; transport semantics only are already supported.
