# Character / Animation Handoff

## 2026-09-10 17:26 KST — agent/character/20260910-1726

### Source-of-Truth gate
Read latest `main` first:
- `design/DESIGN_CONSTITUTION.md`
- `design/DATA_CONTRACT.md`
- `design/SOURCE_OF_TRUTH.md`
- `docs/DEV_HISTORY.md`
- latest relevant World/Character history `docs/DEV_HISTORY_PASS_23_WORLD_CHARACTER.md`

`docs/handoffs/character.md` did not exist on latest main, so this file re-establishes the Character handoff on the current canonical lineage.

### Canon change detected
Latest main commit `405bd764dd38146304fb3939709cf0def17f6958` canonized a stronger directional presentation rule after the previous Character PR base: normal field presentation may not remain front-facing, and `IDLE/WALK/CAST/ATTACK/SKILL/HIT/DEAD` must preserve `NW/NE/SW/SE` facing where applicable. BODY/HAIR/EQUIPMENT/WEAPON/EFFECT must share the same directional anchor/facing contract.

Therefore this run started from latest main rather than stacking further work onto stale Character PR #37.

### Completed visual delta
Updated `CharacterRenderer`:
- current user-approved presentation scale: `PLAYER_RENDER_SCALE=0.92`, `SHADOW_RENDER_SCALE=0.58`;
- logical world foot anchor remains independent at `LOGICAL_FOOT_ANCHOR_Y=0`;
- NW/NE/SW/SE now change head/torso offset and near/far arm/leg overlap, yielding four side-diagonal procedural silhouettes rather than one frontal body;
- WALK depth stepping reverses with diagonal facing;
- ATTACK weapon reach and effect direction follow facing;
- CAST/MAGIC/SKILL/PUNCH/KICK effects are direction-aware;
- HIT recoil moves opposite the current facing vector;
- DEAD fall direction follows facing while retaining the logical foot anchor;
- `DirectionalVisualSet` exposes separate NW/NE/SW/SE future sprite slots so verified crops can replace the procedural placeholder without collapsing direction.

All procedural geometry, frame cadence and effects remain `[B]/[ADAPTED]`. Authenticated original directional sprite/frame/timing remains `PENDING_CROP`.

### Regression gate
Updated `CharacterRendererAudit` to require:
- 4 directions / 7 states / 5 ordered layers / 28 direction-state cases;
- current reduced render scale `< 1.0`;
- shadow scale below actor scale;
- zero logical foot-anchor presentation offset;
- unresolved four-direction asset slots remain safe while source crops are unavailable.

### Boundaries preserved
No `GameView.java`, map/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage, inventory/reward/EXP/save/progression, HUD/input, or NPC dialogue/quest state files were modified.

### Remaining Character P0
1. NPC/monster presentation still needs the same renderer-owned scale/anchor/directional contract once stable presentation seams are available without editing `GameView.java`.
2. Continue original/fan visual evidence acquisition, but do not spend repeated passes on the exhausted legacy Tistory CDN hash path unless a new mirror/cache becomes available.
3. Replace procedural per-direction placeholder shapes only after actual directional source crops are positively identified; keep them `PENDING_CROP` otherwise.
4. Runtime integration should continue feeding `Pose.direction`, `Pose.state`, and `effectFamily`; Character agent must not duplicate combat semantics.

### PR
Draft PR #40: `Character: enforce diagonal facing across all presentation states`.
