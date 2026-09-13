# Combat / Monster handoff

## 2026-09-10 19:56 KST — D002 reprioritization / agent/combat/20260910-1956

### Source state
- Latest main at run start: `d0fb8aa5a7d4093a18387c9ca8b242df651ca955`.
- Re-read `docs/DIRECTOR_BACKLOG.md` revision D002 and canonical `design/SOURCE_OF_TRUTH.md`.
- D002 explicitly supersedes prior visual/runtime priorities based on user device playtest evidence.
- Combat P0 is now animation/action-state playability before further runtime-session infrastructure work.

### New Combat acceptance contract
- ATTACK / CAST / SKILL_KICK must be visibly observable from actual combat action state.
- Action presentation must preserve actionId/action sequence identity and normalized progress from the shared Resolver.
- Completed/cancelled actions must return to locomotion/IDLE rather than remain stuck in an action pose.
- Damage/effect remains Resolver-owned and at most once per action. Animation projection must never mutate HP or apply a second effect.
- Target-facing is still required by D002, but facing calculation/application remains Renderer/UX integration ownership; Combat exposes targetId/action identity only.

### Implemented
- Added `CombatAnimationStateModel`, a renderer-safe projection layer with `IDLE / WALK / ATTACK / CAST / SKILL_KICK / HIT / DEAD` visual states.
- Maps Resolver `ATTACK` -> `ATTACK`, `MAGIC` -> `CAST`, `SKILL/KICK` -> `SKILL_KICK`.
- Synchronizes normalized `ActionSnapshot.progress` so presentation timing comes from the same Resolver action that owns hit timing.
- Consumes `ACTION_STARTED / ACTION_REJECTED / ACTION_CANCELLED / HIT_FEEDBACK / MONSTER_DEFEATED` without drawing or applying damage.
- Exposes explicit locomotion/hit-clear/revive inputs for World/UX integration so action completion can transition to WALK/IDLE deterministically.
- Added `CombatAnimationStateAudit` covering visible ATTACK, CAST and SKILL_KICK states, mid-action progress, one damage application per action, and return to WALK/IDLE.

### Priority changes
- Previous PASS 33 next item `CombatRuntimeSession` façade is deferred behind D002 Combat P0.
- POTION is part of the new QA HUD acceptance, but inventory consumption/heal mutation remains RPG-owned. Combat must not fabricate a potion action or canonical potion values.
- HUD layout, pressed-state visuals, touch ownership, CharacterRenderer drawing, target-facing application, APK build and runtime screenshot remain Director/UX/World-owned integration work.

### Director integration request
1. Route ATTACK/SKILL/MAGIC through the shared Resolver/Orchestrator.
2. Feed Resolver events + `actionSnapshots()` into `CombatAnimationStateModel` once per frame.
3. Bind CharacterRenderer presentation to the model snapshot instead of the detached GameView-local action timer.
4. Use `targetId` from the snapshot for facing before/during action presentation.
5. After an action disappears from Resolver snapshots, project current locomotion into WALK or IDLE.
6. Do not call any legacy direct-damage path in parallel with Resolver effect resolution.

### Boundaries preserved
No `GameView.java`, HUD/touch-coordinate, CharacterRenderer drawing, World/camera/collision/pathfinding/portal, RPG inventory/potion, reward/progression/save, NPC/quest, APK packaging or main merge changes.
