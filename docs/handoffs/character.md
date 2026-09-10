# Character / Animation Handoff

## 2026-09-10 15:25 KST — agent/character/20260910-1525

### Source state
- Verified latest `main` remains `3b58a8d08805cf9031466fe0c50d56e17aa1344d`.
- Re-read current `design/SOURCE_OF_TRUTH.md`; unverified original sprite/frame/timing must remain `PENDING_CROP` and procedural presentation must remain `[B]` / `[ADAPTED]`.
- Previous Character P0 remains unmerged in PR #8, stacked PR #12, and PR #13.
- This run continues from `agent/character/20260910-1455` rather than duplicating prior work.

### Completed this run
- Added presentation-only directional recoil for the already-wired `HIT` state. Recoil moves opposite the current logical facing without changing `pose.x/pose.y` or gameplay/world coordinates.
- Added a directional hit streak to the existing HIT ring so impact direction reads more clearly across `NW/NE/SW/SE`.
- Added a static `[ADAPTED]` `DEAD` collapse transform using renderer-local rotation/flattening only; left/right collapse direction follows facing while authenticated DEAD frames remain `PENDING_CROP`.
- Expanded the DEAD shadow footprint to match the collapsed silhouette while preserving the same logical foot anchor.
- Existing render scale, WALK gait, four-direction ATTACK, CAST/MAGIC/SKILL presentation hooks and layer ordering are preserved.

### User-visible delta
The player now visibly recoils when hit and visibly collapses when dead instead of relying primarily on tint/overlay feedback. Both changes are presentation-only and consume existing renderer state; combat timing, damage semantics, revive logic and world coordinates are unchanged.

### Existing unfinished integration request
`GameView.java` still owns direct NPC/monster drawing. This agent does not modify it. Integrator/UX should delegate those paths to:
- `NpcPresentationRenderer.draw(Canvas, NpcPose)`
- `MonsterPresentationRenderer.draw(Canvas, MonsterPose)`

The caller supplies immutable presentation DTOs only. NPC interaction, MonsterAI, combat/damage/reward, quest and HUD semantics remain outside renderer ownership.

### Validation
- Modified only `CharacterRenderer.java` and this handoff in this run.
- Implementation uses existing Android `Canvas` transforms (`translate`, `rotate`, `scale`) plus existing `Paint/RectF` primitives and renderer enums.
- No `GameView.java` change, APK packaging, or Director integration performed.
- Branch CI is not automatically available under the current workflow; Director/Integrator compile validation remains required before integration.

### Ownership boundary preserved
No changes to map geometry/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage calculation, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue logic, quest state, or `GameView.java`.

### Branch/PR lineage
- PR #8: `agent/character/20260910-1405` → `main`
- PR #12: `agent/character/20260910-1427` → previous Character branch
- PR #13: `agent/character/20260910-1455` → `agent/character/20260910-1427`
- Current run: `agent/character/20260910-1525`, stacked from `agent/character/20260910-1455` because latest main remains unchanged and prior Character P0 is still unmerged.
