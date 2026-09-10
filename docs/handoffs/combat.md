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

---

## 2026-09-10 15:57 KST — PASS 29 runtime adapter + feedback stream hardening

### Source state
- Latest `main` rechecked and still `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.
- Continued existing Draft PR #20 / `agent/combat/20260910-1500` rather than opening a parallel combat branch.
- Ground item/pickup remains retired; no reward/inventory/progression mutation was added.

### Critical correctness fix
PASS 28 `EffectResult.defeatedNow` did not identify the defeated entity class. A monster AUTO action that killed the player could therefore satisfy the generic defeat gate and be mislabeled as `MONSTER_DEFEATED` if a port used resolver publication authority.

PASS 29 adds `DefeatedTargetKind { MONSTER, PLAYER, OTHER }`. Resolver publication now requires all of:
- `defeatedNow == true`
- target kind is `MONSTER`
- publication authority is `RESOLVER_OWNS`
- target life has not published defeat before

Player defeat can therefore never emit `MONSTER_DEFEATED` through the resolver.

### Implemented
- Added `RuntimeCombatPortAdapter` as the concrete combat-owned adapter over current `RuntimeState`.
  - Reads player/monster alive and distance facts.
  - Reads learned player actions from RPG state without mutating progression.
  - Owns combat cooldown bookkeeping for resolver submissions.
  - Mutates player MP only after resolver resource validation.
  - Delegates LOS to an injected `LineOfSightPort`; Combat does not implement map/LOS algorithms.
  - Player -> monster damage delegates to current `RuntimeState.damage()` and returns `PORT_ALREADY_PUBLISHED` because RuntimeState already writes the ledger defeat event.
  - Monster -> player damage delegates to `RuntimeState.damagePlayer()` and returns target kind `PLAYER`.
- Added `CombatResolver.HitSemantic { DAMAGE, CRIT, MISS, HEAL }` to effect results/events.
  - No crit/miss/heal probability was invented; ports may only emit those semantics when an owning runtime rule resolves them.
- Added effect-time `ACTION_CANCELLED` distinct from input-time `ACTION_REJECTED`.
  - A valid action that loses target/range/LOS before hit time is now explicitly cancelled rather than reported as a fresh input rejection.
- Added `CombatFeedbackStream`.
  - Projects resolver events into renderer/UI-safe `ACTION_STARTED / ACTION_REJECTED / ACTION_CANCELLED / EFFECT_APPLIED / HIT / CRIT / MISS / HEAL / MONSTER_DEFEATED` events.
  - Converts `HIT_FEEDBACK` into `DamageNumberModel` snapshots using an injected read-only anchor provider.
  - Combat still draws nothing and owns no HUD coordinates.
- Expanded `CombatDefeatAuthorityAudit` with the monster-kills-player misclassification regression case.
- Expanded `MonsterAIControllerAudit` with target death during WINDUP, LOS loss during WINDUP and shared-resolver rejection recovery.
- Added `CombatFeedbackStreamAudit` covering action-start -> delayed hit -> visible number projection, CRIT/MISS semantic separation and number despawn.

### User-visible combat delta
Once Director/renderer wiring consumes `CombatFeedbackStream`, the runtime can visibly distinguish:
- attack start/windup before hit,
- exact hit-time feedback,
- ordinary damage vs CRIT vs MISS vs HEAL,
- cancelled delayed attacks that lost range/LOS/target before the effect frame.

This is a presentation contract only; no renderer/GameView file was modified.

### Director integration request
1. Instantiate one `RuntimeCombatPortAdapter`, one shared `CombatResolver`, and one `CombatFeedbackStream` for the runtime combat session.
2. Tick adapter cooldowns and resolver from the runtime loop; submit both manual and MonsterAI actions to that resolver.
3. Supply LOS through the World-owned LOS implementation. Do not replace the injected LOS contract with Combat-side geometry logic.
4. Drain resolver events once, feed the same batch into `CombatFeedbackStream`, and route only the presentation snapshot/events to renderer/UI.
5. While `RuntimeState.damage()` remains the monster-death ledger authority, keep adapter result `PORT_ALREADY_PUBLISHED`. RPG consumes that single ledger event as before.
6. On respawn call `resolver.onTargetRespawned(monsterId)` and `MonsterAIController.onRespawn()`.

### Remaining P0
- Director integration is still required before these deltas are user-operable in the APK.
- Current `RuntimeCombatPortAdapter` intentionally does not fabricate a LOS algorithm; World must provide it.
- Crit/miss/heal semantics are transport-ready but production policy remains PENDING until evidence/runtime rules exist.
- Existing legacy `CombatController` can be retired only after Director migrates HUD/runtime consumers to the resolver path; this Combat branch does not edit GameView.

### Boundaries preserved
No `GameView.java`, CharacterRenderer/MonsterPresentationRenderer, map/camera/collision/pathfinding/portal implementation, reward table, inventory mutation, EXP/Gold/progression/save, NPC dialogue/quest UI, APK packaging, main push or merge changes.
