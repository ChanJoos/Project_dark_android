# Character / Animation Handoff

## 2026-09-10 13:48 KST — agent/character/20260910-1348

### Completed
- [ADAPTED] Reduced `CharacterRenderer.PLAYER_RENDER_SCALE` from `1.35f` to `0.92f`.
- [ADAPTED] Reduced shadow presentation scale from `0.72f` to `0.58f`.
- Added explicit `LOGICAL_FOOT_ANCHOR_Y` so the renderer contract names the logical foot anchor independently from presentation scale.
- Added renderer-owned generic ATTACK swing-arc feedback when combat supplies `EffectFamily.NONE` for a normal weapon attack.
- Kept CAST blue rings, MAGIC purple cross/ring, and SKILL green arc as separate presentation hooks.
- All procedural pixels/timing remain [B]/[ADAPTED]; authenticated original sprites remain `PENDING_CROP`.

### Integration request / blocker
NPC and monster presentation are still drawn directly inside `GameView.java`. This character agent must not modify that Integrator-owned file. A future Integrator pass should delegate those draw paths to renderer APIs owned by the character domain so NPC/monster render scale, anchor and sprite proportion can be normalized without cross-agent file conflicts.

Requested bridge shape:
- `NpcPresentationRenderer.draw(Canvas, NpcPose)`
- `MonsterPresentationRenderer.draw(Canvas, MonsterPose)`
- GameView remains responsible only for supplying runtime DTO/state and calling the renderer.

### Ownership boundary preserved
No changes to map geometry/camera/collision/pathfinding/portal, combat resolution/AI/damage semantics, inventory/reward/EXP/save/progression, HUD/input, dialogue/quest state, or APK packaging.

### PR
Draft PR #6: `Character: reduce render scale and clarify action visuals`
