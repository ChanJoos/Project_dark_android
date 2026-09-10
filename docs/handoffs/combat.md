# Combat / Monster handoff

## 2026-09-10 15:00 KST — agent/combat/20260910-1500

### Source state
- Base main: `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.
- Re-read `design/SOURCE_OF_TRUTH.md` and PASS 26 Combat/Monster history.
- Ground item/pickup remains retired. Combat publishes defeat only; RPG owns reward resolution/mutation.
- No prior `docs/handoffs/combat.md` existed on main. The stale empty branch `agent/combat/20260910-1437` pointed at main and contained no handoff/code delta.

### Implemented this run
- Added `CombatResolver` as the combat-owned single action path for MANUAL and AUTO.
- Added distinct action contracts for `ATTACK / SKILL / MAGIC / KICK`: actionId, ActionState and EffectType are independently carried by every event.
- Validation order: actor alive -> target alive -> learned -> cooldown -> resource -> range -> LOS -> busy.
- Accepted actions consume resource/commit cooldown once, then wait until `hitTime` before effect resolution.
- Effect-time target/range/LOS are revalidated; invalid/dead targets cannot receive delayed ghost hits.
- Every accepted action can apply its effect at most once.
- `MONSTER_DEFEATED` is published at most once per target life by the resolver; `onTargetRespawned()` opens the next life-cycle publication gate.
- Added `DamageNumberModel`: semantic `DAMAGE / CRIT / MISS / HEAL`, spawn -> upward move/fade -> despawn lifecycle, presentation DTO only.
- Added `CombatResolverAudit` covering manual/AUTO parity, action identity, learned/resource/range/LOS rejection, hit timing, duplicate-effect blocking, exact-once defeat and damage-number lifecycle.

### User-visible combat delta
Renderer/UI can now consume a stable damage-number snapshot with semantic type, display text, upward offset, alpha and progress. This enables `MISS`, crit, heal and ordinary damage to be visually distinct without Combat drawing on Canvas or touching `GameView.java`.

### Integration request — Director / UX
- Replace GameView's direct damage/resource/cooldown sequence with a thin adapter into this resolver; do not duplicate validation in the view.
- Feed existing ATTACK/MAGIC/SKILL/KICK inputs as `InputMode.MANUAL`; future AUTO feeds the same resolver with `InputMode.AUTO`.
- Convert `HIT_FEEDBACK`/`EFFECT_APPLIED` into `DamageNumberModel.spawn(...)` and pass snapshots to the renderer/UI.
- Runtime adapter must have a single defeat authority. When integrating, avoid emitting `MONSTER_DEFEATED` both from `RuntimeState.damage()` and from the resolver; migrate the existing RuntimeState death ledger emission behind the resolver adapter rather than duplicating it.
- RPG continues to consume only the resulting single `MONSTER_DEFEATED` event and owns reward/inventory/EXP/Gold/save mutation.

### Boundaries preserved
No `GameView.java`, CharacterRenderer/MonsterPresentationRenderer, map/camera/collision/pathfinding/portal, reward/inventory/progression/save, NPC/dialogue/quest or APK packaging changes.

### Next Combat P0
1. Add the concrete RuntimeState/CombatLedger adapter with a single defeat-event authority, then wire it only through Director-owned integration.
2. Add `MonsterAIController` state contract for detect/chase/windup/attack/cancel/death while delegating movement/collision steps to World.
3. Add crit/miss/heal production policy only when canonical/runtime evidence exists; current model intentionally defines semantics without inventing probabilities.


---

## 2026-09-10 — PASS 28 defeat authority + Monster AI

### Implemented
- `CombatResolver.EffectResult` now carries `DefeatPublication.RESOLVER_OWNS / PORT_ALREADY_PUBLISHED`.
- Existing ports remain source-compatible through the two-argument constructor and default to Resolver authority.
- The current `RuntimeState.damage()` adapter must return `PORT_ALREADY_PUBLISHED` because it already writes `MONSTER_DEFEATED` to `CombatLedger`; Resolver will then suppress its own defeat event.
- Added `MonsterAIController` with explicit `IDLE / DETECT / CHASE / WINDUP / ATTACK / RECOVER / CANCELLED / DEAD` snapshots.
- World remains movement/collision authority through `tryMoveToward`.
- AI submits `AttackRequest.inputMode=AUTO`; the adapter must call the same `CombatResolver.begin` path used by manual input.
- Cancellation outcomes distinguish target loss/death, cancel range, LOS, repeated move blocking and monster death.

### Director integration request
1. Build one RuntimeState-to-CombatResolver Port adapter. For legacy `RuntimeState.damage()`, calculate actual applied amount and return `EffectResult(applied, defeatedNow, PORT_ALREADY_PUBLISHED)`.
2. Consume the defeat event from exactly one stream: current `CombatLedger` while the legacy adapter remains; do not mirror a resolver defeat event.
3. Build MonsterAI Port adapters from current monster/player facts. Delegate movement to RuntimeState/World collision and submit the configured monster action through the shared Resolver with AUTO.
4. Bind renderer animation to AI snapshots/events: DETECT cue, CHASE walk, WINDUP telegraph, ATTACK pose, RECOVER, CANCELLED/IDLE and DEAD. Combat supplies state only and draws nothing.
5. On monster respawn call both `MonsterAIController.onRespawn()` and `CombatResolver.onTargetRespawned(monsterId)`.

### Verification
- Isolated Java compile with resolver contract: PASS.
- `MonsterAIControllerAudit`: PASS.
- `CombatDefeatAuthorityAudit`: PASS.
- Full Gradle/APK and Android runtime: pending Director integration.
