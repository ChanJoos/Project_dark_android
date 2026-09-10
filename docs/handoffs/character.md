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

## 2026-09-10 follow-up — renderer extraction prepared

### Added
- `NpcPresentationRenderer.java`
  - [ADAPTED] `NPC_RENDER_SCALE=0.88f`, `SHADOW_RENDER_SCALE=0.56f`
  - explicit logical foot anchor contract
  - `IDLE/WALK/CAST/HIT/DEAD` presentation states
  - NW/NE/SW/SE direction DTO contract
  - selected-ring and name-label presentation
  - `bodyVisualRef/hairVisualRef/equipmentVisualRef/effectVisualRef` placeholders remain `PENDING_CROP`
- `MonsterPresentationRenderer.java`
  - [ADAPTED] `MONSTER_RENDER_SCALE=0.84f`, `SHADOW_RENDER_SCALE=0.60f`
  - explicit logical foot anchor contract
  - `IDLE/WALK/ATTACK/HIT/DEAD` presentation states
  - NW/NE/SW/SE direction DTO contract
  - selected-ring, HP bar and presentation-only `WINDUP/HIT/CRIT/MISS` feedback
  - no damage calculation, targeting, attack timing, AI or death semantics are owned by this renderer

### Exact Integrator bridge requested
Replace only the drawing implementation inside Integrator-owned `GameView.java`; do not move runtime semantics into the renderers.

NPC mapping should construct `NpcPresentationRenderer.NpcPose` from the existing runtime NPC values. `interaction.approachNpc()==n` maps to `selected=true`. Until runtime exposes NPC animation state/direction, use explicit [ADAPTED] defaults rather than claiming original values.

Monster mapping should construct `MonsterPresentationRenderer.MonsterPose` from existing runtime monster values:
- `m.x/m.y/name` -> anchor/name
- `m.hp/(float)m.maxHp` -> hpRatio
- `combat.target()==m` -> selected
- `m.attackPrimed` -> `FeedbackType.WINDUP`
- existing damage-popup lifecycle -> `FeedbackType.HIT` and `lastDamage`
- runtime hit flash -> `State.HIT`
- dead entities may map to `State.DEAD` only if Integrator elects to render corpse/death frames; renderer does not decide lifecycle removal

The Integrator should own renderer instances and call:
- `npcRenderer.draw(c, pose)` from its NPC draw loop
- `monsterRenderer.draw(c, pose)` from its monster draw loop

Do not duplicate HP bars, selection rings or windup/damage popups in `GameView` after delegation because the new renderer owns those presentation details.

### Ownership boundary preserved
No changes to map geometry/camera/collision/pathfinding/portal, combat resolution/AI/damage semantics, inventory/reward/EXP/save/progression, HUD/input, dialogue/quest state, or APK packaging. `GameView.java` remains untouched on this branch.

### PR
Draft PR #6: `Character: reduce render scale and clarify action visuals`
