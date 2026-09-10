# Character / Animation Handoff

## 2026-09-10 18:35 KST — agent/character/20260910-1835

### Source-of-truth / supersession gate
Started from latest main and re-read canonical design/data/source-of-truth and Character history. During the run main advanced to `ba018312ce6ca3fa68877cd60ff09606e3890273` with `design/references/CHARACTER_VISUAL_REFERENCE_20260910.md`. That newer canonical reference explicitly records the user's supplied board: scale 1.50, coherent attached anatomy, compact 2.5–3-head chibi silhouette, NW/NE/SW/SE diagonal presentation, connected shoulder→arm→hand→weapon chain, and no floating limbs. This branch was therefore rebuilt on that newer main; the earlier 18:31 branch is superseded.

### Implemented visual delta
Rebuilt `CharacterRenderer` fallback around the approved board instead of incrementally patching the old rectangular mannequin:
- presentation profile `USER_CONCEPT_20260910_CHIBI_DIAGONAL`;
- fixed user scale `1.50`, logical foot anchor `0`;
- compact ~0.34 head/body ratio, large readable head and wrapped layered hair;
- narrow blue warrior torso, shoulder armor, short connected arms, short separated legs;
- sword and off-hand shield remain connected to the body and swap visual depth with facing;
- NW/NE/SW/SE preserve near/far arm/leg depth and face/back-hair treatment;
- WALK uses short alternating steps and weight transfer;
- ATTACK/SKILL extend from the facing-side arm/weapon chain; SKILL uses a larger blue crescent matching the board's visual language;
- HIT recoils opposite facing; DEAD collapses the complete silhouette while preserving facing.

### Regression fixed
Latest main had `PLAYER_RENDER_SCALE=1.50` while `CharacterRendererAudit` still expected `1.35`. Audit now expects 1.50 and gates the approved presentation profile plus chibi proportion.

### Evidence / ownership
The supplied board is USER_APPROVED_VISUAL_DIRECTION / ADAPTED_REFERENCE, not extracted Nexon sprite pixels. Verified originals remain `PENDING_CROP`. No `GameView.java`, map/camera/collision, combat semantics, RPG, HUD/input, dialogue/quest, or APK packaging files were modified.

### Next P0
Device-playtest all four IDLE/WALK directions and at least one ATTACK/SKILL frame. If any limb still reads detached at runtime scale 1.50, continue this same visual task before unrelated Character work. After the fallback passes, replace layers with verified directional source sprites as they become available.
