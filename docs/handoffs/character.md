# Character / Animation Handoff

## 2026-09-10 16:20 KST — agent/character/20260910-1620

### Source state
- Continued from draft PR #16 / `agent/character/20260910-1605`; previous Character visual work remains unmerged.
- Re-verified Nexon's official character-creation guide `https://lod.nexon.com/info/guide/82283` and creation screenshot.
- Added a second official source: right-menu / character-info guide `https://lod.nexon.com/info/guide/82295` and its Nexon-hosted character-info screenshot `https://storage.nexon.com/dsk00/03/NX_FILE/Board/65536/05/1/000/00/00/4836959960795447513.png`.
- The character-info screenshot positively shows a rendered female in-game avatar at Lv1/commoner information view and a visible equipment-slot layout around the avatar.

### Priority
Character identity / body-hair-equipment reconstruction remains ahead of micro-animation polish.

### Completed this run
- Extended `CharacterVisualSourceManifest` with official female-avatar and equipment-layout provenance.
- Updated `CharacterAppearance` so `Gender.FEMALE` now has positive official silhouette evidence rather than only a UI-choice proof.
- Added `officialInfoFemalePreview()` using the visibly green-haired official female capture as a renderer preview contract. The exact hair slot index remains adapted because the screenshot does not identify the creation-slot index.
- Reworked `CharacterRenderer` female presentation using only visible cues from the official character-info capture: narrower torso/waist, light long lower silhouette and longer side/back hair fall. Exact base-vs-equipped pixels remain unresolved, so geometry is `[ADAPTED_FROM_O]`, not claimed as extracted original sprite data.
- Added `CharacterEquipmentVisualContract` to expose semantic presentation regions for visible equipment layout (`HEAD`, `TORSO`, `MAIN_HAND`, `OFF_HAND`, `LOWER_BODY`, `FEET`, accessories, etc.) without inventing canonical RPG slot IDs or item sprites.
- Existing male creation-screen appearance, 18 hair choices, 14 colour choices, BODY/HAIR/EQUIPMENT/WEAPON/EFFECT layering, 4 directions and action-state contracts remain intact.

### User-visible delta
The renderer can now visibly produce a female avatar presentation grounded in an official Nexon in-game capture instead of using the previous male body plus a decorative outline. Female presentation has a narrower body silhouette, light long lower garment cue and longer hair fall; the official green-haired female preview is exposed through `CharacterAppearance.officialInfoFemalePreview()`.

### Evidence discipline
- Character creation page/screenshot: `[O]` for creation choices and visible male base-avatar proportion.
- Character-info page/screenshot: `[O]` for existence and visible silhouette of an in-game female avatar plus equipment-layout existence.
- Female body/hair procedural reproduction: `[O+B] / [ADAPTED_FROM_O]`.
- Exact female base unequipped sprite, exact hair slot identity, exact item sprite crops and exact equipment slot-to-RPG-slot mapping: `PENDING_CROP` / unresolved.
- No class key art or arbitrary community screenshot was promoted to sprite truth.

### Integration request
- `GameView.java` remains untouched.
- Integrator can later supply `CharacterAppearance` to `CharacterRenderer.draw(canvas, pose, appearance)` when character creation/profile state is exposed.
- RPG/Integrator should map stable canonical equipment slots/item IDs to `CharacterEquipmentVisualContract.VisualRegion`; renderer must not infer or own equip rules.
- NPC/monster direct drawing still needs Integrator delegation to the previously exposed presentation renderers.

### Validation
- Character-owned files only.
- Structural renderer contract remains 4 directions / 7 states / 5 ordered visual layers.
- No APK packaging or Director integration performed.

### Ownership boundary preserved
No changes to `GameView.java`, world/map/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage semantics, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue or quest state.

### Next Character priority
1. Find official Nexon gameplay/guide captures with clearly identifiable weapon/equipment appearances and bind only positively identified visuals to canonical item IDs.
2. Replace procedural body/hair pieces with authenticated crops incrementally when exact frame assets become available.
3. Preserve the appearance/equipment contracts so asset replacement does not require gameplay-layer rewrites.
