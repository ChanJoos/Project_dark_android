# Character / Animation Handoff

## 2026-09-10 16:27 KST — agent/character/20260910-1627

### Baseline read
- Started from latest `main` commit `c149434fe152cd74f9e7b0418ccdc7aa33d92867`.
- Re-read `design/SOURCE_OF_TRUTH.md` revision M001 / D003 and current DEV_HISTORY lineage before modifying presentation code.
- Source-of-truth policy preserved: unresolved original sprites/frames/timings remain `PENDING_CROP`; procedural values are `[B]` / `[ADAPTED]` only.

### P0 finding
Latest `main` still had `CharacterRenderer.PLAYER_RENDER_SCALE=1.35f`, which made the character oversized relative to the world and contradicted the current Character P0. World/logical coordinates must not be modified to compensate.

### Completed user-visible presentation delta
- `PLAYER_RENDER_SCALE`: `1.35f -> 0.92f`.
- `SHADOW_RENDER_SCALE`: `0.72f -> 0.58f`.
- Added `LOGICAL_FOOT_ANCHOR_Y=0f`; scale/pose transforms happen after resolving this anchor, leaving logical/world coordinates untouched.
- WALK: four-direction presentation now includes front/back depth stepping rather than only horizontal leg swapping.
- HIT: directional recoil is applied opposite facing and HIT effect includes a readable impact cross/ring.
- DEAD: body rotates and compresses around the foot-local presentation pivot; logical position is unchanged.
- ATTACK: placeholder weapon reach now phases over action time and follows NW/NE/SW/SE facing.
- SKILL: broad directional crescent + streak presentation.
- MAGIC: focused rune/cross burst remains visually distinct from CAST concentric charging rings.
- PUNCH/KICK: separate local hit silhouettes instead of sharing the generic SKILL arc.

All timing/shape values above remain prototype `[B]/[ADAPTED]`; none are claimed as original Legend of Darkness frame data.

### Contract/audit
`CharacterRendererAudit` now rejects player scales `>=1.0`, invalid shadow scale, or a mutated logical foot anchor in addition to existing 4-direction / 7-state / 5-layer checks.

### Runtime integration request
No `GameView.java` was modified. Runtime/UI integrator should continue mapping action events into `CharacterRenderer.Pose.state` and `effectFamily`:
- normal melee -> `ATTACK` (+ weapon ref when available)
- combat skill -> `SKILL` / appropriate PUNCH or KICK family
- spell/magic -> `CAST` state with `MAGIC` effect family where semantic distinction is known
- incoming damage -> `HIT`

Combat meaning, damage, cooldown and hit resolution remain outside this renderer.

### NPC / monster note
No NPC/monster scale file exists as a dedicated Character-owned renderer on current `main`; current NPC/monster drawing is still coupled elsewhere. Do not edit `GameView.java` from this agent. Director/integrator should expose NPC/monster presentation renderer APIs or move their visual drawing into Character-owned renderer classes in a later integration pass.

### Branch / PR
- branch: `agent/character/20260910-1627`
- draft PR: #37

### Next Character pass
1. Add dedicated Character-owned NPC/monster presentation renderer classes once the integration seam is available, keeping logical positions external.
2. Continue authenticated sprite acquisition / `PENDING_CROP` asset binding without inventing original frames.
3. Add renderer-side visual refs for BODY/HAIR/EQUIPMENT/WEAPON/EFFECT only after evidence identity is sufficient.

### Boundaries preserved
No GameView, world geometry/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage, inventory/reward/EXP/save/progression, HUD/input, dialogue/quest, APK packaging, merge or direct-main push changes.
