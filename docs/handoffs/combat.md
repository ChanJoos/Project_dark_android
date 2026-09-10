# Combat / Monster handoff

## 2026-09-10 17:47 KST — PASS 31 / agent/combat/20260910-1747

### Source state
- Latest main at run start: `6785efb6f7504e070ee0c0aa6924d1281e444469`.
- Re-read canonical `design/DATA_CONTRACT.md` and `design/SOURCE_OF_TRUTH.md`.
- Current main already contains the shared CombatResolver contract but still has a resolver-wide single active action slot.
- `docs/handoffs/combat.md` was absent on latest main, so this branch restores the combat handoff.
- Ground item/pickup gameplay remains retired. Combat emits only the single monster defeat event; RPG owns rewards.

### Implemented
- Replaced the global pending action with one deterministic action slot per actor.
- MANUAL player action and multiple AUTO monster actions can now coexist in the same resolver.
- `ACTION_BUSY` blocks only a second unfinished action from the same actor.
- Added `CombatActionOrchestrator` so both MANUAL and AUTO submissions enter the same resolver by stable `actionId`; unknown IDs fail closed as `ACTION_UNRESOLVED`.
- Added `ActionSnapshot` containing actor/target/action identity, state/effect/input mode, elapsed, hitTime and normalized progress for renderer/UI presentation.
- Added immediate death interruption propagation: when an effect kills an entity, unfinished actions owned by that entity cancel with `ACTOR_DEAD`, and unfinished actions targeting it cancel with `TARGET_DEAD` immediately instead of continuing ghost windup.
- Existing delayed hit validation, resource/cooldown commit, effect-at-most-once and monster-defeat exact-once gates remain intact.

### User-visible combat delta
Renderer/UI can bind attack/cast telegraph progress to `ActionSnapshot.progress`. If a monster dies during another actor's windup, the stale windup is cancelled on the death frame rather than visually completing against a corpse.

### Verification contract
`CombatConcurrentInterruptionAudit` covers:
- player + two monster actor slots concurrently;
- visible pre-hit progress between 0 and 1;
- immediate cancellation of a dead monster's unfinished attack;
- exactly one `MONSTER_DEFEATED`;
- surviving concurrent actor still resolves exactly once.

### Director integration request
- Instantiate one shared resolver/orchestrator per combat session.
- Route existing HUD manual actions and MonsterAI AUTO requests by actionId through this orchestrator.
- Renderer/UI reads `actionSnapshots()` for telegraph/windup progress and resolver events for hit/cancel feedback.
- Do not mirror `MONSTER_DEFEATED` into a second event stream if the current RuntimeState/CombatLedger adapter already publishes it.
- No GameView changes are included here.

### Boundaries preserved
No map/camera/collision/pathfinding/portal implementation, renderer drawing, reward/inventory/EXP/Gold/progression/save, HUD/touch/GameView, NPC/dialogue/quest, APK packaging, main push or merge changes.
