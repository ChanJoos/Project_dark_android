# Character / Animation Handoff

## 2026-09-10 13:57 KST — agent/character/20260910-1405

### Source state
- Run started from latest `main` commit `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.
- Reviewed current `CharacterRenderer`, latest World/Character DEV history, and prior character PR #6 before continuing.
- Authenticated original sprite/frame/timing is still not positively verified. All procedural presentation remains `[B]` / `[ADAPTED]`; asset replacement status remains `PENDING_CROP`.

### Completed
- [ADAPTED] Reduced `CharacterRenderer.PLAYER_RENDER_SCALE` from `1.35f` to `0.92f`.
- [ADAPTED] Reduced player shadow presentation scale from `0.72f` to `0.58f`.
- Added explicit `LOGICAL_FOOT_ANCHOR_Y` so gameplay/world coordinates remain independent from presentation scale.
- Added renderer-owned generic ATTACK swing arc when `EffectFamily.NONE` is supplied for a normal weapon attack.
- Added a distinct SKILL arm pose while preserving separate CAST blue-ring, MAGIC purple cross/ring, and SKILL green-arc presentation hooks.
- Added `NpcPresentationRenderer` with independent scale, foot-anchor and placeholder visual contract.
- Added `MonsterPresentationRenderer` with independent scale, foot-anchor, selection ring, attack telegraph, HP bar, floating damage and placeholder body contract.

### User-visible delta
The currently wired player renderer becomes materially smaller on screen and receives visible generic ATTACK swing feedback without altering logical/world position or combat semantics.

### Integration request / blocker
`GameView.java` still owns the current direct NPC and monster draw paths. This character agent intentionally did not modify it.

Integrator/UX should replace those direct draw blocks with calls equivalent to:
- `NpcPresentationRenderer.draw(Canvas, NpcPose)`
- `MonsterPresentationRenderer.draw(Canvas, MonsterPose)`

The caller remains responsible for translating runtime state into immutable presentation DTOs only. Do not move NPC interaction, MonsterAI, target selection, damage resolution, reward, quest or HUD semantics into these renderers.

### Validation
- Repository branch writes completed successfully.
- No APK packaging was performed.
- No branch CI compile was available from the current repository workflow during this run; Director/Integrator should run compile/static validation before merge.

### Ownership boundary preserved
No changes to map geometry/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage calculation, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue logic, quest state, or `GameView.java`.

### PR
Draft PR #8: `Character: normalize player scale and add NPC/monster presentation contracts`
