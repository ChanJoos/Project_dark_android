# Character / Animation Handoff

## 2026-09-10 15:48 KST — agent/character/20260910-1548

### Source state
- Latest `main` was checked before this pass; prior Character work remains unmerged and this run continues from `agent/character/20260910-1525`.
- Re-read current Source of Truth visual policy before implementation.
- Official Nexon character-creation guide page was positively identified: `https://lod.nexon.com/info/guide/82283`.
- The guide links an official Nexon-hosted screenshot containing the actual in-game male base avatar plus hair and hair-colour selection UI: `https://storage.nexon.com/dsk03/13/NX_FILE/Board/65536/05/1/000/00/00/5557536463815442599.png`.
- Official class-introduction artwork was also identified on `lod.nexon.com/info/intro` / `lwi.nexon.com`; this is class key art and must not be treated as sprite-frame evidence.

### Priority change
Per user direction, Character work now prioritizes character visual identity / body-hair-equipment composition over micro-animation polish.

### Completed this run
- Added `CharacterVisualSourceManifest` to preserve official Nexon character visual provenance in code.
- Promoted the base-avatar proportion source from pure `[B]` invention to `[O+B]`: official screenshot for silhouette/proportion evidence plus adapted procedural pixels until authenticated crops are extracted.
- Rebuilt the current base player silhouette toward the official creation-screen avatar vocabulary: larger head-to-body ratio, narrower torso, slimmer limbs and short base lower garment.
- Reworked prototype hair into a fuller creation-UI-like silhouette. The current violet tone is explicitly an adapted sample, not a claimed canonical default; the official guide confirms selectable hair colour rather than a fixed colour.
- Removed the old invented always-on shirt when `equipmentVisualRef == PENDING_CROP`. A player with no bound equipment now renders as the base/commoner-style avatar rather than falsely appearing equipped.
- When an equipment item ID is bound but its sprite is unresolved, an `[ADAPTED]` equipment overlay is still used without inventing an original item sprite.

### User-visible delta
The currently wired player is no longer the old blocky shirt/pants placeholder by default. Its body/head/hair proportion and unequipped appearance now visibly follow the official Nexon character-creation screenshot more closely while retaining the existing layered renderer and four-direction state contracts.

### Asset status / evidence discipline
- Character creation page/screenshot: official Nexon evidence `[O]` for existence, selectable sex/hair/hair-colour UI and visible base-avatar silhouette/proportion.
- Exact sprite pixels / individual directional frames / timing extracted from that screenshot: still `PENDING_CROP`.
- Class-introduction `gi_c1..gi_c5.jpg`: official key art only; not usable as direct in-game sprite-frame evidence.
- No unverified frame/timing/detail was promoted to original canon.

### Existing integration request
`GameView.java` remains untouched. NPC/monster direct drawing still needs Integrator delegation to the previously exposed `NpcPresentationRenderer` / `MonsterPresentationRenderer` APIs.

### Validation
- Character-owned code only: `CharacterVisualSourceManifest.java`, `CharacterRenderer.java`, this handoff.
- No APK packaging or Director integration performed.
- Branch CI compile remains an Integrator/Director validation responsibility under the current workflow.

### Ownership boundary preserved
No changes to `GameView.java`, world/map/camera/collision/pathfinding/portal, CombatResolver/MonsterAI/damage semantics, inventory/reward/EXP/save/progression, HUD/input, NPC dialogue or quest state.

### Next Character priority
1. Locate/crop higher-fidelity official in-game character sprites or guide/screenshots for male/female base avatars and multiple hair variants.
2. Add a proper hair visual binding contract so runtime sex/hair/hair-colour selection can replace the current sample without changing renderer internals.
3. Use verified equipment/weapon visuals where official evidence exists; keep unresolved item art `PENDING_CROP`.
4. Continue character/NPC/monster visual identity implementation before additional micro-animation polish.
