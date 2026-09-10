# Character / Animation Handoff

## 2026-09-10 18:31 KST — agent/character/20260910-1831

### Source-of-truth gate
Started from latest main `aeb55648de8b59194b032aa084e80feb5edd6704` and read `design/DESIGN_CONSTITUTION.md`, `design/DATA_CONTRACT.md`, `design/SOURCE_OF_TRUTH.md`, `docs/DEV_HISTORY.md`, relevant latest World/Character history, and current renderer/audit before coding. `docs/handoffs/character.md` was absent on latest main, so this run recreates the handoff from current canonical/runtime state rather than assuming an older branch handoff is integrated.

### New explicit user visual canon
User supplied a character sprite concept board in chat and explicitly rejected the current character as visually wrong. The supplied concept is now the presentation target for PROJECT DARK character reconstruction: compact chibi/pixel-art proportion, large readable head, narrow armored torso, short separated legs, strong side-diagonal NW/NE/SW/SE silhouette, warrior sword+shield vocabulary, readable WALK/ATTACK/SKILL/HIT/DEAD poses, and mobile presentation scale 1.5. The concept is user-approved presentation evidence, not automatically original Nexon sprite pixels.

### Implemented delta
Rebuilt `CharacterRenderer` procedural fallback around the approved concept rather than incrementally patching the previous mannequin geometry:
- new profile `USER_CONCEPT_20260910_CHIBI_DIAGONAL`;
- scale remains latest user-approved `1.50`; logical foot anchor remains `0`;
- chibi head/body target ratio `0.34`;
- large shifted face/hair mass and explicit forward/back diagonal face handling;
- narrow blue warrior torso with shoulder armor, off-hand shield and always-readable sword;
- near/far limb and leg depth changes preserve NW/NE/SW/SE;
- WALK has short step/weight shift, ATTACK/SKILL extend along facing, HIT recoils opposite facing, DEAD falls while retaining facing;
- SKILL uses a larger blue crescent presentation closer to the supplied concept; CAST/MAGIC/HIT remain distinguishable hooks.

### Regression fix discovered
Latest main had `PLAYER_RENDER_SCALE=1.50` while `CharacterRendererAudit.USER_APPROVED_PLAYER_RENDER_SCALE` still expected `1.35`, which would make the renderer contract fail. Audit is aligned to `1.50` and now also gates the approved presentation profile and chibi head/body ratio.

### Evidence / boundaries
The supplied board is used as `[U]/user-approved visual concept + [ADAPTED]` presentation authority. No procedural pixel is labeled as extracted original Nexon art. `PENDING_CROP` remains active for eventual verified source sprite replacement. No `GameView.java`, map, camera, collision, combat semantics, RPG, HUD/input, dialogue/quest or APK packaging file was modified.

### Next Character P0
1. Integrator/Director should device-playtest this renderer profile at scale 1.50 before any further proportion tuning.
2. Continue replacing procedural BODY/HAIR/EQUIPMENT/WEAPON layers with verified directional source assets when positively identified.
3. Preserve this concept's silhouette/proportion as the fallback/golden presentation target while source assets are incomplete.
