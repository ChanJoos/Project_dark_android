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

---

## 2026-09-10 17:39 KST — agent/character/20260910-1739

### Source-of-Truth gate / continuity
Re-read latest `main` (`405bd764dd38146304fb3939709cf0def17f6958`) canonical design, data contract, source-of-truth, `docs/DEV_HISTORY.md`, and `docs/DEV_HISTORY_PASS_23_WORLD_CHARACTER.md` before coding. No newer canonical commit exists after the directional-character canon. Continued directly from draft PR #40 head rather than starting an unrelated topic.

### Actual unresolved seam found
NPC and monster bodies are still drawn directly inside `GameView.java` (`drawNpcs` / `drawMonsters`). Character agent does not own that file, so replacing those calls there would violate the MECE boundary.

### Completed renderer-owned delta
Added `WorldEntityPresentationRenderer.java` and `WorldEntityPresentationAudit.java`:
- independent NPC/monster presentation seam using the shared `CharacterRenderer.Direction` and `CharacterRenderer.State` contracts;
- `[ADAPTED]` reduced presentation scales `NPC_RENDER_SCALE=0.84`, `MONSTER_RENDER_SCALE=0.88` with independent shadow scales;
- `LOGICAL_FOOT_ANCHOR_Y=0` keeps runtime/world positions and collision radii untouched;
- four diagonal NW/NE/SW/SE silhouettes with direction-dependent head/body offsets and far/near limb overlap;
- NPC WALK stepping preserves facing rather than a frontal billboard;
- generic monster placeholder uses a lower/hunched directional silhouette explicitly tagged `[B]`, not claimed as original LOD art;
- ATTACK directional lunge, HIT recoil/flash, DEAD directional fall, selection ring and renderer-local effect hooks;
- `presentationState(RuntimeState.Monster)` consumes existing monster runtime state without changing combat behavior;
- `directionToward(...)` is presentation-only and never moves an entity;
- all source sprite mappings remain `PENDING_CROP`.

### Regression gate
`WorldEntityPresentationAudit` verifies:
- both actor kinds exist;
- both render scales are below 1.0 and shadow scales remain below actor scales;
- logical foot anchor remains zero;
- all four directional transforms resolve correctly;
- existing monster CHASE/WANDER, ATTACK, hitFlash and DEAD runtime states map to WALK, ATTACK, HIT and DEAD presentation states respectively.

### Integration request — owner: Integrator/UX
Replace the legacy direct primitive drawing inside `GameView.drawNpcs()` / `drawMonsters()` with `WorldEntityPresentationRenderer.draw(...)`. Integrator must own this wiring because Character agent is explicitly prohibited from modifying `GameView.java`. Feed/retain a last-known facing direction per entity so IDLE/HIT/DEAD do not collapse to a frontal or arbitrary orientation. Do not change collision radii or entity logical coordinates when adopting the renderer.

### Remaining Character P0
1. Integrator wiring of the new NPC/monster renderer seam is the concrete blocker to seeing this delta in the integrated APK.
2. Once wired, device-playtest NPC/monster proportions against the player-approved 0.92 player scale and adjust only presentation constants if needed.
3. Continue source-backed directional sprite acquisition; retain `PENDING_CROP` until positive identification.
4. After actual source crops exist, bind per-direction assets without changing the logical/state contracts introduced here.
